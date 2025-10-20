// StockTradingApplication.java
package com.example.brokerrmi.rmi_stock_exchange;

import com.example.brokerrmi.rmi_stock_exchange.broker.TradingService;
import com.example.brokerrmi.rmi_stock_exchange.broker.TradingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.logging.Logger;

@SpringBootApplication
public class RmiStockExchangeApplication implements CommandLineRunner {
	private static final Logger logger = Logger.getLogger(RmiStockExchangeApplication.class.getName());

	@Autowired
	private TradingService tradingService;

	@Autowired
	private TradingServiceImpl tradingServiceImpl;

	public static void main(String[] args) {
		SpringApplication.run(RmiStockExchangeApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// Connect to other brokers if specified
		if (args.length > 0) {
			for (String arg : args) {
				if (arg.startsWith("connect:")) {
					String[] parts = arg.split(":");
					if (parts.length == 3) {
						String host = parts[1];
						int port = Integer.parseInt(parts[2]);
						connectToBroker(host, port);
					}
				}
			}
		}

		// Print initial status
		tradingServiceImpl.printStockStatus();

		// Keep the application running
		logger.info("Broker server is running. Press 'q' to quit, 's' for status.");

		Scanner scanner = new Scanner(System.in);
		while (scanner.hasNextLine()) {
			String input = scanner.nextLine().trim();
			if ("q".equalsIgnoreCase(input)) {
				break;
			} else if ("s".equalsIgnoreCase(input)) {
				tradingServiceImpl.printStockStatus();
			} else if (input.startsWith("connect:")) {
				String[] parts = input.split(":");
				if (parts.length == 3) {
					String host = parts[1];
					int port = Integer.parseInt(parts[2]);
					connectToBroker(host, port);
				}
			}
		}

		scanner.close();
		System.exit(0);
	}

	private void connectToBroker(String host, int port) {
		try {
			Registry registry = LocateRegistry.getRegistry(host, port);
			TradingService peerService = (TradingService) registry.lookup("TradingService");

			String myBrokerId = tradingService.getBrokerId();
			peerService.registerBroker(myBrokerId, tradingService);

			logger.info("Connected to broker at " + host + ":" + port);
		} catch (Exception e) {
			logger.warning("Failed to connect to broker at " + host + ":" + port + " - " + e.getMessage());
		}
	}
}