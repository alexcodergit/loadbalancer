package iptiq.balancer;

import iptiq.provider.Provider;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class LoadBalancer {
    public static final int MAX_PROVIDERS = 10;
    // synchronizes main thread and health checker thread
    private static final Object mutex = new Object();
    private final ArrayList<Provider> registeredProviders;
    // points to provider in  'registeredProviders' that accepted last request
    private int currentProvider;
    // number of healthy available providers
    private int included;
    // providers will be checked every 'healthCheckFrequency' seconds
    private int healthCheckFrequency;
    // separate thread for health checking
    private Thread healthChecker;
    // indicates whether healt checker is siarted
    private boolean healthCheckerStarted;
    private String healthCheckMessage;

    // policy for dispatching requests
    public enum QUERY_POLICY {
        ROUND_ROBIN,
        RANDOM
    }

    QUERY_POLICY queryPolicy = QUERY_POLICY.ROUND_ROBIN;

    /*
     * constructor sets some default values
     */
    public LoadBalancer() {
        registeredProviders = new ArrayList<>();
        currentProvider = 0;
        included = 0;
        healthCheckFrequency = 10;
        healthCheckerStarted = false;
        healthCheckMessage = "";
    }

    /*
     * registers providers for processing requests
     * in real application we would need to check whether
     *  the provider is already registered at another load balancer
     *  here for simplicity we skip the check
     */
    public void registerProvider(Provider provider) {
        synchronized (mutex) {
            if (registeredProviders.size() > MAX_PROVIDERS) {
                throw new IllegalArgumentException("Max number of allowed providers exceeded.");
            }
            registeredProviders.add(provider);
            included++;
        }
    }

    /*
     * returns true when health checker thread is started
     * and was not already running
     */
    public boolean startHealthChecker() {
        if (!healthCheckerStarted) {
            healthChecker = new Thread(() -> {
                while (true) {
                    if (healthCheckMessage.length() > 0)
                        System.out.println(healthCheckMessage);
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
            healthCheckerStarted = true;
            return true;
        }
        return false;
    }

    /*
     * checks current dispatching policy
     * before dispatching requests
     */
    public String get() {
        if (queryPolicy == QUERY_POLICY.ROUND_ROBIN) {
            return getRoundRobinProviderResponse();
        } else {
            return getRandomProviderResponse();
        }
    }

    /*
     * dispatches multiple requets
     * checks available total capacity
     */
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

    /*
     * next provider t process requets will be chosen randomly
     * synchronization with health checker is needed
     */
    private String getRandomProviderResponse() {
        synchronized (mutex) {
            if (included < 1) {
                return "";
            }
            int providerIdx = ThreadLocalRandom.current().nextInt(0, registeredProviders.size());
            // if provider with providerIdx not working look for the next working
            while (!registeredProviders.get(providerIdx).providerWorking()) {
                providerIdx++;
                if (providerIdx == registeredProviders.size())
                    providerIdx = 0;
            }
            return registeredProviders.get(providerIdx).get();
        }
    }

    /*
     * next provider to process request will be chosen based on round robin algorithm
     * synchronization with health checker has to be used
     */
    private String getRoundRobinProviderResponse() {
        synchronized (mutex) {
            // no active nodes, request will be NOT processed
            if (included < 1) {
                return "";
            }
            // index points to provider that accepted last request
            currentProvider++;
            if (currentProvider == registeredProviders.size())
                currentProvider = 0;
            // look for working provider
            // because included > 0 at least one will be found
            while (!registeredProviders.get(currentProvider).providerWorking()) {
                currentProvider++;
                if (currentProvider == registeredProviders.size())
                    currentProvider = 0;
            }
            return registeredProviders.get(currentProvider).get();
        }
    }

    /*
     * query policy for dispatching requests
     */
    public void setQueryPolicy(QUERY_POLICY policy) {
        queryPolicy = policy;
    }

    public QUERY_POLICY getQueryPolicy() {
        return queryPolicy;
    }

    /*
     * 'dead' providers will be excluded from active nodes
     *  to prevent race conditions with requests dispatcher
     *  mutex is used
     */
    public void runCheck() {
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

    /*
     * try to join health check thread
     */
    public void stopHealthChecker() {
        healthChecker.interrupt();
        healthCheckerStarted = false;
    }

    public int getHealthCheckFrequency() {
        return healthCheckFrequency;
    }

    /*
     * providers health will be checked every frequency seconds
     */
    public boolean setHealthCheckFrequency(int frequency) {
        if (frequency > 0) {
            healthCheckFrequency = frequency;
            return true;
        }
        return false;
    }

    /*
     * combined capacity of working providers
     */
    public int getCapacity() {
        int result = 0;
        synchronized (mutex) {
            for (Provider provider : registeredProviders) {
                if (provider.providerWorking()) {
                    result += provider.getCapacity();
                }
            }
            return result;
        }
    }

    /*
     * Sets health check message to follow simulation
     */
    public void setHealthCheckMessage(String healthCheckMessage) {
        this.healthCheckMessage = healthCheckMessage;
    }
}
