package com.example.brokerrmi.rmi_stock_exchange;

import com.example.brokerrmi.rmi_stock_exchange.broker.TradingService;
import com.example.brokerrmi.rmi_stock_exchange.broker.TradingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Logger;

@SpringBootApplication
public class RmiStockExchangeApplication implements CommandLineRunner {
	private static final Logger logger = Logger.getLogger(RmiStockExchangeApplication.class.getName());

	@Autowired
	private TradingService tradingService;

	public static void main(String[] args) {
		SpringApplication.run(RmiStockExchangeApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		logger.info("Processing command line args: " + Arrays.toString(args));

		// Connect to other brokers if specified
		for (String arg : args) {
			if (arg.startsWith("--connect=")) {
				String address = arg.substring(10); // Remove "--connect="
				String[] parts = address.split(":");
				if (parts.length == 2) {
					String host = parts[0];
					int port = Integer.parseInt(parts[1]);
					connectToBroker(host, port);
				}
			}
		}

		// Print initial status
		TradingServiceImpl impl = (TradingServiceImpl) tradingService;
		impl.printStockStatus();

		// Interactive menu
		logger.info("Broker server is running. Press 'q' to quit, 's' for status, 'c' to connect to another broker.");

		Scanner scanner = new Scanner(System.in);
		while (scanner.hasNextLine()) {
			String input = scanner.nextLine().trim();
			if ("q".equalsIgnoreCase(input)) {
				break;
			} else if ("s".equalsIgnoreCase(input)) {
				impl.printStockStatus();
			} else if (input.startsWith("c ")) {
				// Manual connection: c localhost:1100
				String[] parts = input.split(" ");
				if (parts.length == 2) {
					String[] addressParts = parts[1].split(":");
					if (addressParts.length == 2) {
						connectToBroker(addressParts[0], Integer.parseInt(addressParts[1]));
					}
				}
			} else if ("peers".equalsIgnoreCase(input)) {
				// Debug: show connected peers
				showConnectedPeers(impl);
			}
		}

		scanner.close();
		System.exit(0);
	}

	private void connectToBroker(String host, int port) {
		try {
			logger.info("Attempting to connect to broker at " + host + ":" + port);
			Registry registry = LocateRegistry.getRegistry(host, port);
			TradingService peerService = (TradingService) registry.lookup("TradingService");

			String myBrokerId = tradingService.getBrokerId();
			peerService.registerBroker(myBrokerId, tradingService);

			logger.info("✅ Successfully connected to broker at " + host + ":" + port);
		} catch (Exception e) {
			logger.warning("❌ Failed to connect to broker at " + host + ":" + port + " - " + e.getMessage());
		}
	}

	private void showConnectedPeers(TradingServiceImpl impl) {
		try {
			// Using reflection to access peerBrokers for debugging
			java.lang.reflect.Field peerBrokersField = TradingServiceImpl.class.getDeclaredField("peerBrokers");
			peerBrokersField.setAccessible(true);
			java.util.List<?> peerBrokers = (java.util.List<?>) peerBrokersField.get(impl);

			java.lang.reflect.Field registeredBrokerIdsField = TradingServiceImpl.class.getDeclaredField("registeredBrokerIds");
			registeredBrokerIdsField.setAccessible(true);
			java.util.List<?> registeredBrokerIds = (java.util.List<?>) registeredBrokerIdsField.get(impl);

			logger.info("=== Connected Peers ===");
			logger.info("Peer brokers count: " + peerBrokers.size());
			logger.info("Registered broker IDs: " + registeredBrokerIds);
		} catch (Exception e) {
			logger.warning("Could not access peer information: " + e.getMessage());
		}
	}
}