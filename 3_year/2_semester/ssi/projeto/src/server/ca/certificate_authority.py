"""Manage root CA lifecycle and user certificate issuance.

Create or load the server root certificate authority and provide helpers for
issuing per-user certificates and exporting the CA certificate in PEM format.
"""
import os

from src.crypto.certificates import create_ca_certificate, issue_user_certificate, cert_to_pem, pem_to_cert
from src.crypto.asymmetric import generate_ed25519_keypair, deserialize_ed25519_private, serialize_private_key
from src.rich_logging import log_security, log_success
import config


class CertificateAuthority:
    """Provide certificate authority services for the chat server.

    Load persisted CA material when present, otherwise generate a new root key
    and self-signed certificate, then expose certificate issuance operations.

    :ivar _ca_key_path: Filesystem path where the CA private key is stored.
    :vartype _ca_key_path: str
    :ivar _ca_cert_path: Filesystem path where the CA certificate is stored.
    :vartype _ca_cert_path: str
    :ivar _ca_priv: In-memory Ed25519 private key for CA signing.
    :vartype _ca_priv: cryptography.hazmat.primitives.asymmetric.ed25519.Ed25519PrivateKey
    :ivar _ca_pub: In-memory Ed25519 public key derived from `_ca_priv`.
    :vartype _ca_pub: cryptography.hazmat.primitives.asymmetric.ed25519.Ed25519PublicKey
    :ivar ca_cert: X.509 certificate object representing the root CA.
    :vartype ca_cert: cryptography.x509.Certificate
    """

    def __init__(self):
        """Initialize CA paths and load or generate root materials.

        :returns: None.
        :rtype: None
        """
        self._ca_key_path = config.CA_KEY_PATH
        self._ca_cert_path = config.CA_CERT_PATH
        if os.path.exists(self._ca_key_path) and os.path.exists(self._ca_cert_path):
            self._load_ca()
        else:
            self._generate_ca()

    def _generate_ca(self):
        """Generate and persist a new root CA key and certificate."""
        log_security("ca.generate", "Generating new Root CA key and certificate")
        self._ca_priv, self._ca_pub = generate_ed25519_keypair()
        self.ca_cert = create_ca_certificate(self._ca_priv, "SecureChatCA")
        os.makedirs(os.path.dirname(self._ca_key_path), exist_ok=True)
        with open(self._ca_key_path, "wb") as f:
            f.write(serialize_private_key(self._ca_priv))
        with open(self._ca_cert_path, "wb") as f:
            f.write(cert_to_pem(self.ca_cert))
        log_success("ca.generate", "New Root CA created and saved", cert_path=self._ca_cert_path)

    def _load_ca(self):
        """Load persisted root CA key and certificate from disk."""
        log_security("ca.load", "Loading existing Root CA")
        with open(self._ca_key_path, "rb") as f:
            self._ca_priv = deserialize_ed25519_private(f.read())
        with open(self._ca_cert_path, "rb") as f:
            self.ca_cert = pem_to_cert(f.read())
        self._ca_pub = self._ca_priv.public_key()
        log_success("ca.load", "Root CA loaded successfully", cert_path=self._ca_cert_path)

    def issue_user_cert(self, user_public_key, username):
        """Issue a certificate for a user identity public key.

        :param user_public_key: User Ed25519 public key to certify.
        :type user_public_key: cryptography.hazmat.primitives.asymmetric.ed25519.Ed25519PublicKey
        :param username: Subject common name for the user certificate.
        :type username: str
        :returns: Newly issued user certificate.
        :rtype: cryptography.x509.Certificate
        :raises ValueError: If certificate generation receives invalid inputs.
        """
        return issue_user_certificate(self._ca_priv, self.ca_cert, user_public_key, username)

    def get_ca_cert_pem(self):
        """Return the root CA certificate serialized as PEM bytes.

        :returns: PEM-encoded root certificate.
        :rtype: bytes
        """
        return cert_to_pem(self.ca_cert)
