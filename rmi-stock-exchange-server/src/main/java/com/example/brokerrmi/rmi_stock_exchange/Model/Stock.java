// Stock.java
package com.example.brokerrmi.rmi_stock_exchange.Model;

public class Stock {
    private String symbol;
    private double price;
    private int availableQuantity;

    public Stock(String symbol, double price, int availableQuantity) {
        this.symbol = symbol;
        this.price = price;
        this.availableQuantity = availableQuantity;
    }

    // Getters and setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    @Override
    public String toString() {
        return String.format("Stock{symbol='%s', price=%.2f, quantity=%d}",
                symbol, price, availableQuantity);
    }
}