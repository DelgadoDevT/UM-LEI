"""Password hashing and verification helpers."""

from __future__ import annotations

import base64
import os

from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC

from src.crypto.primitives import constant_time_compare

_PBKDF2_SCHEME = "pbkdf2_sha256"
_PBKDF2_ITERATIONS = 600_000
_PBKDF2_SALT_BYTES = 16
_PBKDF2_DERIVED_BYTES = 32


def _pbkdf2_derive(password: str, salt: bytes, iterations: int) -> bytes:
    """Derive raw key material with PBKDF2-SHA256."""
    kdf = PBKDF2HMAC(
        algorithm=hashes.SHA256(),
        length=_PBKDF2_DERIVED_BYTES,
        salt=salt,
        iterations=iterations,
    )
    return kdf.derive(password.encode("utf-8"))


def hash_password(password: str) -> str:
    """Return a PBKDF2-SHA256 encoded password hash string."""
    salt = os.urandom(_PBKDF2_SALT_BYTES)
    derived = _pbkdf2_derive(password, salt, _PBKDF2_ITERATIONS)
    salt_b64 = base64.b64encode(salt).decode("ascii")
    derived_b64 = base64.b64encode(derived).decode("ascii")
    return f"{_PBKDF2_SCHEME}${_PBKDF2_ITERATIONS}${salt_b64}${derived_b64}"


def is_modern_password_hash(stored_hash: str) -> bool:
    """Return whether a password hash uses the PBKDF2 scheme."""
    return stored_hash.startswith(f"{_PBKDF2_SCHEME}$")


def verify_password(password: str, stored_hash: str) -> bool:
    """Verify plaintext password against PBKDF2-SHA256 hash."""
    if not stored_hash:
        return False

    if not is_modern_password_hash(stored_hash):
        return False

    parts = stored_hash.split("$")
    if len(parts) != 4:
        return False

    _, iterations_raw, salt_b64, expected_b64 = parts
    try:
        iterations = int(iterations_raw)
        salt = base64.b64decode(salt_b64.encode("ascii"))
        expected = base64.b64decode(expected_b64.encode("ascii"))
    except (ValueError, TypeError):
        return False
    derived = _pbkdf2_derive(password, salt, iterations)
    return constant_time_compare(derived, expected)
