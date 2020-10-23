package iptiq.test;

import iptiq.provider.Provider;

public class TestProvider {

    public static void testProviderCreation() {
        Provider provider = Provider.getProviderInstance("1");
        try {
            Provider tmp = Provider.getProviderInstance("1");
            throw new RuntimeException("Creating two providers with same identifier must be not possible.");

        } catch (IllegalArgumentException ex) {
            System.out.println("testProviderCreation ok");
        }
    }
}
