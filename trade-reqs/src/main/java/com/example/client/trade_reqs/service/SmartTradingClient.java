// SmartTradingClient.java
package com.example.client.trade_reqs.service;

import com.example.brokerrmi.rmi_stock_exchange.broker.TradingService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SmartTradingClient {
    private static final List<String> BROKER_PORTS = List.of("1099", "1100", "1101");
    private static final String HOST = "localhost";

    private TradingService currentBroker;
    private String currentBrokerId;

    public void connect() {
        // Try brokers in random order for load balancing
        List<String> shuffledPorts = new ArrayList<>(BROKER_PORTS);
        Collections.shuffle(shuffledPorts);

        for (String port : shuffledPorts) {
            try {
                Registry registry = LocateRegistry.getRegistry(HOST, Integer.parseInt(port));
                TradingService broker = (TradingService) registry.lookup("TradingService");

                if (broker.isAlive()) {
                    this.currentBroker = broker;
                    this.currentBrokerId = broker.getBrokerId();
                    System.out.println("Connected to Broker: " + currentBrokerId + " on port " + port);
                    return;
                }
            } catch (Exception e) {
                System.out.println("Failed to connect to port " + port + ": " + e.getMessage());
            }
        }
        throw new RuntimeException("No available brokers found!");
    }

    public boolean buyStock(String symbol, int quantity, String clientId) {
        try {
            return currentBroker.buyStock(symbol, quantity, clientId);
        } catch (Exception e) {
            System.out.println("Broker " + currentBrokerId + " failed, reconnecting...");
            connect(); // Reconnect to another broker
            return buyStock(symbol, quantity, clientId); // Retry
        }
    }

    // Similar methods for sellStock, getPrice, etc.
}