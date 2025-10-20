// TradingServiceImpl.java
package com.example.brokerrmi.rmi_stock_exchange.broker;

import com.example.brokerrmi.rmi_stock_exchange.Model.Stock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

@Service
public class TradingServiceImpl extends UnicastRemoteObject implements TradingService {
    private static final Logger logger = Logger.getLogger(TradingServiceImpl.class.getName());

    private final String brokerId;
    private final int port;
    private final Map<String, Stock> stockDatabase;
    private final CopyOnWriteArrayList<TradingService> peerBrokers;
    private final CopyOnWriteArrayList<String> registeredBrokerIds;

    // Use @Value to inject properties instead of constructor parameters
    public TradingServiceImpl(
            @Value("${broker.id:broker1}") String brokerId,
            @Value("${rmi.port:1099}") int port) throws RemoteException {
        super(port);
        this.brokerId = brokerId;
        this.port = port;
        this.stockDatabase = new ConcurrentHashMap<>();
        this.peerBrokers = new CopyOnWriteArrayList<>();
        this.registeredBrokerIds = new CopyOnWriteArrayList<>();
        initializeSampleData();
    }

    // ... rest of the methods remain the same
    private void initializeSampleData() {
        stockDatabase.put("AAPL", new Stock("AAPL", 150.0, 1000));
        stockDatabase.put("GOOGL", new Stock("GOOGL", 2800.0, 500));
        stockDatabase.put("TSLA", new Stock("TSLA", 700.0, 800));
        stockDatabase.put("AMZN", new Stock("AMZN", 3400.0, 300));
        stockDatabase.put("MSFT", new Stock("MSFT", 300.0, 1200));

        logger.info("Broker " + brokerId + " initialized with sample data on port " + port);
    }

    @Override
    public boolean buyStock(String symbol, int quantity, String clientId) throws RemoteException {
        logger.info(String.format("Broker %s: BUY request - %s x %d from client %s",
                brokerId, symbol, quantity, clientId));

        Stock stock = stockDatabase.get(symbol);
        if (stock == null) {
            logger.warning("Stock not found: " + symbol);
            return false;
        }

        synchronized (stock) {
            if (stock.getAvailableQuantity() < quantity) {
                logger.warning("Insufficient quantity for " + symbol);
                return false;
            }

            double newPrice = stock.getPrice() * (1 + (quantity * 0.001));
            stock.setPrice(newPrice);
            stock.setAvailableQuantity(stock.getAvailableQuantity() - quantity);

            logger.info(String.format("Broker %s: BOUGHT %s x %d at $%.2f for client %s",
                    brokerId, symbol, quantity, newPrice, clientId));

            replicateStockUpdate(symbol, newPrice, stock.getAvailableQuantity());

            return true;
        }
    }

    @Override
    public boolean sellStock(String symbol, int quantity, String clientId) throws RemoteException {
        logger.info(String.format("Broker %s: SELL request - %s x %d from client %s",
                brokerId, symbol, quantity, clientId));

        Stock stock = stockDatabase.get(symbol);
        if (stock == null) {
            stock = new Stock(symbol, 100.0, 0);
            stockDatabase.put(symbol, stock);
        }

        synchronized (stock) {
            double newPrice = stock.getPrice() * (1 - (quantity * 0.0005));
            stock.setPrice(Math.max(newPrice, 1.0));
            stock.setAvailableQuantity(stock.getAvailableQuantity() + quantity);

            logger.info(String.format("Broker %s: SOLD %s x %d at $%.2f for client %s",
                    brokerId, symbol, quantity, newPrice, clientId));

            replicateStockUpdate(symbol, newPrice, stock.getAvailableQuantity());

            return true;
        }
    }

    @Override
    public double getPrice(String symbol) throws RemoteException {
        Stock stock = stockDatabase.get(symbol);
        return stock != null ? stock.getPrice() : 0.0;
    }

    @Override
    public Map<String, Double> getAllStocks() throws RemoteException {
        Map<String, Double> prices = new ConcurrentHashMap<>();
        stockDatabase.forEach((symbol, stock) -> prices.put(symbol, stock.getPrice()));
        return prices;
    }

    @Override
    public void registerBroker(String brokerId, TradingService brokerService) throws RemoteException {
        if (!this.brokerId.equals(brokerId) && !registeredBrokerIds.contains(brokerId)) {
            peerBrokers.add(brokerService);
            registeredBrokerIds.add(brokerId);
            logger.info("Broker " + this.brokerId + " registered peer broker: " + brokerId);

            brokerService.syncStockData(getStockDataForSync());
        }
    }

    @Override
    public void updateStock(String symbol, double newPrice, String sourceBrokerId) throws RemoteException {
        if (this.brokerId.equals(sourceBrokerId)) {
            return;
        }

        Stock stock = stockDatabase.get(symbol);
        if (stock != null) {
            synchronized (stock) {
                stock.setPrice(newPrice);
                logger.info(String.format("Broker %s: Updated %s to $%.2f (from broker %s)",
                        brokerId, symbol, newPrice, sourceBrokerId));
            }
        } else {
            stockDatabase.put(symbol, new Stock(symbol, newPrice, 1000));
            logger.info(String.format("Broker %s: Added new stock %s at $%.2f (from broker %s)",
                    brokerId, symbol, newPrice, sourceBrokerId));
        }
    }

    @Override
    public void syncStockData(Map<String, Double> stockData) throws RemoteException {
        stockData.forEach((symbol, price) -> {
            Stock existing = stockDatabase.get(symbol);
            if (existing == null) {
                stockDatabase.put(symbol, new Stock(symbol, price, 1000));
            } else if (price > existing.getPrice()) {
                existing.setPrice(price);
            }
        });
        logger.info("Broker " + brokerId + " synchronized stock data");
    }

    @Override
    public boolean isAlive() throws RemoteException {
        return true;
    }

    @Override
    public String getBrokerId() throws RemoteException {
        return brokerId;
    }

    private void replicateStockUpdate(String symbol, double newPrice, int quantity) {
        peerBrokers.forEach(broker -> {
            try {
                broker.updateStock(symbol, newPrice, brokerId);
            } catch (RemoteException e) {
                logger.warning("Failed to replicate update to broker: " + e.getMessage());
                peerBrokers.remove(broker);
            }
        });
    }

    private Map<String, Double> getStockDataForSync() {
        Map<String, Double> syncData = new ConcurrentHashMap<>();
        stockDatabase.forEach((symbol, stock) -> syncData.put(symbol, stock.getPrice()));
        return syncData;
    }

    public void printStockStatus() {
        logger.info("=== Broker " + brokerId + " Stock Status ===");
        stockDatabase.forEach((symbol, stock) ->
                logger.info(String.format("  %s: $%.2f (Qty: %d)",
                        symbol, stock.getPrice(), stock.getAvailableQuantity())));
    }
}