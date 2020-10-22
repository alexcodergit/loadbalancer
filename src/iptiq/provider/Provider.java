package iptiq.provider;

import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

public class Provider {
    private static final HashSet<String> createdProviders = new HashSet<>();
    public final String providerIdentifier;
    private int capacity;
    // chance of dying is equal 1 / robustness
    // hiher robustness means lower probability of dying
    private int robustness;
    private int aliveHeartBeatLimit;
    private int currentHeartBeatCount;
    private boolean working;

    private Provider(String identifier) {
        providerIdentifier = identifier;
        robustness = 5;
        capacity = 10;
        aliveHeartBeatLimit = 2;
        currentHeartBeatCount = aliveHeartBeatLimit;
        working = true;
    }

    public String get() {
        if (working)
            return providerIdentifier;
        else
            throw new IllegalStateException("Provider not working.");
    }

    public boolean providerWorking() {
        return working;
    }

    public boolean check() {
        int dead = ThreadLocalRandom.current().nextInt(0, robustness);
        if (dead == 0) {
            currentHeartBeatCount = 0;
            working = false;
            return false;
        } else if (currentHeartBeatCount < aliveHeartBeatLimit - 1) {
            currentHeartBeatCount++;
            return false;
        } else {
            working = true;
            return true;
        }
    }

    public boolean setRobustness(int r) {
        if (r > 1) {
            robustness = r;
            return true;
        }
        return false;
    }

    public int getRobustness() {
        return robustness;
    }

    public boolean setCapacity(int cap) {
        if (cap > 0) {
            capacity = cap;
            return true;
        }
        return false;
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean setAliveHeartBeatLimit(int limit) {
        if (limit > 0) {
            aliveHeartBeatLimit = limit;
            return true;
        }
        return false;
    }

    public int getAliveHeartBeatLimit() {
        return aliveHeartBeatLimit;
    }

    public static Provider getProviderInstance(String identifier) {
        if (createdProviders.contains(identifier))
            throw new IllegalArgumentException("Provider " + identifier + " already exists.");
        Provider provider = new Provider(identifier);
        createdProviders.add(identifier);
        return provider;
    }

    public static boolean providerExists(String providerIdentifier) {
        return createdProviders.contains(providerIdentifier);
    }
}
