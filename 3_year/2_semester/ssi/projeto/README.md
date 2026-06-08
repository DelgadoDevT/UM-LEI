# Secure Chat E2EE

End-to-end encrypted chat system for the Information Systems Security project (2025/2026), based on a client-server architecture with optional direct peer fallback.

[![Python](https://img.shields.io/badge/Python-3.11%2B-blue.svg)](https://www.python.org/)
[![Tests](https://img.shields.io/badge/Tests-181%20Passing-success.svg)]()
[![Crypto](https://img.shields.io/badge/Crypto-X3DH%20%2B%20Double%20Ratchet-orange.svg)]()
[![Mode](https://img.shields.io/badge/Delivery-Server--first%20with%20Direct%20Fallback-purple.svg)]()

## Table of contents

- [Secure Chat E2EE](#secure-chat-e2ee)
  - [Table of contents](#table-of-contents)
  - [Project goals](#project-goals)
  - [Implemented features](#implemented-features)
  - [Architecture overview](#architecture-overview)
    - [Message flow (high level)](#message-flow-high-level)
  - [Security model and cryptography](#security-model-and-cryptography)
  - [Installation and setup](#installation-and-setup)
    - [Prerequisites](#prerequisites)
    - [Install dependencies](#install-dependencies)
    - [Optional environment configuration](#optional-environment-configuration)
  - [Run the system](#run-the-system)
    - [Start server](#start-server)
    - [Start one client (new terminal)](#start-one-client-new-terminal)
    - [Typical first run flow](#typical-first-run-flow)
    - [Useful Make targets](#useful-make-targets)
  - [CLI command reference](#cli-command-reference)
    - [Connection and auth](#connection-and-auth)
    - [Contacts and direct messages](#contacts-and-direct-messages)
    - [Group commands](#group-commands)
  - [Testing](#testing)
    - [Test files and scope](#test-files-and-scope)
    - [Recent regression-focused test additions](#recent-regression-focused-test-additions)
  - [Known limitations](#known-limitations)
  - [Repository structure](#repository-structure)
  - [Related documentation](#related-documentation)
  - [Authors](#authors)

## Project goals

This project implements the requirements of the course statement:

- End-to-end confidentiality and integrity of user messages.
- Server as coordinator/router (honest-but-curious model).
- Client-side command interface for account/contact/message management.
- Support for value-added features: PKI, offline messages, group messaging, forward secrecy, and direct fallback mode.

## Implemented features

- **1:1 E2EE messaging** with X3DH session bootstrap + Double Ratchet message encryption.
- **Server relay + offline queue** for users not currently connected.
- **Direct peer fallback** when server send fails (`send_chat_message_routed`).
- **Offline first-message bootstrap**: when server is disconnected, clients can initialize a new session from cached contact bundle and send direct.
- **PKI support**: server-side CA, per-user certificate issuance, client-side certificate verification in contact management.
- **Group messaging** with sender-key based group encryption and local group history.
- **Group member removal + coordinated rekey** to rotate sender keys after membership changes.
- **Rich terminal UI (client)** for interactive use (`/connect`, `/register`, `/login`, `/msg`, `/group ...`, etc.).
- **Desktop GUI client** with local FastAPI backend and live WebSocket event stream for contacts, direct messaging, groups, invites, and offline fetch workflows.
- **CLI navigation improvements**: TAB completion (`readline`) and per-command output separators for better readability.
- **Server runtime dashboard** with live counters (when running in a compatible terminal).

## Architecture overview

The codebase is split into five main modules:

| Module | Main responsibility | Key files |
|---|---|---|
| `src/crypto` | Cryptographic primitives and protocols | `primitives.py`, `x3dh.py`, `ratchet.py`, `certificates.py` |
| `src/protocol` | Wire messages, framing, transport handshake | `messages.py`, `transport.py`, `handshake.py` |
| `src/storage` | SQLite wrappers for server/client persistent data | `secure_db.py`, `client_db.py` |
| `src/server` | Async TCP server, auth, routing, offline delivery, CA integration, runtime dashboard | `server.py`, `connection_handler.py`, `runtime_dashboard.py`, `ca/certificate_authority.py` |
| `src/client` | Chat client runtime, terminal UI, local key/session/contact/group managers | `client.py`, `cli.py`, `session_manager.py`, `group_manager.py` |

### Message flow (high level)

1. **Client connects to server** and runs ephemeral X25519 transport handshake.
2. **User registers/logs in**, uploads/fetches public bundle material.
3. **First 1:1 message** bootstraps session via X3DH (if no session exists).
4. **Subsequent messages** use Double Ratchet state evolution.
5. **Routing policy**: try server send first; if it fails and peer endpoint is known, fallback to direct P2P send.
6. **Direct endpoint stability**: with `CLIENT_P2P_PORT=0`, each user gets a deterministic per-user direct port, reducing stale endpoint issues across restarts.

## Security model and cryptography

| Area | Current implementation |
|---|---|
| Transport protection | ECDH-derived directional transport keys + AES-GCM envelope per framed message |
| 1:1 session bootstrap | X3DH (IK + SPK + optional OPK) |
| Ongoing 1:1 secrecy | Double Ratchet (DH ratchet + symmetric chain ratchet) |
| Symmetric encryption | AES-256-GCM |
| Asymmetric primitives | Ed25519 (signatures), X25519 (ECDH) |
| KDFs | HKDF-SHA256 + HMAC-SHA256 based chain derivation |
| Password storage | PBKDF2-SHA256 with per-user salt and high iteration count |
| Identity binding | CA-issued user certificates and client verification when adding contacts |

## Installation and setup

### Prerequisites

- Python 3.11+
- Poetry

### Install dependencies

```bash
make install
```

### Optional environment configuration

```bash
cp .env.example .env
```

Main config values are loaded from `config.py`/`.env`:

- `SERVER_HOST`, `SERVER_PORT`
- `CLIENT_P2P_HOST`, `CLIENT_P2P_PORT`, `CLIENT_P2P_ADVERTISE_HOST`
- `DB_PATH_SERVER`, `DB_PATH_CLIENT`
- `DB_KEY_HEX`, `DB_KEY_FILE`
- `CA_KEY_PATH`, `CA_CERT_PATH`

## Run the system

### Start server

```bash
make server
```

When running in a compatible terminal, the server also shows a live runtime dashboard panel.

### Start one client (new terminal)

```bash
make client
```

### Start desktop GUI client (new terminal)

```bash
make gui
```

Client quality-of-life features:
- TAB completion for slash commands (and `/group` subcommands) in interactive terminals.
- Visual separators for command output and async notifications (`LIVE`/`OFFLINE`).

### Typical first run flow

```text
/connect
/register <username>    # or /login <username> if already registered on this device
/add <contact>
/msg <contact> hello
```

### Useful Make targets

| Command | Description |
|---|---|
| `make help` | Show available targets |
| `make install` | Install dependencies with Poetry |
| `make test` | Run full test suite (`pytest -q`) |
| `make test-v` | Run tests in verbose mode |
| `make server` | Start server |
| `make client` | Start one client instance |
| `make gui` | Start desktop GUI client |
| `make clean-data` | Remove local DB/client test data |

## CLI command reference

### Connection and auth

| Command | Description |
|---|---|
| `/help` | Show available commands |
| `/connect` | Connect to server and start direct listener |
| `/disconnect` | Disconnect only from server (keeps direct listener active for peer fallback) |
| `/register <username>` | Register account |
| `/login <username>` | Login account |
| `/quit` | Exit client |
| `/exit` | Exit client (alias for `/quit`) |

### Contacts and direct messages

| Command | Description |
|---|---|
| `/contacts` | List saved contacts |
| `/add <username>` | Fetch bundle and add contact |
| `/msg <username> <message>` | Send encrypted 1:1 message |
| `/history <username>` | Show local history with contact |
| `/offline` | Fetch queued offline messages |

### Group commands

| Command | Description |
|---|---|
| `/group create <name>` | Create group |
| `/group list` | List groups |
| `/group invites` | List pending invites |
| `/group accept <group_id>` | Accept invite |
| `/group reject <group_id>` | Reject invite |
| `/group members <group_id>` | List group members |
| `/group invite <group_id> <username>` | Invite member |
| `/group remove <group_id> <username>` | Remove member and trigger group rekey |
| `/group msg <group_id> <message>` | Send group message |
| `/group history <group_id>` | Show local group history |

## Testing

Run all tests:

```bash
make test
```

or:

```bash
poetry run pytest -q
```

Current suite status: **136 passing tests**.

### Test files and scope

| File | Scope |
|---|---|
| `tests/test_crypto.py` | AES-GCM, HKDF/KDF, signatures, key serialization |
| `tests/test_x3dh.py` | X3DH success/failure paths |
| `tests/test_ratchet.py` | Ratchet state evolution, out-of-order, tamper rejection |
| `tests/test_protocol.py` | Message serialization, framing, handshake compatibility |
| `tests/test_server.py` | DB/CA/server logic and server-side workflows |
| `tests/test_storage.py` | SQLite-backed storage behavior, integrity checks, concurrency paths |
| `tests/test_client.py` | Key/session/contact/group managers and CLI behaviors |
| `tests/test_integration.py` | End-to-end client-server integration, routing/fallback/disconnect behavior |
| `tests/test_rich_logging.py` | Rich logging formatting and separator behavior |

### Recent regression-focused test additions

Recent additions include:

1. Invalid non-command input rejection in CLI.
2. Unknown command handling in CLI.
3. Group command parsing failure (`/group msg` without message text).
4. Client receive-loop behavior when server-side socket drops.
5. Routed send fallback behavior after server-only disconnect.
6. Endpoint refresh and stale endpoint prevention for direct fallback.
7. Offline first-message direct bootstrap from cached contact bundles.
8. Client TAB completion and command output title behavior.
9. Server logging separator presentation behavior.

## Known limitations

These points reflect the current code state:

- No multi-device key sync: a login requires local key material already present for that username on that device.
- Direct fallback still depends on peer reachability at send time (recipient must be online with direct listener active for immediate delivery).

## Repository structure

```text
trabalho-pratico/
├── src/
│   ├── client/
│   ├── server/
│   ├── protocol/
│   ├── crypto/
│   └── storage/
├── tests/
├── guides/      (local documentation artifacts)
├── docs/        (Sphinx and screenshots)
├── diagrams/    (UML diagrams and exports)
├── report.md
├── pyproject.toml
├── config.py
└── Makefile
```

## Related documentation

- `report.md`: formal project report for delivery, containing the full technical specification and architecture details.

## Authors

<table>
<tr>
<td align="center">
<a href="https://github.com/DelgadoDevT">
<img src="https://github.com/DelgadoDevT.png" width="100px;" alt="DelgadoDevT"/><br />
<sub><b>DelgadoDevT</b></sub>
</a>
</td>
<td align="center">
<a href="https://github.com/PaoComPlanta">
<img src="https://github.com/PaoComPlanta.png" width="100px;" alt="PaoComPlanta"/><br />
<sub><b>PaoComPlanta</b></sub>
</a>
</td>
<td align="center">
<a href="https://github.com/yHugoSoares">
<img src="https://github.com/yHugoSoares.png" width="100px;" alt="yHugoSoares"/><br />
<sub><b>yHugoSoares</b></sub>
</a>
</td>
</tr>
</table>

<div align="center">
