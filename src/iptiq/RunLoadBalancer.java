package iptiq;

import iptiq.balancer.LoadBalancer;
import iptiq.provider.Provider;

import java.util.concurrent.ThreadLocalRandom;

/*
 * LoadBalance running the simulation
 */
public class RunLoadBalancer {
    public static void main(String[] args) {
        LoadBalancer balancer = new LoadBalancer();

        Provider provider1 = Provider.getProviderInstance("1");
        provider1.setCapacity(10);
        provider1.setAliveHeartBeatLimit(2);
        balancer.registerProvider(provider1);

        Provider provider2 = Provider.getProviderInstance("2");
        provider1.setCapacity(10);
        provider1.setAliveHeartBeatLimit(3);
        balancer.registerProvider(provider2);

        Provider provider3 = Provider.getProviderInstance("3");
        provider1.setCapacity(10);
        provider1.setAliveHeartBeatLimit(2);
        balancer.registerProvider(provider3);

        Provider provider4 = Provider.getProviderInstance("4");
        provider1.setCapacity(10);
        provider1.setAliveHeartBeatLimit(2);
        balancer.registerProvider(provider4);

        balancer.setHealthCheckMessage("Health check is running...");

        if (!balancer.startHealthChecker())
            System.out.println("Health checker already started.");

        int queryCounter = 0;

        while (queryCounter < 1000) {
            System.out.println("Sending request: " + queryCounter);
            String response = balancer.get();
            if (response.length() > 0)
                System.out.println("Getting response from provider " + response);
            else
                System.out.println("No providers currently available, please resend response ");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
                System.exit(-1);
            }
            queryCounter++;
            // all 10 queries change policy
            if (queryCounter % 10 == 0) {
                if (balancer.getQueryPolicy() == LoadBalancer.QUERY_POLICY.RANDOM) {
                    System.out.println("Changing query policy to ROUND_ROBIN");
                    balancer.setQueryPolicy(LoadBalancer.QUERY_POLICY.ROUND_ROBIN);
                } else {
                    System.out.println("Changing query policy to RANDOM");
                    balancer.setQueryPolicy(LoadBalancer.QUERY_POLICY.RANDOM);
                }

                System.out.println("Processing multiple requests:");
                int requests = ThreadLocalRandom.current().nextInt(30, 70);
                int resp = balancer.get(requests);
                if (resp > 0)
                    System.out.println("Multiple requets rocessed: " + resp);
                else {
                    System.out.println("Available capacity exceeded by " + -1 * resp);
                }
            }
        }
        balancer.stopHealthChecker();
    }
}
