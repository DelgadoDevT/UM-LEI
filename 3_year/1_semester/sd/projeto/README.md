<div align="center">

# 📊 SD-2526 - Distributed Sales Management System

### *High-Performance Time Series Data Management with LRU Caching & Real-Time Notifications*

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Gradle](https://img.shields.io/badge/Gradle-8.14-blue.svg)](https://gradle.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Status](https://img.shields.io/badge/Status-Active-success.svg)]()

---

**SD-2526** is a sophisticated distributed system for managing sales data across time series with advanced caching mechanisms, real-time notifications, and multi-threaded client-server architecture. Built for high performance and scalability, it implements **LRU cache eviction**, **concurrent data access**, and **event-driven notifications**.

[Features](#-key-features) • [Installation](#️-installation) • [Usage](#-usage) • [Architecture](#-architecture) • [Authors](#-authors)

---

</div>

## ✨ Key Features

<table>
<tr>
<td width="50%">

### 📈 Time Series Management
- Daily sales data aggregation by product
- Temporal simulation with date advancement
- Persistent storage with automatic save/load
- Efficient serialization with full and filtered modes

</td>
<td width="50%">

### 🚀 High-Performance Caching
- **LRU (Least Recently Used)** eviction policy
- Configurable cache size (S parameter)
- Automatic persistence of modified series
- Separate aggregation cache (S × 100 entries)

</td>
</tr>
<tr>
<td width="50%">

### 🔔 Real-Time Notifications
- **Simultaneous Sales**: Block until products sold together
- **Consecutive Sales**: Wait for N sequential product sales
- Thread-safe condition variables
- End-of-day epoch handling

</td>
<td width="50%">

### 👥 Multi-User System
- Secure user authentication and registration
- Persistent user credentials storage
- Thread-safe concurrent access
- Read-write lock optimization

</td>
</tr>
<tr>
<td width="50%">

### 📊 Data Aggregation
- **Total Quantity**: Sum of all units sold
- **Total Volume**: Sum of all sales values
- **Average Sale**: Mean value per transaction
- **Maximum Sale**: Highest single transaction
- Date-range queries with caching

</td>
<td width="50%">

### 🧵 Concurrent Architecture
- Multi-threaded server with worker threads
- Tagged connection protocol (Demultiplexer)
- Thread-safe data structures with ReentrantLocks
- Graceful shutdown with data persistence

</td>
</tr>
</table>

---

## 🏗️ Architecture

### System Design

The project follows a **modular multi-tier architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────────────────┐
│                   CLIENT LAYER                      │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────┐  │
│  │ UserInterface│  │ SalesClient  │  │StressTest │  │
│  └──────────────┘  └──────────────┘  └───────────┘  │
└─────────────────────────────────────────────────────┘
                         ▼ TCP/IP
┌─────────────────────────────────────────────────────┐
│                  MIDDLEWARE LAYER                   │
│  ┌──────────────────┐  ┌──────────────────────────┐ │
│  │ Demultiplexer    │  │  TaggedConnection        │ │
│  │ (Message Router) │  │  (Protocol Handler)      │ │
│  └──────────────────┘  └──────────────────────────┘ │
└─────────────────────────────────────────────────────┘
                         ▼
┌─────────────────────────────────────────────────────┐
│                   SERVER LAYER                      │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────┐  │
│  │ ServerWorker │  │ ServerCache  │  │UserManager│  │
│  └──────────────┘  └──────────────┘  └───────────┘  │
│  ┌──────────────┐  ┌──────────────────────────────┐ │
│  │Notification  │  │   SeriesFileManager          │ │
│  │Manager       │  │   (Persistence Layer)        │ │
│  └──────────────┘  └──────────────────────────────┘ │
└─────────────────────────────────────────────────────┘
                         ▼
┌─────────────────────────────────────────────────────┐
│                  STORAGE LAYER                      │
│       data/users.dat  |  data/timeseries/*.dat      │
└─────────────────────────────────────────────────────┘
```

### Key Components

| Component | Module | Responsibility |
|-----------|--------|----------------|
| **UserInterface** | Client | Interactive CLI for user operations |
| **SalesClient** | Client | API wrapper for server communication |
| **StressTestClient** | Client | Performance benchmarking tool |
| **Demultiplexer** | Middleware | Asynchronous message routing by tag |
| **TaggedConnection** | Middleware | Protocol for sending/receiving tagged frames |
| **ServerMain** | Server | Server initialization and lifecycle management |
| **ServerWorker** | Server | Handles individual client connections |
| **ServerCache** | Server | LRU cache with automatic persistence |
| **TimeSeries** | Server | Daily sales data aggregation container |
| **NotificationManager** | Server | Event-driven notification system |
| **UserManager** | Server | Authentication and user management |
| **SeriesFileManager** | Server | Binary file persistence for time series |

### Design Patterns

| Pattern | Implementation | Purpose |
|---------|---------------|---------|
| **Multi-Threading** | ServerWorker threads | Concurrent client handling |
| **Producer-Consumer** | Demultiplexer queues | Asynchronous message processing |
| **LRU Cache** | ServerCache (LinkedHashMap) | Memory-efficient data management |
| **Read-Write Lock** | ReentrantReadWriteLock | Optimized concurrent access |
| **Observer** | NotificationManager | Event-driven notification delivery |
| **Strategy** | Aggregation methods | Pluggable computation algorithms |

---

## 🛠️ Installation

### Prerequisites

- **Java 21** or higher ([Download JDK](https://www.oracle.com/java/technologies/downloads/))
- **Gradle 8.14** (included via wrapper)
- Git

### Quick Start

#### 1️⃣ **Clone the Repository**

```bash
git clone https://github.com/DelgadoDevT/UM-LEI/SD-2526.git
cd SD-2526
```

#### 2️⃣ **Build the Project**

```bash
# Linux/Mac
./gradlew build

# Windows
.\gradlew.bat build
```

This will compile all modules:
- `:common` - Shared protocol definitions (Tag constants)
- `:middleware` - Communication layer (Demultiplexer, TaggedConnection)
- `:server` - Server implementation with caching and persistence
- `:client` - Client interfaces and utilities

---

## 🚀 Usage

### Starting the Server

The server requires a **cache size (S)** parameter and optional **port** configuration:

```bash
# Using Gradle
./gradlew :server:run --args="100 12345"

# Or build and run directly
./gradlew :server:installDist
./server/build/install/server/bin/server 100 12345
```

**Parameters:**
- `<cache_size_S>`: Maximum number of TimeSeries objects in memory (required)
- `[port]`: Server listening port (optional, default: 12345)

**Example Output:**
```
Servidor a iniciar na porta 12345...
Use Ctrl+C para encerrar
Cache size (S): 100
Data inicial do sistema: 2025-12-27
Dias históricos disponíveis (D): 5
Servidor pronto para aceitar clientes!
```

### Running the Client

#### Interactive CLI Client

```bash
# Using Gradle
./gradlew :client:run --args="localhost 12345"

# Or build and run
./gradlew :client:installDist
./client/build/install/client/bin/client localhost 12345
```

**Default connection:** `localhost:12345`

#### Client Menu Options

```
  ╔═══════════════════════════════════════╗
  ║                                       ║
  ║    _____ ____        ____ _           ║
  ║   / ____|  _ \      / ___| |          ║
  ║   \___ \| | | |____| |   | |          ║
  ║    ___) | |_| |____| |___| |___       ║
  ║   |____/|____/      \____|_____|      ║
  ║                                       ║
  ║        Sales System v1.0              ║
  ║                                       ║
  ╚═══════════════════════════════════════╝

═══════════════════════════════════════════
  🔐 AUTHENTICATION
     1. Register
     2. Login
═══════════════════════════════════════════
  📝 EVENTS
     3. Record Sale (Today)
     4. New Day (Advance Time)
═══════════════════════════════════════════
  📊 STATISTICS
     5. Query Aggregations
     6. Filter Events (Compact)
═══════════════════════════════════════════
  🔔 NOTIFICATIONS
     7. Simultaneous Sales
     8. Consecutive Sales
═══════════════════════════════════════════
     0. EXIT
═══════════════════════════════════════════
```

### 🎮 User Workflows

#### **New User Registration** 📝
1. Select **"1"** - Register
2. Enter desired username
3. Enter password
4. System confirms registration

#### **Recording Sales** 🛒
1. Login with credentials (option 2)
2. Select **"3"** - Record Sale (Today)
3. Enter product name (e.g., "Laptop")
4. Enter quantity (e.g., 5)
5. Enter unit value (e.g., 1200.50)
6. System confirms sale recorded for current day

#### **Querying Aggregations** 📊
1. Login to the system
2. Select **"5"** - Query Aggregations
3. Choose aggregation type (Quantity/Volume/Average/Maximum)
4. Enter product name
5. Enter number of days to analyze
6. View computed result

**Example:**
```
📊 Query Aggregations
   📦 Product: Laptop
   📅 Days: 7
   ✓ Total Quantity: 342 units
```

#### **Notification Subscriptions** 🔔

**Simultaneous Sales:**
1. Select **"7"** - Simultaneous Sales
2. Enter first product name (e.g., "Laptop")
3. Enter second product name (e.g., "Mouse")
4. Thread blocks until both products sold today
5. System notifies when condition met

**Consecutive Sales:**
1. Select **"8"** - Consecutive Sales
2. Enter product name
3. Enter threshold (N consecutive sales)
4. Thread blocks until N consecutive sales occur
5. System notifies when condition met

#### **Time Simulation** 🌅
1. Select **"4"** - New Day (Advance Time)
2. System increments date
3. Current day becomes historical
4. Notifications reset for new day
5. Modified data automatically persisted

### Stress Testing

Run performance benchmarks to evaluate cache efficiency:

```bash
# Run stress test
./gradlew :client:run --console=plain --args="--stress localhost 12345"
```

Or programmatically via `StressTestClient`:
- **Throughput Test**: 100,000 concurrent operations
- **Cache Performance**: Miss (disk) vs Hit (RAM) latency
- **Persistence Test**: 50 cycles × 20,000 events/day

---

## 📂 Project Structure

```
SD-2526/
├── 📄 README.md                          # This file
├── 📄 LICENSE                            # MIT License
├── 📄 SD_2526.pdf                        # Project specification
├── 📄 settings.gradle.kts                # Multi-module configuration
├── 📄 build.gradle.kts                   # Root build configuration
├── 📄 gradlew / gradlew.bat             # Gradle wrapper scripts
│
├── 📁 gradle/wrapper/                    # Gradle wrapper files
│
├── 📁 common/                            # Shared protocol definitions
│   ├── build.gradle.kts
│   └── src/main/java/sd/common/
│       └── Tag.java                     # Message tag constants
│
├── 📁 middleware/                        # Communication layer
│   ├── build.gradle.kts
│   └── src/main/java/sd/middleware/
│       ├── TaggedConnection.java        # Socket wrapper with tagging
│       └── Demultiplexer.java           # Async message router
│
├── 📁 server/                            # Server implementation
│   ├── build.gradle.kts
│   ├── data/                            # Persistent storage (generated)
│   │   ├── users.dat                   # User credentials
│   │   └── timeseries/                 # Daily sales data files
│   │       └── series_YYYY-MM-DD.dat
│   └── src/main/java/sd/
│       ├── server/
│       │   ├── ServerMain.java         # 🚀 Server entry point
│       │   ├── ServerWorker.java       # Client handler thread
│       │   ├── ServerState.java        # Shared server state
│       │   ├── ServerCache.java        # LRU cache implementation
│       │   ├── UserManager.java        # Authentication system
│       │   └── NotificationManager.java # Event notifications
│       └── series/
│           ├── TimeSeries.java         # Daily aggregation container
│           ├── ProductEvent.java       # Product-level events
│           ├── SalesEvent.java         # Individual sale record
│           └── SeriesFileManager.java  # File persistence
│
└── 📁 client/                            # Client implementations
    ├── build.gradle.kts
    └── src/main/java/sd/client/
        ├── UserInterface.java          # 🖥️ Interactive CLI
        ├── SalesClient.java            # API wrapper
        └── StressTestClient.java       # Performance benchmarks
```

---

## 🔧 Configuration

### Server Configuration

Edit cache size and port in server startup:

```bash
# Small cache (good for testing eviction)
./gradlew :server:run --args="10 12345"

# Large cache (better performance)
./gradlew :server:run --args="1000 12345"

# Custom port
./gradlew :server:run --args="100 8080"
```

### Client Configuration

Connect to different servers:

```bash
# Local server
./gradlew :client:run --args="localhost 12345"

# Remote server
./gradlew :client:run --args="192.168.1.100 8080"
```

### Gradle Configuration

The `build.gradle.kts` configures:
- **Java Version**: 21 (toolchain)
- **Test Framework**: JUnit 5 (Jupiter)
- **Application Plugins**: For executable scripts
- **Standard Input**: Enabled for interactive CLI
- **Javadoc**: Configured to suppress documentation warnings

---

## 📊 Data Formats

### User Data File (`data/users.dat`)

Binary format:
```
[4 bytes] - Number of users (int)
For each user:
  [variable] - Username (UTF-8 string)
  [variable] - Password (UTF-8 string)
```

### Time Series File (`data/timeseries/series_YYYY-MM-DD.dat`)

Binary format:
```
[4 bytes] - Year (int)
[4 bytes] - Month (int)
[4 bytes] - Day (int)
[4 bytes] - Number of products (int)
For each product:
  [variable] - Product name (UTF-8)
  [4 bytes]  - Number of events (int)
  For each event:
    [variable] - Product name (UTF-8)
    [4 bytes]  - Quantity (int)
    [8 bytes]  - Value (double)
    [8 bytes]  - Timestamp (long, epoch millis)
```

### Network Protocol

Tagged message format:
```
[4 bytes] - Tag (int) - Operation identifier
[4 bytes] - Length (int) - Payload size
[N bytes] - Payload - Operation-specific data
```

**Tag Constants** (from `Tag.java`):
- `1` - REGISTER
- `2` - LOGIN
- `3` - ADD_EVENT
- `4` - AG_QUANTITY (Total quantity aggregation)
- `5` - AG_VOLUME (Total volume aggregation)
- `6` - AG_AVG (Average aggregation)
- `7` - AG_MAX (Maximum aggregation)
- `8` - SIMUL_SALES (Simultaneous sales notification)
- `9` - CONSEC_SALES (Consecutive sales notification)
- `10` - NEW_DAY (Advance day)
- `11` - FILTER_EVENTS (Query filtered events)

---

## 🐛 Troubleshooting

### Server Won't Start

```bash
# Check if port is already in use
lsof -i :12345  # Linux/Mac
netstat -ano | findstr :12345  # Windows

# Use different port
./gradlew :server:run --args="100 8080"
```

### Client Connection Failed

```bash
# Verify server is running
telnet localhost 12345

# Check firewall settings
sudo ufw allow 12345  # Linux

# Try localhost vs 127.0.0.1
./gradlew :client:run --args="127.0.0.1 12345"
```

### Build Fails

```bash
# Clean and rebuild
./gradlew clean build

# Check Java version
java -version  # Should be 21+

# Update wrapper
./gradlew wrapper --gradle-version=8.14
```

### Data Corruption

```bash
# Clear data directory
./gradlew :server:cleanData

# Or manually
rm -rf server/data/*

# Server will recreate structure on next start
```

### Console Input Issues

```bash
# Run with plain console
./gradlew :client:run --console=plain

# Or run from built script
./client/build/install/client/bin/client
```

---

## 📚 Documentation

### Project Specification

See `SD_2526.pdf` for detailed:
- System requirements
- Architecture specifications
- Performance requirements
- Evaluation criteria

### Code Documentation

All classes include comprehensive Javadoc:
- Purpose and responsibility
- Thread-safety guarantees
- Method parameters and return values
- Exception handling
- Usage examples

Generate Javadoc:
```bash
./gradlew javadoc
open build/docs/javadoc/index.html
```

---

## 🔒 Security Considerations

- **Authentication**: Username/password stored in plaintext (educational project)
- **Network**: Unencrypted TCP (not production-ready)
- **Validation**: Minimal input validation
- **Concurrency**: Thread-safe with proper locking
- **Persistence**: Data integrity via write locks during save

⚠️ **Note**: This is an academic project. Do not use in production without implementing proper security measures (TLS, password hashing, input sanitization, etc.).

---

## 🚀 Performance Tips

### Server Optimization

- **Cache Size**: Set `S` to balance memory usage vs disk I/O
  - Too small: Frequent evictions, high disk I/O
  - Too large: High memory usage, infrequent evictions
  - Rule of thumb: `S = D/2` where D = total historical days

- **JVM Options**: Increase heap for large caches
  ```bash
  export JAVA_OPTS="-Xmx4g -Xms2g"
  ./gradlew :server:run --args="1000"
  ```

### Client Optimization

- **Connection Pooling**: Reuse SalesClient instances
- **Batch Operations**: Group multiple sales before sending
- **Async Operations**: Use threads for concurrent queries

---

## 👥 Authors

**Grupo 1** - *Distributed Systems Course (SD 2025/26)*

<table>
<tr>
<td align="center">
<a href="https://github.com/DelgadoDevT">
<img src="https://github.com/DelgadoDevT.png" width="100px;" alt="DelgadoDevT"/><br />
<sub><b>João Pedro Delgado Teixeira</b></sub><br />
<sub>A106836</sub>
</a>
</td>
<td align="center">
<a href="https://github.com/PaoComPlanta">
<img src="https://github.com/PaoComPlanta.png" width="100px;" alt="PaoComPlanta"/><br />
<sub><b>Simão Pedro Pacheco Mendes</b></sub><br />
<sub>A106928</sub>
</a>
</td>
<td align="center">
<a href="https://github.com/SirLordNelson">
<img src="https://github.com/SirLordNelson.png" width="100px;" alt="SirLordNelson"/><br />
<sub><b>Nelson Manuel Rocha Mendes</b></sub><br />
<sub>A106884</sub>
</a>
</td>
<td align="center">
<a href="https://github.com/M4chad0">
<img src="https://github.com/M4chad0.png" width="100px;" alt="M4chad0"/><br />
<sub><b>Tomás Furtado Botelho Machado</b></sub><br />
<sub>A104186</sub>
</a>
</td>
</tr>
</table>

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

Copyright (c) 2025 João Delgado, Simão Mendes, Tomás Machado, Nelson Mendes

---

## 🙏 Acknowledgments

- **Professor**: For guidance on distributed systems design and concurrent programming
- **University of Minho**: For providing the educational framework
- **Java Community**: For excellent concurrency utilities and documentation
- **Gradle Team**: For the powerful build automation tool

---

<div align="center">

**Made with ☕ for the Distributed Systems Course**

*University of Minho • Software Engineering • 2025/26*

---

### 📈 Project Statistics

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Lines of Code](https://img.shields.io/badge/Lines%20of%20Code-3300%2B-blue?style=flat-square)
![Classes](https://img.shields.io/badge/Classes-16-green?style=flat-square)
![Modules](https://img.shields.io/badge/Modules-4-red?style=flat-square)
![Gradle](https://img.shields.io/badge/Gradle-8.14-blue?style=flat-square)

</div>

