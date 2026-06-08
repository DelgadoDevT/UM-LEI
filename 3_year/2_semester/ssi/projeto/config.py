"""
Loads deployment-specific configuration from a .env file.
"""
import os
from dotenv import load_dotenv

# Load environment variables from a .env file in the project root
load_dotenv()

# -- Server Network Configuration --
SERVER_HOST: str = os.getenv("SERVER_HOST", "127.0.0.1")
SERVER_PORT: int = int(os.getenv("SERVER_PORT", 9999))
SERVER_IDLE_TIMEOUT_SECONDS: int = int(os.getenv("SERVER_IDLE_TIMEOUT_SECONDS", 300))

# -- Client P2P Network Configuration --
CLIENT_P2P_HOST: str = os.getenv("CLIENT_P2P_HOST", "0.0.0.0")
CLIENT_P2P_PORT: int = int(os.getenv("CLIENT_P2P_PORT", 0))
CLIENT_P2P_ADVERTISE_HOST: str = os.getenv("CLIENT_P2P_ADVERTISE_HOST", "")

# -- Database Path Configuration --
DB_PATH_SERVER: str = os.getenv("DB_PATH_SERVER", "server_data.db")
DB_PATH_CLIENT: str = os.getenv("DB_PATH_CLIENT", "client_data.db")

# -- Database Encryption Key --
DB_KEY_HEX: str = os.getenv("DB_KEY_HEX", "")
DB_KEY_FILE: str = os.getenv("DB_KEY_FILE", "ca/server_db.key")

# -- Certificate Authority Configuration --
CA_KEY_PATH: str = os.getenv("CA_KEY_PATH", "ca/ca_private_key.raw")
CA_CERT_PATH: str = os.getenv("CA_CERT_PATH", "ca/ca_root_cert.pem")

# -- Cryptographic Constants --
AES_KEY_SIZE: int = 32
NONCE_SIZE: int = 12
HKDF_HASH: str = "SHA256"
X3DH_NUM_ONETIME_KEYS: int = 10
RATCHET_MAX_SKIP: int = 100
BUFFER_SIZE: int = 65536
