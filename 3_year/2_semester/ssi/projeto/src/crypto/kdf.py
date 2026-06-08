"""Key derivation helpers for protocol and ratchet state evolution."""

from __future__ import annotations

from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.hmac import HMAC
from cryptography.hazmat.primitives.kdf.hkdf import HKDF


def hkdf_derive(input_key_material: bytes, length: int, salt: bytes, info: bytes) -> bytes:
	"""Derive key material using HKDF-SHA256.

	Security guarantees:
	- Domain-separated output through explicit ``info``.
	- Salted extraction stage to harden derivation from input material.
	"""
	if length <= 0:
		raise ValueError("HKDF output length must be positive")

	hkdf = HKDF(
		algorithm=hashes.SHA256(),
		length=length,
		salt=salt,
		info=info,
	)
	return hkdf.derive(input_key_material)


def hkdf_extract_and_expand(
	input_key_material: bytes,
	salt: bytes,
	info: bytes,
	length: int = 32,
) -> bytes:
	"""Alias for HKDF derive to make call-sites explicit."""
	return hkdf_derive(input_key_material, length=length, salt=salt, info=info)


def kdf_rk(root_key: bytes, dh_output: bytes) -> tuple[bytes, bytes]:
	"""Double Ratchet root KDF step.

	Security guarantees:
	- Advances root key after every DH output.
	- Produces a fresh chain key linked to the new root key.
	"""
	out = hkdf_derive(
		input_key_material=dh_output,
		length=64,
		salt=root_key,
		info=b"RootKeyDerivation",
	)
	return out[:32], out[32:]


def kdf_ck(chain_key: bytes) -> tuple[bytes, bytes]:
	"""Double Ratchet chain KDF step using HMAC-SHA256.

	Security guarantees:
	- Derives unique message keys per message index.
	- Evolves the chain key so old message keys cannot be recomputed.
	"""
	hmac_message_key = HMAC(chain_key, hashes.SHA256())
	hmac_message_key.update(b"\x01")
	message_key = hmac_message_key.finalize()

	hmac_chain_key = HMAC(chain_key, hashes.SHA256())
	hmac_chain_key.update(b"\x02")
	new_chain_key = hmac_chain_key.finalize()

	return new_chain_key, message_key
