package iptiq.balancer;

import iptiq.provider.Provider;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class LoadBalancer {
    public static final int MAX_PROVIDERS = 10;
    private static final Object mutex = new Object();
    private final ArrayList<Provider> registeredProviders;
    private int currentProvider;
    private int included;
    private final int healthCheckFrequency;
    Thread healthChecker;

    public enum QUERY_POLICY {
        ROUND_ROBIN,
        RANDOM
    }

    QUERY_POLICY queryPolicy = QUERY_POLICY.ROUND_ROBIN;

    public LoadBalancer(int frequency) {
        registeredProviders = new ArrayList<>();
        currentProvider = 0;
        included = 0;
        healthCheckFrequency = frequency;

        healthChecker = new Thread(() -> {
            while (true) {
                this.runCheck();
                try {
                    Thread.sleep(healthCheckFrequency * 1000);
                } catch (InterruptedException ex) {
                    System.out.println(ex.getMessage());
                    System.exit(-1);
                }
            }
        });
        healthChecker.start();
    }

    public void registerProvider(String identifier) {
        synchronized (mutex) {
            if (registeredProviders.size() > MAX_PROVIDERS) {
                throw new IllegalArgumentException("Max number of allowed providers exceeded.");
            }
            if (Provider.providerExists(identifier)) {
                throw new IllegalArgumentException("Provider with the name " + identifier + " already exists.");
            }
            registeredProviders.add(Provider.getProviderInstance(identifier));
            included++;
        }
    }

    public String get() {
        if (queryPolicy == QUERY_POLICY.ROUND_ROBIN) {
            return getRoundRobinProviderResponse();
        } else {
            return getRandomProviderResponse();
        }
    }

    public int get(int numOfRequests) {
        int totalCapacity = 0;
        synchronized (mutex) {
            for (Provider provider : registeredProviders) {
                if (provider.providerWorking()) {
                    totalCapacity += provider.getCapacity();
                }
            }
            if (totalCapacity < numOfRequests)
                return totalCapacity - numOfRequests;
            return numOfRequests;
        }
    }

    private String getRandomProviderResponse() {
        synchronized (mutex) {
            if (included < 1) {
                return "";
            }
            int providerIdx = ThreadLocalRandom.current().nextInt(0, registeredProviders.size());
            while (!registeredProviders.get(providerIdx).providerWorking()) {
                providerIdx++;
                if (providerIdx == registeredProviders.size())
                    providerIdx = 0;
            }
            return registeredProviders.get(providerIdx).get();
        }
    }

    private String getRoundRobinProviderResponse() {
        synchronized (mutex) {
            if (included < 1) {
                return "";
            }
            currentProvider++;
            if (currentProvider == registeredProviders.size())
                currentProvider = 0;
            while (!registeredProviders.get(currentProvider).providerWorking()) {
                currentProvider++;
                if (currentProvider == registeredProviders.size())
                    currentProvider = 0;
            }
            return registeredProviders.get(currentProvider).get();
        }
    }

    public void setQueryPolicy(QUERY_POLICY policy) {
        queryPolicy = policy;
    }

    public QUERY_POLICY getQueryPolicy() {
        return queryPolicy;
    }

    public void runCheck() {
        System.out.println("Health check runs");
        System.out.println("...........");
        synchronized (mutex) {
            included = 0;
            for (Provider registeredProvider : registeredProviders) {
                if (!registeredProvider.check()) {
                    System.out.println(
                            "Excluding provider " + registeredProvider.providerIdentifier);
                } else {
                    included++;
                }
            }
        }
    }

    public void stopBalancer() {
        try {
            healthChecker.join();
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
            System.exit(-1);
        }
    }

    public boolean setProviderCapacity(int idx, int capacity) {
        if (idx < registeredProviders.size()) {
            registeredProviders.get(idx).setCapacity(capacity);
            return true;
        }
        return false;
    }
}
