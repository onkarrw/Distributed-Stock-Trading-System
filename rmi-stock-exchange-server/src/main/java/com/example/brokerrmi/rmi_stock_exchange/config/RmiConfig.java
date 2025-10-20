// RmiConfig.java
package com.example.brokerrmi.rmi_stock_exchange.config;

import com.example.brokerrmi.rmi_stock_exchange.broker.TradingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

@Configuration
public class RmiConfig {
    private static final Logger logger = Logger.getLogger(RmiConfig.class.getName());

    @Value("${rmi.port:1099}")
    private int rmiPort;

    @Autowired
    private TradingService tradingService;

    @Bean
    public boolean registerRmiService() {
        try {
            Registry registry = LocateRegistry.createRegistry(rmiPort);
            registry.rebind("TradingService", tradingService);
            logger.info("RMI TradingService bound successfully on port: " + rmiPort);
            return true;
        } catch (Exception e) {
            logger.warning("Failed to create RMI registry on port " + rmiPort + ": " + e.getMessage());
            try {
                Registry registry = LocateRegistry.getRegistry(rmiPort);
                registry.rebind("TradingService", tradingService);
                logger.info("RMI TradingService rebound successfully on port: " + rmiPort);
                return true;
            } catch (Exception ex) {
                logger.severe("Failed to bind RMI service: " + ex.getMessage());
                return false;
            }
        }
    }
}