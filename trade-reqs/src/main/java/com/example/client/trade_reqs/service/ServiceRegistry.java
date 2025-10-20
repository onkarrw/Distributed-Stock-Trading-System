// ServiceRegistry.java
package com.example.client.trade_reqs.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRegistry {
    private static final Map<String, String> BROKER_REGISTRY = new ConcurrentHashMap<>();

    static {
        // Pre-register known brokers
        BROKER_REGISTRY.put("broker1", "localhost:1099");
        BROKER_REGISTRY.put("broker2", "localhost:1100");
        BROKER_REGISTRY.put("broker3", "localhost:1101");
    }

    public static String getRandomBroker() {
        List<String> brokers = new ArrayList<>(BROKER_REGISTRY.values());
        Random rand = new Random();
        return brokers.get(rand.nextInt(brokers.size()));
    }

    public static List<String> getAllBrokers() {
        return new ArrayList<>(BROKER_REGISTRY.values());
    }
}