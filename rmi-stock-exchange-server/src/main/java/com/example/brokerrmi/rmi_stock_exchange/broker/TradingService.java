// TradingService.java
package com.example.brokerrmi.rmi_stock_exchange.broker;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface TradingService extends Remote {

    // Client operations
    boolean buyStock(String symbol, int quantity, String clientId) throws RemoteException;
    boolean sellStock(String symbol, int quantity, String clientId) throws RemoteException;
    double getPrice(String symbol) throws RemoteException;
    Map<String, Double> getAllStocks() throws RemoteException;

    // Broker replication operations
    void registerBroker(String brokerId, TradingService brokerService) throws RemoteException;
    void updateStock(String symbol, double newPrice, String sourceBrokerId) throws RemoteException;
    void syncStockData(Map<String, Double> stockData) throws RemoteException;

    // Health check
    boolean isAlive() throws RemoteException;

    String getBrokerId() throws RemoteException;
}