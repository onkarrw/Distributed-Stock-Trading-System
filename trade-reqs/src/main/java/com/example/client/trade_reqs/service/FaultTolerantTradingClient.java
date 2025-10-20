package com.example.client.trade_reqs.service;

import com.example.brokerrmi.rmi_stock_exchange.broker.TradingService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FaultTolerantTradingClient {
    private final List<String> brokerAddresses;
    private TradingService currentBroker;
    private String currentBrokerId;

    public FaultTolerantTradingClient() {
        this.brokerAddresses = Arrays.asList(
                "localhost:1099",
                "localhost:1100",
                "localhost:1101"
        );
    }

    private void connectToBroker(String address) throws Exception {
        String[] parts = address.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        Registry registry = LocateRegistry.getRegistry(host, port);
        TradingService broker = (TradingService) registry.lookup("TradingService");

        this.currentBroker = broker;
        this.currentBrokerId = broker.getBrokerId();
        System.out.println("Connected to: " + currentBrokerId + " at " + address);
    }

    public void connect() {
        Collections.shuffle(brokerAddresses); // Load balancing

        for (String address : brokerAddresses) {
            try {
                connectToBroker(address);
                return;
            } catch (Exception e) {
                System.out.println("Failed to connect to " + address + ": " + e.getMessage());
            }
        }
        throw new RuntimeException("All brokers are unavailable!");
    }

    public boolean executeTrade(String operation, String symbol, int quantity, String clientId) {
        int retries = brokerAddresses.size();

        while (retries > 0) {
            try {
                switch (operation.toLowerCase()) {
                    case "buy" -> {
                        return currentBroker.buyStock(symbol, quantity, clientId);
                    }
                    case "sell" -> {
                        return currentBroker.sellStock(symbol, quantity, clientId);
                    }
                    default -> throw new IllegalArgumentException("Unknown operation: " + operation);
                }
            } catch (Exception e) {
                System.out.println("Operation failed on " + currentBrokerId + ", retrying...");
                connect(); // Reconnect to another broker
                retries--;
            }
        }
        throw new RuntimeException("Operation failed after all retries");
    }

    public double getStockPrice(String symbol) {
        int retries = brokerAddresses.size();

        while (retries > 0) {
            try {
                return currentBroker.getPrice(symbol);
            } catch (Exception e) {
                System.out.println("Price query failed on " + currentBrokerId + ", retrying...");
                connect();
                retries--;
            }
        }
        throw new RuntimeException("Price query failed after all retries");
    }

    public Map<String, Double> getAllStocks() {
        int retries = brokerAddresses.size();

        while (retries > 0) {
            try {
                return currentBroker.getAllStocks();
            } catch (Exception e) {
                System.out.println("Get all stocks failed on " + currentBrokerId + ", retrying...");
                connect();
                retries--;
            }
        }
        throw new RuntimeException("Get all stocks failed after all retries");
    }

    public void registerBroker(String brokerId, TradingService brokerService) {
        int retries = brokerAddresses.size();

        while (retries > 0) {
            try {
                currentBroker.registerBroker(brokerId, brokerService);
                return;
            } catch (Exception e) {
                System.out.println("Register broker failed on " + currentBrokerId + ", retrying...");
                connect();
                retries--;
            }
        }
        throw new RuntimeException("Register broker failed after all retries");
    }

    public void updateStock(String symbol, double newPrice, String sourceBrokerId) {
        int retries = brokerAddresses.size();

        while (retries > 0) {
            try {
                currentBroker.updateStock(symbol, newPrice, sourceBrokerId);
                return;
            } catch (Exception e) {
                System.out.println("Update stock failed on " + currentBrokerId + ", retrying...");
                connect();
                retries--;
            }
        }
        throw new RuntimeException("Update stock failed after all retries");
    }

    public void syncStockData(Map<String, Double> stockData) {
        int retries = brokerAddresses.size();

        while (retries > 0) {
            try {
                currentBroker.syncStockData(stockData);
                return;
            } catch (Exception e) {
                System.out.println("Sync stock data failed on " + currentBrokerId + ", retrying...");
                connect();
                retries--;
            }
        }
        throw new RuntimeException("Sync stock data failed after all retries");
    }

    public boolean isAlive() {
        int retries = brokerAddresses.size();

        while (retries > 0) {
            try {
                return currentBroker.isAlive();
            } catch (Exception e) {
                System.out.println("Health check failed on " + currentBrokerId + ", retrying...");
                connect();
                retries--;
            }
        }
        throw new RuntimeException("Health check failed after all retries");
    }

    public String getCurrentBrokerId() {
        int retries = brokerAddresses.size();

        while (retries > 0) {
            try {
                return currentBroker.getBrokerId();
            } catch (Exception e) {
                System.out.println("Get broker ID failed on " + currentBrokerId + ", retrying...");
                connect();
                retries--;
            }
        }
        throw new RuntimeException("Get broker ID failed after all retries");
    }

    // Additional utility methods

    public String getConnectionStatus() {
        try {
            boolean alive = isAlive();
            String brokerId = getCurrentBrokerId();
            return "Connected to: " + brokerId + " | Status: " + (alive ? "🟢 ALIVE" : "🔴 DEAD");
        } catch (Exception e) {
            return "🔴 DISCONNECTED - No active broker connection";
        }
    }

    public void printAllStocks() {
        try {
            Map<String, Double> stocks = getAllStocks();
            System.out.println("\n📋 Available Stocks:");
            System.out.println("┌────────────┬─────────────┐");
            System.out.println("│ Symbol     │ Price       │");
            System.out.println("├────────────┼─────────────┤");
            stocks.forEach((symbol, price) ->
                    System.out.printf("│ %-10s │ $%-10.2f │%n", symbol, price));
            System.out.println("└────────────┴─────────────┘");
        } catch (Exception e) {
            System.out.println("❌ Failed to retrieve stock list: " + e.getMessage());
        }
    }

    public void printDetailedStockInfo(String symbol) {
        try {
            double price = getStockPrice(symbol);
            System.out.printf("\n📊 %s Stock Information:%n", symbol);
            System.out.printf("   Current Price: $%.2f%n", price);
            System.out.printf("   Connected Broker: %s%n", getCurrentBrokerId());
            System.out.printf("   Connection Status: %s%n", getConnectionStatus());
        } catch (Exception e) {
            System.out.println("❌ Failed to get stock information for " + symbol + ": " + e.getMessage());
        }
    }

    // Method to simulate multiple rapid trades (for testing)
    public void executeMultipleTrades(String operation, String symbol, int quantity, int numberOfTrades) {
        System.out.printf("\n🔄 Executing %d %s operations for %s...%n", numberOfTrades, operation.toUpperCase(), symbol);

        int successCount = 0;
        for (int i = 1; i <= numberOfTrades; i++) {
            try {
                boolean success = executeTrade(operation, symbol, quantity, "batch-client-" + i);
                if (success) {
                    successCount++;
                    System.out.printf("   %d/%d: ✅ SUCCESS - %s %d %s%n", i, numberOfTrades, operation.toUpperCase(), quantity, symbol);
                } else {
                    System.out.printf("   %d/%d: ❌ FAILED - %s %d %s%n", i, numberOfTrades, operation.toUpperCase(), quantity, symbol);
                }

                // Small delay to see the replication in action
                Thread.sleep(100);

            } catch (Exception e) {
                System.out.printf("   %d/%d: 💥 ERROR - %s%n", i, numberOfTrades, e.getMessage());
            }
        }

        System.out.printf("\n📊 Batch Operation Summary:%n");
        System.out.printf("   Successful: %d/%d%n", successCount, numberOfTrades);
        System.out.printf("   Success Rate: %.1f%%%n", (successCount * 100.0 / numberOfTrades));

        // Show final price after all operations
        try {
            double finalPrice = getStockPrice(symbol);
            System.out.printf("   Final %s Price: $%.2f%n", symbol, finalPrice);
        } catch (Exception e) {
            System.out.println("   Could not retrieve final price");
        }
    }
}