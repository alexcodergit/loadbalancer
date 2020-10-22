package iptiq;

import iptiq.balancer.LoadBalancer;

import java.util.concurrent.ThreadLocalRandom;


public class RunLoadBalancer {
    public static void main(String[] args) {
        LoadBalancer balancer = new LoadBalancer(5);
        balancer.registerProvider("1");
        balancer.setProviderCapacity(0, 15);
        balancer.registerProvider("2");
        balancer.setProviderCapacity(1, 20);
        balancer.registerProvider("3");
        balancer.registerProvider("4");
        balancer.registerProvider("5");
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
            if (queryCounter % 10 == 0) {
                if (balancer.getQueryPolicy() == LoadBalancer.QUERY_POLICY.RANDOM) {
                    System.out.println("Changing query policy to ROUND_ROBIN");
                    balancer.setQueryPolicy(LoadBalancer.QUERY_POLICY.ROUND_ROBIN);
                } else {
                    System.out.println("Changing query policy to RANDOM");
                    balancer.setQueryPolicy(LoadBalancer.QUERY_POLICY.RANDOM);
                }
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
        balancer.stopBalancer();
    }
}
