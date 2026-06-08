"""
E2EE Chat Client Package.

Provides a secure end-to-end encrypted chat client with:
- X3DH key agreement
- Double Ratchet forward secrecy
- CLI interface
"""
from src.client.client import ChatClient
from src.client.key_manager import KeyManager
from src.client.session_manager import SessionManager
from src.client.contact_manager import ContactManager
from src.client.cli import ChatCLI

__all__ = [
    "ChatClient",
    "KeyManager",
    "SessionManager",
    "ContactManager",
    "ChatCLI",
]