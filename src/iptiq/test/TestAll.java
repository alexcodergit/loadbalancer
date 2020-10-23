package iptiq.test;

public class TestAll {
    /*
     * test functionality
     * like mentioned in requirements no use of any frameworks
     */
    public static void main(String[] args) {
        TestProvider.testProviderCreation();
        TestLoadBalancer.testLoadBalancerRegisterProviders();
        TestLoadBalancer.testLoadBalancerCapacity();
        TestLoadBalancer.testLoadBalancerRoundRobinPolicy();
        TestLoadBalancer.testLoadBalancerHealthChecker();
        System.out.println("All tests passed.");
    }
}
