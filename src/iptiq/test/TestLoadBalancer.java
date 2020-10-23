package iptiq.test;

import iptiq.balancer.LoadBalancer;
import iptiq.provider.Provider;

public class TestLoadBalancer {

    public static void testLoadBalancerRegisterProviders() {
        LoadBalancer balancer = new LoadBalancer();
        String response = balancer.get();
        if (response.length() > 0)
            throw new RuntimeException("testLoadBalancerRegisterProviders test failed.");
        Provider provider = Provider.getProviderInstance("2");
        balancer.registerProvider(provider);
        response = balancer.get();
        if (response.length() < 1)
            throw new RuntimeException("testLoadBalancerRegisterProviders test failed.");
        System.out.println("testLoadBalancerRegisterProviders ok");
    }

    public static void testLoadBalancerCapacity() {
        LoadBalancer balancer = new LoadBalancer();
        Provider provider3 = Provider.getProviderInstance("3");
        provider3.setCapacity(10);
        balancer.registerProvider(provider3);

        Provider provider4 = Provider.getProviderInstance("4");
        provider4.setCapacity(15);
        balancer.registerProvider(provider4);
        int capacity = balancer.getCapacity();
        if (capacity != 25)
            throw new RuntimeException("testLoadBalancerCapacity failed.");
        System.out.println("testLoadBalancerCapacity ok");
    }

    public static void testLoadBalancerRoundRobinPolicy() {
        LoadBalancer balancer = new LoadBalancer();
        Provider provider5 = Provider.getProviderInstance("5");
        balancer.registerProvider(provider5);

        Provider provider6 = Provider.getProviderInstance("6");
        balancer.registerProvider(provider6);

        Provider provider7 = Provider.getProviderInstance("7");
        balancer.registerProvider(provider7);
        balancer.setQueryPolicy(LoadBalancer.QUERY_POLICY.ROUND_ROBIN);
        String response = balancer.get();
        if (!response.equals("6"))
            throw new RuntimeException("testLoadBalancerRoundRobinPolicy failed.");

        response = balancer.get();
        if (!response.equals("7"))
            throw new RuntimeException("testLoadBalancerRoundRobinPolicy failed.");

        response = balancer.get();
        if (!response.equals("5"))
            throw new RuntimeException("testLoadBalancerRoundRobinPolicy failed.");

        System.out.println("testLoadBalancerRoundRobinPolicy ok");
    }

    public static void testLoadBalancerHealthChecker() {
        LoadBalancer balancer = new LoadBalancer();
        Provider provider8 = Provider.getProviderInstance("8");
        provider8.setCapacity(10);
        balancer.registerProvider(provider8);

        if (!balancer.startHealthChecker()) {
            throw new RuntimeException("testLoadBalancerHealthChecker failed");
        }
        balancer.stopHealthChecker();

        if (!balancer.startHealthChecker()) {
            throw new RuntimeException("testLoadBalancerHealthChecker failed");
        }
        balancer.stopHealthChecker();

        if (!balancer.startHealthChecker()) {
            throw new RuntimeException("testLoadBalancerHealthChecker failed");
        }
        balancer.stopHealthChecker();
        System.out.println("testLoadBalancerHealthChecker ok");
    }
}
