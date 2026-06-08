<div align="center">

# 🏪 Taki — Franchise Management System

### *Distributed platform for a convenience store network*

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Javalin](https://img.shields.io/badge/Javalin-6.x-blue.svg)](https://javalin.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791.svg)](https://www.postgresql.org/)
[![Vite](https://img.shields.io/badge/Vite-5.x-646CFF.svg)](https://vitejs.dev/)
[![Docker](https://img.shields.io/badge/Docker-Compose%20v2-2496ED.svg)](https://docs.docker.com/compose/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Status](https://img.shields.io/badge/Status-Active-success.svg)]()

---

**Taki** is an integrated management system for a convenience store network. It ensures **operational autonomy** in each store (POS, inventory, returns) and **synchronization** with the headquarters central server through a **Hub-and-Spoke** architecture over JWT RS256 and PostgreSQL.

[Features](#-features) • [Architecture](#%EF%B8%8F-architecture) • [Installation](#%EF%B8%8F-installation) • [Usage](#-usage) • [Documentation](#-documentation) • [Authors](#-authors)

---

</div>

## ✨ Features

<table>
<tr>
<td width="50%">

### 🏬 Autonomous Local Operation
- Point of Sale (POS) with barcode scanner
- Stock, return, and promotion management per store
- Continues to operate even without connection to headquarters

</td>
<td width="50%">

### 🌐 Central Network Management
- Central catalog propagated to all branches
- Aggregated KPIs, PDF reports, and **SAF-T** export
- Global CRUD for stores, profiles, and employees

</td>
</tr>
<tr>
<td width="50%">

### 🔄 Resilient Synchronization
- *Outbox Pattern* in DAOs for *at-least-once* delivery
- Idempotency by UUID — no duplicates after reconnection
- Redundant headquarters endpoints via `URLS_SEDE_CSV`

</td>
<td width="50%">

### 🔐 Distributed Authentication
- **RS256** JWT signed with shared RSA key
- **HttpOnly** `auth_token` Cookie with `SameSite`
- RBAC: `GESTOR_CENTRAL`, `GESTOR_LOJA`, `OPERADOR_CAIXA`

</td>
</tr>
<tr>
<td width="50%">

### 🐳 Docker Orchestration
- 3 profiles: **CENTRAL**, **BRANCH**, and **DEV**
- PostgreSQL, Javalin, and Vite in isolated containers
- Reproducible startup on any machine

</td>
<td width="50%">

### 📊 Real-Time Dashboards
- Sales KPIs, average ticket, and volume per store
- Sales by hour, month, and category
- Inventory reports and duplicate invoice (PDF)

</td>
</tr>
<tr>
<td colspan="2">

### 🧪 Test Coverage & Documentation
- Manual test battery for Local API, Global API, and UI ([`docs/MANUAL_TEST_SCRIPT.md`](docs/MANUAL_TEST_SCRIPT.md))
- Complete production installation manual ([`docs/MANUAL_INSTALACAO.md`](docs/MANUAL_INSTALACAO.md))
- Academic report, UML diagrams, and state machines in Typst (`LI4/`)

</td>
</tr>
</table>

---

## 🏗️ Architecture

```
MACHINE A — Headquarters (192.168.1.82)      LOCAL NETWORK
┌──────────────────────────┐
│ docker-compose-          │       HTTP (demo) / HTTPS (production)
│   central.yml            │◄──────────────────────────────────┐
│                          │                                   │
│  :8081  Global API       │                                   │
│  :8080  Local API        │       MACHINE B — Store N         │
│  :5173  Frontend         │       ┌──────────────────────────┐│
│  :5432  PostgreSQL       │       │ docker-compose.yml       ││
└──────────────────────────┘       │  :8080  Local API        │┘
                                   │  :5173  Frontend          │
                                   │  :5433  PostgreSQL        │
                                   └──────────────────────────┘
                                                ▲
                                       Terminal (browser)
                                       http://<STORE_IP>:5173
```

| Layer | Technology |
|---|---|
| Backend | Java 21 · Javalin · Gradle 8.6 |
| Database | PostgreSQL 16 (UUID, *outbox pattern*) |
| Authentication | **RS256** JWT via HttpOnly cookie |
| Frontend | Vite (host `0.0.0.0`) |
| Orchestration | Docker Compose v2 |
| Documentation | Typst |

| File | Mode | When to use |
|---|---|---|
| `docker-compose-central.yml` | **CENTRAL** | Headquarters server — Local + Global API |
| `docker-compose.yml`         | **BRANCH**  | Server for each store — Local API only |
| `docker-compose-dev.yml`     | **DEV**     | Complete environment on a single machine |

---

## 🛠️ Installation

### Prerequisites

| Component | Minimum version | Check with |
|---|---|---|
| Docker Engine | **24.x** | `docker --version` |
| Docker Compose | **2.x** (plugin) | `docker compose version` |
| Git | **2.x** | `git --version` |

> JDK 21 and Node.js are **not** required on the machines — Docker handles everything. They are only needed if you want to run the project outside the containers.

### Quick Start

1. **Clone the repository:**

```bash
git clone https://github.com/SirLordNelson/LI4_2526.git
cd LI4_2526
git checkout dev
```

2. **Generate RSA keys** *(only once, shared across the network)*:

```bash
openssl genrsa -out private_key.pem 2048
openssl rsa -in private_key.pem -pubout -out public_key.pem
base64 -w 0 public_key.pem  > public_key.b64
base64 -w 0 private_key.pem > private_key.b64
```

> ⚠ **Important:** the same key pair must exist at the headquarters **and** in all stores — otherwise JWTs will not be mutually recognized.

3. **Configure .env:**

```bash
cp .env.example .env
# Edit and paste TAKI_JWT_PUBLIC_KEY and TAKI_JWT_PRIVATE_KEY
```

4. **Start the Headquarters:**

```bash
docker compose -f docker-compose-central.yml down -v --remove-orphans
docker compose -f docker-compose-central.yml up -d --build
sleep 5
bash ./database/load_data.sh central
```

5. **Start a Store** *(repeat for each branch, `N = 1..4`)*:

In `docker-compose.yml`, adjust `TAKI_CORS_ORIGIN` to the store's frontend IP and add to `.env`:

```ini
URLS_SEDE_CSV=http://192.168.1.82:8081/api/v1
```

Then:

```bash
docker compose -f docker-compose.yml down -v --remove-orphans
docker compose -f docker-compose.yml up -d --build
sleep 5
bash ./database/load_data.sh N   # 1, 2, 3 or 4
```

> 📘 **Production installation:** see the detailed procedure in [`docs/MANUAL_INSTALACAO.md`](docs/MANUAL_INSTALACAO.md).

---

## 🚀 Usage

### LAN Access

With the headquarters at `192.168.1.82` (registered in `lan_ip.txt`), any device on the same network accesses:

| Service | URL |
|---|---|
| 🖥️  Frontend (headquarters) | `http://192.168.1.82:5173` |
| 🔌 Local API        | `http://192.168.1.82:8080` |
| 🌐 Global API       | `http://192.168.1.82:8081` |

To discover the machine's current IP:

```bash
cat lan_ip.txt
# or
hostname -I | awk '{print $1}'
```

### Quick check

```bash
# Global API health-check (headquarters)
curl http://localhost:8081/health
# Expected: 200 OK

# Central manager login
curl -c cookies.txt -s -X POST http://localhost:8081/api/global/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"dono@taki.pt","password":"admin"}' | python3 -m json.tool
```

### Firewall (Linux)

```bash
sudo ufw allow 5173/tcp   # Frontend
sudo ufw allow 8080/tcp   # Local API
sudo ufw allow 8081/tcp   # Global API
```

### Demo Credentials

| User | Email | Password | Role | Store |
|---|---|---|---|---|
| Central Manager  | `dono@taki.pt`            | `admin` | `GESTOR_CENTRAL`  | Central |
| Store 1 Manager  | `tiago.rocha@taki.pt`     | `admin` | `GESTOR_LOJA`     | 1 |
| Store 1 Cashier | `operador1@taki.pt`       | `admin` | `OPERADOR_CAIXA`  | 1 |
| Store 2 Manager  | `sofia.alves@taki.pt`     | `admin` | `GESTOR_LOJA`     | 2 |
| Store 2 Cashier | `operador2@taki.pt`       | `admin` | `OPERADOR_CAIXA`  | 2 |
| Store 3 Manager  | `carolina.jesus@taki.pt`  | `admin` | `GESTOR_LOJA`     | 3 |
| Store 3 Cashier | `operador3@taki.pt`       | `admin` | `OPERADOR_CAIXA`  | 3 |
| Store 4 Manager  | `david.marques@taki.pt`   | `admin` | `GESTOR_LOJA`     | 4 |
| Store 4 Cashier | `operador4@taki.pt`       | `admin` | `OPERADOR_CAIXA`  | 4 |

---

## 📚 Documentation

| Document | Description |
|---|---|
| [`docs/MANUAL_INSTALACAO.md`](docs/MANUAL_INSTALACAO.md) | Complete installation and production deployment procedure (headquarters + branches) |
| [`docs/MANUAL_TEST_SCRIPT.md`](docs/MANUAL_TEST_SCRIPT.md) | Manual test script for Local API, Global API, synchronization, and UI |
| `LI4/` | Academic report (Typst) — requirements, use cases, and diagrams |
| `seq/` · `*.puml` | UML class, sequence, and state machine diagrams (PlantUML) |
| [`AUDIT_REPORT.md`](AUDIT_REPORT.md) | Internal code and security audit report |

---

## 📂 Project Structure

```
LI4_2526/
├── 📁 backend/                  # Javalin Application (Java 21 + Gradle)
│   └── src/main/java/pt/uminho/taki/
│       ├── 📁 api/local/        # Local API routes and controller
│       ├── 📁 api/global/       # Global API routes and controller
│       └── 📁 ln/               # Business logic (sales, inventory, sync, …)
├── 📁 frontend/                 # Vite SPA (host 0.0.0.0)
├── 📁 database/
│   ├── 📄 schema_postgres.sql       # DDL + UUID types
│   ├── 📄 logica_ativa_postgres.sql # Triggers and procedures
│   ├── 📄 sincronizacao_postgres.sql# Outbox + sync functions
│   ├── 📄 user_roles.sql            # Profiles and permissions
│   ├── 📄 indices_postgres.sql      # Performance indices
│   ├── 🐍 populate.py               # Generates deterministic CSVs
│   └── 📜 load_data.sh              # Loads CSVs (central|1..4)
├── 📁 docs/                     # Installation and test manuals
├── 📁 seq/                      # Sequence diagrams (PlantUML)
├── 📁 LI4/                      # Academic report (Typst)
├── 🐳 docker-compose.yml        # BRANCH mode (store)
├── 🐳 docker-compose-central.yml# CENTRAL mode (headquarters)
├── 🐳 docker-compose-dev.yml    # DEV mode (all in one machine)
├── ⚙️  .env.example
├── 🌐 lan_ip.txt                # Current headquarters IP
├── 📄 LICENSE
└── 📄 README.md                 # This file
```

---

## 🩺 Troubleshooting

| Symptom | Probable cause | Solution |
|---|---|---|
| `CORS error` in browser | Wrong `TAKI_CORS_ORIGIN` | Update the frontend IP in `docker-compose*.yml` and restart |
| Backend won't start | Missing JWT keys | Check `TAKI_JWT_PUBLIC_KEY` / `TAKI_JWT_PRIVATE_KEY` in `.env` |
| DB won't initialize | Port already in use | `docker compose down -v` and `ss -tlnp \| grep 5432` |
| Frontend can't access backend | Hardcoded IP | `VITE_LOCAL_API_TARGET` must use `http://backend:8080` inside Docker |
| `401 Unauthorized` | Cookie not sent | Ensure `withCredentials: true` in Vite proxy |
| Synchronization fails | Central endpoint inaccessible | Check `URLS_SEDE_CSV` in `.env` and network connectivity |

---

## 👥 Authors

**Group 17** — *Informatics Labs IV · Software Engineering · 2025/2026*

<table>
<tr>
<td align="center">
<a href="https://github.com/SirLordNelson">
<img src="https://github.com/SirLordNelson.png" width="100px;" alt="SirLordNelson"/><br />
<sub><b>SirLordNelson</b></sub>
</a>
</td>
<td align="center">
<a href="https://github.com/PaoComPlanta">
<img src="https://github.com/PaoComPlanta.png" width="100px;" alt="PaoComPlanta"/><br />
<sub><b>PaoComPlanta</b></sub>
</a>
</td>
<td align="center">
<a href="https://github.com/DelgadoDevT">
<img src="https://github.com/DelgadoDevT.png" width="100px;" alt="DelgadoDevT"/><br />
<sub><b>DelgadoDevT</b></sub>
</a>
</td>
<td align="center">
<a href="https://github.com/M4chad0">
<img src="https://github.com/M4chad0.png" width="100px;" alt="M4chad0"/><br />
<sub><b>M4chad0</b></sub>
</a>
</td>
<td align="center">
<a href="https://github.com/Kyuma23">
<img src="https://github.com/Kyuma23.png" width="100px;" alt="Kyuma23"/><br />
<sub><b>Kyuma23</b></sub>
</a>
</td>
</tr>
</table>

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Made with 🛒 for the Informatics Labs IV Course**

*Universidade do Minho • 2025/2026*

</div>