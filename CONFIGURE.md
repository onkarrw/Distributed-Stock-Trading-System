# RMI Stock Exchange — Full Multi-Broker & Client Setup

This README provides **all commands in one place** so you can copy and run them directly to start **3 broker servers** and **3 trading clients**.

---

## ⚙️ Prerequisites

- Built JARs:
    - `rmi-stock-exchange-0.0.1.jar` (Broker/Exchange server)
    - `trade-reqs-0.0.1-SNAPSHOT.jar` (Trading client)

---

## 🚀 START ALL BROKER SERVERS

> Open **3 terminals** and run the following commands:

### 🧩 Terminal 1 — Broker 1
```bash
java -jar rmi-stock-exchange-0.0.1.jar \
  --server.port=8080 \
  --broker.id=broker1 \
  --rmi.port=1099
```

### 🧩 Terminal 2 — Broker 2
```bash
java -jar rmi-stock-exchange-0.0.1.jar \
  --server.port=8081 \
  --broker.id=broker2 \
  --rmi.port=1100
```

### 🧩 Terminal 3 — Broker 3
```bash
java -jar rmi-stock-exchange-0.0.1.jar \
  --server.port=8082 \
  --broker.id=broker3 \
  --rmi.port=1101
```













## 🚀 START MULTIPLE CLIENTS

> Open **n terminals** and run the following commands:

### 🧩 Terminal 1 — Broker 1
```bash
java -jar trade-reqs-0.0.1-SNAPSHOT.jar \
  --server.port=8083
```

### 🧩 Terminal 2 — Broker 2
```bash
java -jar trade-reqs-0.0.1-SNAPSHOT.jar \
  --server.port=8083
```

### 🧩 Terminal 3 — Broker 3
```bash
java -jar trade-reqs-0.0.1-SNAPSHOT.jar \
  --server.port=8084
```