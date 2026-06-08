"""Symmetric authenticated encryption primitives."""

from __future__ import annotations

import os

from cryptography.hazmat.primitives import constant_time
from cryptography.hazmat.primitives.ciphers.aead import AESGCM

# Re-export for backward compatibility with modules that import HKDF from primitives.
from .kdf import hkdf_derive

from config import AES_KEY_SIZE, NONCE_SIZE


def aes_gcm_encrypt(key: bytes, plaintext: bytes, aad: bytes = b"") -> tuple[bytes, bytes]:
	"""Encrypt data with AES-256-GCM and return nonce plus ciphertext+tag.

	Security guarantees:
	- Confidentiality and integrity for plaintext and AAD.
	- Fresh random nonce is generated for every encryption operation.
	"""
	if len(key) != AES_KEY_SIZE:
		raise ValueError("AES-GCM key must be 32 bytes")

	nonce = os.urandom(NONCE_SIZE)
	ciphertext = AESGCM(key).encrypt(nonce, plaintext, aad)
	return nonce, ciphertext


def aes_gcm_decrypt(key: bytes, nonce: bytes, ciphertext: bytes, aad: bytes = b"") -> bytes:
	"""Decrypt AES-256-GCM ciphertext.

	Security guarantees:
	- Raises on authentication failure and never returns unauthenticated plaintext.
	- Authenticates both ciphertext and AAD.
	"""
	if len(key) != AES_KEY_SIZE:
		raise ValueError("AES-GCM key must be 32 bytes")
	if len(nonce) != NONCE_SIZE:
		raise ValueError("AES-GCM nonce must be 12 bytes")

	return AESGCM(key).decrypt(nonce, ciphertext, aad)


def constant_time_compare(a: bytes, b: bytes) -> bool:
	"""Compare bytes in constant time to avoid timing side-channel leaks."""
	return constant_time.bytes_eq(a, b)
