<div align="center">

# 🍽️ RestaurantChain DSS - Management System

### *Comprehensive Restaurant Chain Management & Order Processing System*

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Gradle](https://img.shields.io/badge/Gradle-8.x-blue.svg)](https://gradle.org/)
[![MariaDB](https://img.shields.io/badge/MariaDB-11.x-blue.svg)](https://mariadb.org/)
[![License](https://img.shields.io/badge/License-Academic-green.svg)]()
[![Status](https://img.shields.io/badge/Status-Active-success.svg)]()

---

**RestaurantChain DSS** is a sophisticated enterprise-grade management system designed for restaurant chains. Built with **layered architecture principles**, it implements complete order lifecycle management, employee workflows, performance analytics, and real-time communication across multiple restaurant locations.

[Features](#-key-features) • [Installation](#️-installation) • [Usage](#-usage) • [Architecture](#-architecture) • [Authors](#-authors)

---

</div>

## ✨ Key Features

<table>
<tr>
<td width="50%">

### 🍔 Order Management
- Complete order lifecycle from creation to delivery
- Customizable products with ingredient modifications
- Multiple payment methods (Cash, MBWay, Card)
- Order tracking by state (Pending, In Preparation, Ready, Delivered)

</td>
<td width="50%">

### 👥 Multi-Role System
- **Customers**: Place orders with personalized menus
- **Cooks**: Manage kitchen queue and preparation
- **Attendants**: Handle order delivery and customer service
- **Managers**: Analytics, messaging, and oversight

</td>
</tr>
<tr>
<td width="50%">

### 📊 Performance Analytics
- Real-time performance indicators per restaurant
- Date-range filtering for historical analysis
- Revenue tracking and order statistics
- Employee productivity metrics

</td>
<td width="50%">

### 💬 Internal Communication
- Broadcast messaging system for managers
- Targeted messages by role or restaurant
- Message inbox for each employee
- Real-time notification delivery

</td>
</tr>
<tr>
<td colspan="2">

### 🏗️ Enterprise Architecture
- **3-Layer Architecture**: UI, Business Logic, Data Access
- **Facade Pattern**: Simplified subsystem interactions
- **DAO Pattern**: Clean database abstraction with MariaDB
- **MVC Design**: Separation of concerns for maintainability

</td>
</tr>
</table>

---

## 🛠️ Installation

### Prerequisites

- **Java 21** or higher ([Download JDK](https://www.oracle.com/java/technologies/downloads/))
- **Gradle 8.x** (included via wrapper)
- **MariaDB 11.x** ([Installation Guide](https://mariadb.org/download/))
- Git

### Quick Start

#### 1️⃣ **Clone the Repository**

```bash
git clone https://github.com/DelgadoDevT/UM-LEI/DSS-2526.git
cd DSS-2526
```

#### 2️⃣ **Setup Database**

Start your MariaDB service and execute the setup scripts:

```bash
# Linux/Mac
mysql -u root -p < ../mariadb/database_creation.sql
mysql -u root -p < ../mariadb/populate_database.sql

# Windows (using MySQL Client)
mysql -u root -p
SOURCE C:/path/to/mariadb/database_creation.sql;
SOURCE C:/path/to/mariadb/populate_database.sql;
```

Or use the provided database scripts directly:
- `mariadb/database_creation.sql` - Creates database structure
- `mariadb/populate_database.sql` - Populates with sample data
- `mariadb/see_tables.sql` - Query templates for verification

#### 3️⃣ **Configure Database Connection**

Edit the database credentials in `src/main/java/dss/cadeiaRestaurantesDL/DAOConfig.java`:

```java
public class DAOConfig {
    public static final String URL = "jdbc:mariadb://localhost:3306/cadeiaRestaurantes";
    public static final String USERNAME = "funcionario";
    public static final String PASSWORD = "";
}
```

#### 4️⃣ **Build the Project**

```bash
# Linux/Mac/Windows
./gradlew build

# On Windows with PowerShell
.\gradlew.bat build
```

#### 5️⃣ **Generate Documentation (Optional)**

```bash
# Generate Javadoc API documentation
./gradlew javadoc

# Open in browser
xdg-open build/docs/javadoc/index.html  # Linux
open build/docs/javadoc/index.html      # Mac
start build\docs\javadoc\index.html     # Windows
```

---

## 🚀 Usage

### Running the Application

#### Using Gradle (Recommended)

```bash
./gradlew run

# On Windows
.\gradlew.bat run
```

#### Using JAR

```bash
# Build distribution
./gradlew installDist

# Run from scripts
./build/install/DSS-2526/bin/DSS-2526

# Or run JAR directly
java -jar build/libs/DSS-2526-1.0-SNAPSHOT.jar
```

### 🎮 User Workflows

#### **Customer Flow** 🛒
1. Select **"Fazer Pedido"** from main menu
2. Choose restaurant location
3. Browse menu and add items (products or packs)
4. Customize ingredients if desired
5. Select consumption type (dine-in/takeaway)
6. Complete payment (Cash/MBWay/Card)
7. Receive order confirmation

#### **Cook Flow** 👨‍🍳
1. Login with employee ID
2. View kitchen queue
3. Start preparation for pending orders
4. Complete orders and move to ready state
5. Check messages from management

#### **Attendant Flow** 🙋
1. Login with employee ID
2. View ready orders awaiting pickup
3. Register order delivery to customer
4. Check messages and updates

#### **Manager Flow** 💼
1. Login with manager credentials
2. Access analytics dashboard
3. View performance indicators by date range
4. Send broadcast messages to staff
5. Monitor restaurant operations

### 📊 Sample Credentials

The `populate_database.sql` creates sample employees:

| Role | ID | Restaurant |
|------|-----|-----------|
| Manager (General) | 1 | All Locations |
| Manager | 2 | Braga Downtown |
| Cook | 3 | Braga Downtown |
| Attendant | 4 | Braga Downtown |

---

## 🏗️ Architecture

### Key Design Patterns

| Pattern | Implementation | Purpose |
|---------|---------------|---------|
| **Facade** | `CadeiaRestaurantesLNFacade` | Unified interface to subsystems |
| **DAO** | All `*DAO` classes | Abstract database operations |
| **MVC** | View/Controller/Model separation | UI decoupling |
| **Layered** | UI → LN → DL structure | Separation of concerns |

### Domain Model

**Core Entities:**
- **Restaurante**: Restaurant locations with multiple work stations
- **Funcionario**: Employees (Gestor/Cozinheiro/Atendente)
- **Pedido**: Orders with state management
- **Produto/Menu**: Sellable items with ingredients
- **Pagamento**: Payment methods (Dinheiro/MBWay/Cartao_Bancario)
- **IndicadorDesempenho**: Performance metrics

---

## 📂 Project Structure

```
GrupoTP-08/
├── 📄 README.md                          # This file
├── 📄 modelo_de_dominio.vpp             # Visual Paradigm domain model
├── 📄 modelo_de_use_case.vpp            # Visual Paradigm use cases
├── 📄 DSS-Especicacao-Use-Cases.pdf     # Requirements specification
│
├── 📁 mariadb/                           # Database scripts
│   ├── database_creation.sql            # Schema definition
│   ├── populate_database.sql            # Sample data
│   └── see_tables.sql                   # Query examples
│
└── 📁 DSS-2526/                          # Main application
    ├── 📄 build.gradle.kts              # Gradle build configuration
    ├── 📄 gradlew                       # Gradle wrapper (Linux/Mac)
    ├── 📄 gradlew.bat                   # Gradle wrapper (Windows)
    │
    └── 📁 src/main/java/dss/
        ├── 📄 Application.java          # 🚀 Main entry point
        │
        ├── 📁 cadeiaRestaurantesUI/     # Presentation Layer
        │   ├── CadeiaRestaurantesView.java
        │   └── CadeiaRestaurantesController.java
        │
        ├── 📁 cadeiaRestaurantesLN/     # Business Logic Layer
        │   ├── CadeiaRestaurantesLNFacade.java
        │   ├── ICadeiaRestaurantesLN.java
        │   │
        │   ├── 📁 subsistemaRestaurantes/
        │   │   ├── RestaurantesFacade.java
        │   │   ├── Restaurante.java
        │   │   ├── Funcionario.java (Gestor/Cozinheiro/Atendente)
        │   │   └── IndicadorDesempenho.java
        │   │
        │   └── 📁 subsistemaMenuPedidos/
        │       ├── MenuPedidosFacade.java
        │       ├── Pedido.java
        │       ├── LinhaDePedido.java
        │       ├── Produto.java
        │       ├── Menu.java (MenuPack/MenuCategoria)
        │       ├── Ingrediente.java
        │       └── Pagamento.java (Dinheiro/MBWay/Cartao_Bancario)
        │
        └── 📁 cadeiaRestaurantesDL/     # Data Access Layer
            ├── DAOConfig.java           # Database connection config
            ├── RestauranteDAO.java
            ├── FuncionarioDAO.java
            ├── PedidoDAO.java
            ├── ProdutoDAO.java
            ├── MenuDAO.java
            ├── IngredienteDAO.java
            └── IndicadorDAO.java
```

---

## 🧪 Testing

### Compile and Test

```bash
# Run tests
./gradlew test

# View test reports
open build/reports/tests/test/index.html
```

### Manual Testing Checklist

- [ ] Database connection established
- [ ] Customer can create orders
- [ ] Orders appear in cook's queue
- [ ] Order state transitions work correctly
- [ ] Manager can view analytics
- [ ] Messaging system delivers notifications
- [ ] Payment methods process correctly
- [ ] Ingredient customization persists

---

## 🔧 Configuration

### Database Configuration

Edit `src/main/java/dss/cadeiaRestaurantesDL/DAOConfig.java`:

```java
public static final String URL = "jdbc:mariadb://localhost:3306/cadeiaRestaurantes";
public static final String USERNAME = "funcionario";
public static final String PASSWORD = "your_password";
```

### Gradle Configuration

The `build.gradle.kts` configures:
- Java version (21)
- Dependencies (JUnit, MariaDB JDBC)
- Application main class
- Console input handling

---

## 📚 Documentation

### 📖 API Documentation (Javadoc)

The project includes comprehensive API documentation generated from Javadoc comments in the source code.

#### Generate Documentation

```bash
cd DSS-2526
./gradlew javadoc

# On Windows
.\gradlew.bat javadoc
```

#### View Documentation

After generation, open in your browser:

```bash
# Linux
xdg-open build/docs/javadoc/index.html

# Mac
open build/docs/javadoc/index.html

# Windows
start build\docs\javadoc\index.html
```

Or navigate directly to: `DSS-2526/build/docs/javadoc/index.html`

The documentation includes:
- 📦 **All Packages**: Complete package structure (UI, LN, DL layers)
- 🏗️ **Class Hierarchy**: Inheritance and interface relationships
- 📝 **Method Documentation**: Parameters, return types, and descriptions
- 🔍 **Search Functionality**: Quick navigation to any class or method

### Use Case Specification

See `DSS-Especicacao-Use-Cases.pdf` for detailed:
- Actor descriptions
- Use case scenarios
- Business rules
- System requirements

### Visual Models

- **Domain Model**: `modelo_de_dominio.vpp` / `modelo_de_dominio.jpg`
- **Use Case Model**: `modelo_de_use_case.vpp` / `modelo_de_use_case.jpg`

Open `.vpp` files with [Visual Paradigm](https://www.visual-paradigm.com/).

---

## 🐛 Troubleshooting

### Database Connection Failed

```bash
# Check MariaDB is running
sudo systemctl status mariadb  # Linux
brew services list              # Mac
net start MySQL                 # Windows

# Verify credentials
mysql -u funcionario -p cadeiaRestaurantes
```

### Build Fails

```bash
# Clean and rebuild
./gradlew clean build

# Check Java version
java -version  # Should be 21+
```

### Console Input Issues

```bash
# Run with plain console
./gradlew run --console=plain
```

---

## 👥 Authors

**Group 8** - *Software Systems Development Course (DSS 2025/26)*

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
</tr>
</table>

---

## 📄 License

This project is an academic work developed for the **Software Systems Development** course at **University of Minho**. All rights reserved to the authors and the university.

---

## 🙏 Acknowledgments

- **Professor**: For guidance on layered architecture and design patterns
- **MariaDB Foundation**: For the excellent open-source database
- **Gradle Team**: For the powerful build automation tool

---

<div align="center">

**Made with ☕ for the DSS Course**

*University of Minho • Software Engineering • 2025/26*

---

### 📈 Project Statistics

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Lines of Code](https://img.shields.io/badge/Lines%20of%20Code-5000%2B-blue?style=flat-square)
![Classes](https://img.shields.io/badge/Classes-50%2B-green?style=flat-square)
![Database Tables](https://img.shields.io/badge/DB%20Tables-15%2B-red?style=flat-square)

</div>
