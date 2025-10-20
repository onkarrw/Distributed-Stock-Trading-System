package com.example.client.trade_reqs;

import com.example.client.trade_reqs.service.FaultTolerantTradingClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;
import java.util.Scanner;

@SpringBootApplication
public class TradeReqsApplication implements CommandLineRunner {

	private final FaultTolerantTradingClient client;
	private final Scanner scanner;

	public TradeReqsApplication() {
		this.client = new FaultTolerantTradingClient();
		this.scanner = new Scanner(System.in);
	}

	public static void main(String[] args) {
		SpringApplication.run(TradeReqsApplication.class, args);
	}

	@Override
	public void run(String... args) {
		System.out.println("🚀 Starting Distributed Stock Trading Client...");

		try {
			client.connect();
			runInteractiveMenu();
		} catch (Exception e) {
			System.out.println("❌ Failed to start client: " + e.getMessage());
		} finally {
			scanner.close();
		}
	}


	private void runInteractiveMenu() {
		while (true) {
			System.out.print("\n📊 TRADER > ");
			String input = scanner.nextLine().trim();

			if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
				System.out.println("👋 Thank you for trading! Goodbye!");
				break;
			}

			processCommand(input);
		}
	}

	private void processCommand(String command) {
		String[] parts = command.split("\\s+");
		String action = parts[0].toLowerCase();

		try {
			switch (action) {
				case "price":
				case "p":
					if (parts.length >= 2) {
						String symbol = parts[1].toUpperCase();
						double price = client.getStockPrice(symbol);
						System.out.printf("💰 %s Current Price: $%.2f%n", symbol, price);
					} else {
						System.out.println("❌ Usage: price <SYMBOL> or p <SYMBOL>");
					}
					break;

				case "buy":
				case "b":
					if (parts.length >= 3) {
						String symbol = parts[1].toUpperCase();
						int quantity = Integer.parseInt(parts[2]);
						boolean success = client.executeTrade("buy", symbol, quantity, "trader");
						if (success) {
							System.out.printf("✅ BOUGHT %d shares of %s%n", quantity, symbol);
							double newPrice = client.getStockPrice(symbol);
							System.out.printf("📈 New %s Price: $%.2f%n", symbol, newPrice);
						} else {
							System.out.printf("❌ Failed to buy %s%n", symbol);
						}
					} else {
						System.out.println("❌ Usage: buy <SYMBOL> <QUANTITY> or b <SYMBOL> <QUANTITY>");
					}
					break;

				case "sell":
				case "s":
					if (parts.length >= 3) {
						String symbol = parts[1].toUpperCase();
						int quantity = Integer.parseInt(parts[2]);
						boolean success = client.executeTrade("sell", symbol, quantity, "trader");
						if (success) {
							System.out.printf("✅ SOLD %d shares of %s%n", quantity, symbol);
							double newPrice = client.getStockPrice(symbol);
							System.out.printf("📉 New %s Price: $%.2f%n", symbol, newPrice);
						} else {
							System.out.printf("❌ Failed to sell %s%n", symbol);
						}
					} else {
						System.out.println("❌ Usage: sell <SYMBOL> <QUANTITY> or s <SYMBOL> <QUANTITY>");
					}
					break;

				case "list":
				case "l":
				case "stocks":
					Map<String, Double> allStocks = client.getAllStocks();
					System.out.println("\n📋 Available Stocks:");
					System.out.println("┌────────────┬─────────────┐");
					System.out.println("│ Symbol     │ Price       │");
					System.out.println("├────────────┼─────────────┤");
					allStocks.forEach((symbol, price) ->
							System.out.printf("│ %-10s │ $%-10.2f │%n", symbol, price));
					System.out.println("└────────────┴─────────────┘");
					break;

				case "status":
					System.out.println("🟢 Client is connected and ready");
					System.out.println("📡 Broker network: 3 nodes available");
					break;

				case "help":
				case "h":
				case "?":
					showHelp();
					break;

				case "bulk":
					if (parts.length >= 4) {
						String operation = parts[1].toLowerCase();
						String symbol = parts[2].toUpperCase();
						int quantity = Integer.parseInt(parts[3]);
						int times = parts.length >= 5 ? Integer.parseInt(parts[4]) : 5;

						System.out.printf("🔄 Executing %d %s operations for %s...%n", times, operation, symbol);
						for (int i = 1; i <= times; i++) {
							boolean success = client.executeTrade(operation, symbol, quantity, "bulk-trader");
							System.out.printf("   %d/%d: %s %d %s - %s%n",
									i, times, operation.toUpperCase(), quantity, symbol,
									success ? "✅" : "❌");
							Thread.sleep(500); // Small delay between operations
						}
					} else {
						System.out.println("❌ Usage: bulk <buy/sell> <SYMBOL> <QUANTITY> [TIMES]");
					}
					break;

				default:
					System.out.println("❌ Unknown command. Type 'help' for available commands.");
			}
		} catch (NumberFormatException e) {
			System.out.println("❌ Invalid number format. Please check your input.");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			System.out.println("❌ Operation interrupted.");
		} catch (Exception e) {
			System.out.println("❌ Error: " + e.getMessage());
		}
	}

	private void showHelp() {
		System.out.println("\n📖 Available Commands:");
		System.out.println("┌───────────────────┬─────────────────────────────────────────────┐");
		System.out.println("│ Command           │ Description                                 │");
		System.out.println("├───────────────────┼─────────────────────────────────────────────┤");
		System.out.println("│ price <SYMBOL>    │ Get current stock price                     │");
		System.out.println("│ buy <SYM> <QTY>   │ Buy shares of a stock                       │");
		System.out.println("│ sell <SYM> <QTY>  │ Sell shares of a stock                      │");
		System.out.println("│ list              │ Show all available stocks                   │");
		System.out.println("│ status            │ Show client connection status               │");
		System.out.println("│ bulk <op> <s> <q> │ Execute multiple trades (op=buy/sell)       │");
		System.out.println("│ help              │ Show this help message                      │");
		System.out.println("│ exit              │ Exit the application                        │");
		System.out.println("└───────────────────┴─────────────────────────────────────────────┘");
		System.out.println("\n💡 Shortcuts: p (price), b (buy), s (sell), l (list), h (help)");
		System.out.println("💡 Example: buy AAPL 10, price TSLA, bulk buy GOOGL 5 3");
	}
}