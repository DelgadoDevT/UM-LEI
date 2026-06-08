"""Create, serialize, and validate X.509 certificates for Secure Chat.

Provide helpers to generate a root CA certificate, issue user certificates,
convert certificate encodings, and validate trust and subject metadata.
"""
import datetime
from cryptography import x509
from cryptography.hazmat.primitives.asymmetric.ed25519 import Ed25519PrivateKey, Ed25519PublicKey
from cryptography.x509.oid import NameOID
from cryptography.hazmat.primitives import hashes


def create_ca_certificate(ca_private_key: Ed25519PrivateKey, ca_name: str) -> x509.Certificate:
    """Create a self-signed root CA certificate.

    :param ca_private_key: Ed25519 private key used to sign the certificate.
    :type ca_private_key: Ed25519PrivateKey
    :param ca_name: Common Name value for the CA subject.
    :type ca_name: str
    :returns: Self-signed X.509 root certificate.
    :rtype: x509.Certificate

    Example::

        ca_cert = create_ca_certificate(ca_priv, "SecureChatCA")
    """
    ca_public_key = ca_private_key.public_key()
    
    subject = issuer = x509.Name([
        x509.NameAttribute(NameOID.COMMON_NAME, ca_name),
        x509.NameAttribute(NameOID.ORGANIZATION_NAME, "SecureChat"),
    ])
    
    now = datetime.datetime.now(datetime.timezone.utc)
    
    cert = (
        x509.CertificateBuilder()
        .subject_name(subject)
        .issuer_name(issuer)
        .public_key(ca_public_key)
        .serial_number(x509.random_serial_number())
        .not_valid_before(now)
        .not_valid_after(now + datetime.timedelta(days=3650))  # 10 years
        .add_extension(
            x509.BasicConstraints(ca=True, path_length=0),
            critical=True,
        )
        .add_extension(
            x509.KeyUsage(
                digital_signature=True,
                key_cert_sign=True,
                crl_sign=True,
                key_encipherment=False,
                content_commitment=False,
                data_encipherment=False,
                key_agreement=False,
                encipher_only=False,
                decipher_only=False,
            ),
            critical=True,
        )
        .sign(ca_private_key, None)  # Ed25519 doesn't use a hash algorithm parameter
    )
    
    return cert


def issue_user_certificate(
    ca_private_key: Ed25519PrivateKey,
    ca_cert: x509.Certificate,
    user_public_key: Ed25519PublicKey,
    username: str
) -> x509.Certificate:
    """Issue a user identity certificate signed by the root CA.

    :param ca_private_key: CA Ed25519 private key used to sign the certificate.
    :type ca_private_key: Ed25519PrivateKey
    :param ca_cert: CA certificate used as issuer reference.
    :type ca_cert: x509.Certificate
    :param user_public_key: User Ed25519 public key to certify.
    :type user_public_key: Ed25519PublicKey
    :param username: Username embedded as certificate common name.
    :type username: str
    :returns: Signed X.509 certificate for the user.
    :rtype: x509.Certificate

    Example::

        user_cert = issue_user_certificate(ca_priv, ca_cert, user_pub, "alice")
    """
    subject = x509.Name([
        x509.NameAttribute(NameOID.COMMON_NAME, username),
    ])
    
    now = datetime.datetime.now(datetime.timezone.utc)
    
    cert = (
        x509.CertificateBuilder()
        .subject_name(subject)
        .issuer_name(ca_cert.subject)
        .public_key(user_public_key)
        .serial_number(x509.random_serial_number())
        .not_valid_before(now)
        .not_valid_after(now + datetime.timedelta(days=365))  # 1 year
        .add_extension(
            x509.BasicConstraints(ca=False, path_length=None),
            critical=True,
        )
        .add_extension(
            x509.KeyUsage(
                digital_signature=True,
                key_cert_sign=False,
                crl_sign=False,
                key_encipherment=False,
                content_commitment=False,
                data_encipherment=False,
                key_agreement=False,
                encipher_only=False,
                decipher_only=False,
            ),
            critical=True,
        )
        .sign(ca_private_key, None)  # Ed25519 doesn't use a hash algorithm parameter
    )
    
    return cert


def cert_to_pem(cert: x509.Certificate) -> bytes:
    """Serialize a certificate to PEM bytes.

    :param cert: X.509 certificate to serialize.
    :type cert: x509.Certificate
    :returns: PEM-encoded certificate bytes.
    :rtype: bytes
    """
    from cryptography.hazmat.primitives.serialization import Encoding
    return cert.public_bytes(Encoding.PEM)


def pem_to_cert(pem_bytes: bytes) -> x509.Certificate:
    """Deserialize a PEM-encoded certificate.

    :param pem_bytes: PEM-encoded certificate bytes.
    :type pem_bytes: bytes
    :returns: Parsed X.509 certificate object.
    :rtype: x509.Certificate
    :raises ValueError: If `pem_bytes` does not contain a valid certificate.
    """
    return x509.load_pem_x509_certificate(pem_bytes)


def verify_certificate(cert: x509.Certificate, ca_cert: x509.Certificate) -> bool:
    """Verify certificate signature and validity period against a CA.

    :param cert: Certificate to verify.
    :type cert: x509.Certificate
    :param ca_cert: Trusted CA certificate used for signature verification.
    :type ca_cert: x509.Certificate
    :returns: ``True`` when certificate is signed by the CA and time-valid.
    :rtype: bool
    """
    try:
        ca_public_key = ca_cert.public_key()
        ca_public_key.verify(cert.signature, cert.tbs_certificate_bytes)
        
        # Check validity period
        now = datetime.datetime.now(datetime.timezone.utc)
        if cert.not_valid_before_utc > now or cert.not_valid_after_utc < now:
            return False
        
        return True
    except Exception:
        return False


def get_cert_username(cert: x509.Certificate) -> str:
    """Extract the certificate common name as username.

    :param cert: X.509 certificate to inspect.
    :type cert: x509.Certificate
    :returns: Common Name value from the certificate subject.
    :rtype: str
    :raises ValueError: If the certificate subject has no Common Name.
    """
    for attr in cert.subject:
        if attr.oid == NameOID.COMMON_NAME:
            return attr.value
    raise ValueError("Certificate has no Common Name")
