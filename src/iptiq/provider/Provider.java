package iptiq.provider;

import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

public class Provider {
    private static final HashSet<String> createdProviders = new HashSet<>();
    public final String providerIdentifier;
    // how many requests the provider can process parallel
    private int capacity;
    // chance of dying is equal 1 / robustness
    // hiher robustness means lower probability of dying
    private int robustness;
    // how many consecutive alive heart beats needed to make the node 'alive'
    private int aliveHeartBeatLimit;
    // when provider 'dies' counter is set to 0
    private int currentHeartBeatCount;
    // when provider 'dies' 'working' is set false
    private boolean working;

    /*
     * Some values are assigned for
     * default robustness, capacity, aliveHeartBeatLimit,
     * aliveHeartBeatLimit
     * These values can be changed using getters/setters
     * Constructor wit private specifier
     * new provider instances can be made only using getProviderInstance method
     */
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

    /*
     * checks health of the provider
     * failure of the node simulated using random number
     * a number in the range 0 - robustness generated
     * when the number is 0, node 'dies'
     */
    public boolean check() {
        int dead = ThreadLocalRandom.current().nextInt(0, robustness);
        // 0 is chosen arbitrary, it can be any other number
        // in the range 0 - robustness - 1
        if (dead == 0) {
            currentHeartBeatCount = 0;
            working = false;
            return false;
            // not enough consecutive alive beats yet
        } else if (currentHeartBeatCount < aliveHeartBeatLimit - 1) {
            currentHeartBeatCount++;
            return false;
        } else {
            // currentHeartBeatCount == aliveHeartBeatLimit
            working = true;
            return true;
        }
    }

    /*
     * Provider nodes may be not equal
     * here is the possibility to set robustness
     * of the provider individually
     */
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

    /*
     * the nodes do not need to be equal to each other
     * here individual capacity can be set.
     */
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

    /*
     * How many consequtive 'alive' beats must happen
     * to make the node 'alive'
     */
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

    /*
     * To make sure that every provider has unique identifier
     * the providers IDs stored in hash set
     * in reality providers are physical or virtual machines
     *  their numbers are limited
     */
    public static Provider getProviderInstance(String identifier) {
        if (createdProviders.contains(identifier))
            throw new IllegalArgumentException("Provider " + identifier + " already exists.");
        Provider provider = new Provider(identifier);
        createdProviders.add(identifier);
        return provider;
    }

    /*
     * allows to check whether specific identifier can be used
     * to create new provider
     */
    public static boolean providerExists(String providerIdentifier) {
        return createdProviders.contains(providerIdentifier);
    }
}
