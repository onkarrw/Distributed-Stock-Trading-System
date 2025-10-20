# Distributed Stock Trading System

## 📝 Project Overview
The **Distributed Stock Trading System** simulates a multi-broker stock trading platform using **Java RMI**. Multiple broker servers maintain synchronized stock data and handle client buy/sell requests in real time. The system ensures **data consistency**, **fault tolerance**, and **replication across brokers**, providing a realistic simulation of distributed trading architectures.

---

## 💡 Features
- Multiple broker servers with **in-memory stock data** (`ConcurrentHashMap`)
- Clients can connect to **any broker** to execute trades
- **Replication** of stock updates across brokers via RMI callbacks
- Supports **multiple clients and brokers simultaneously**
- Fault tolerance: clients can reconnect to other brokers if one fails
- Console-based logs for monitoring trades and updates
- Optional integration with **Spring Boot** for simplified RMI service export

---

## 🧱 System Components

| Component | Role |
|-----------|------|
| `TradingService` (interface) | Defines methods: `buyStock`, `sellStock`, `getPrice`, and `updateStock` for replication |
| `BrokerServer` | Implements `TradingService`, manages local trades, and replicates updates to peer brokers |
| `TradingClient` | Connects to a broker and invokes trading operations |
| RMI Registry | Stores broker references for discovery by clients |
| Optional Logger / DB | Logs trades for auditing and debugging |

---

## ⚙️ Flow of a Trade
1. Client calls `buyStock(symbol, qty)` on Broker A
2. Broker A updates its local stock price
3. Broker A invokes `updateStock(symbol, newPrice)` on all peer brokers
4. Each peer broker updates its local stock data
5. Broker A returns confirmation to the client

---

## 🎯 Learning Outcomes
- Understand **Java RMI** and remote method invocation
- Learn **distributed replication** and **data consistency**
- Handle **multi-client, multi-server concurrency**
- Simulate **real-world broker-client architectures**
- Learn **fault tolerance** strategies in distributed systems
- Optional: Learn Spring Boot RMI integration for service export

---