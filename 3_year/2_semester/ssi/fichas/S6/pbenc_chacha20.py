import struct, os, sys, getpass
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC

def deriveKey(password: bytes, salt: bytes) -> bytes:
    kdf = PBKDF2HMAC(
        algorithm=hashes.SHA256(),
        length=32,
        salt=salt,
        iterations=1_200_000,
    )
    return kdf.derive(password)

def encryptChaCha20(filename):
    password = getpass.getpass("Pass-phrase: ").encode('utf-8')
    salt = os.urandom(16)

    key = deriveKey(password, salt)
   
    nonce = os.urandom(8)
    counter = 0
    full_nonce = struct.pack("<Q", counter) + nonce
    algorithm = algorithms.ChaCha20(key, full_nonce)
    cipher = Cipher(algorithm, mode=None)
    encryptor = cipher.encryptor()
    
    with open(filename, 'rb') as file:
        plaintext = file.read()
    
    cryptography = encryptor.update(plaintext)
    with open(filename + '.enc', 'wb') as encFile:
        encFile.write(salt)
        encFile.write(full_nonce)
        encFile.write(cryptography)

def decryptChaCha20(filename):
    password = getpass.getpass("Pass-phrase: ").encode('utf-8')
    
    with open(filename, 'rb') as encFile:
        salt = encFile.read(16)
        full_nonce = encFile.read(16)
        ciphertext = encFile.read()

    key = deriveKey(password, salt)

    algorithm = algorithms.ChaCha20(key, full_nonce)
    cipher = Cipher(algorithm, mode=None)
    decryptor = cipher.decryptor()
    plaintext = decryptor.update(ciphertext)

    with open(filename + '.dec', 'wb') as decFile:
        decFile.write(plaintext)  

def main():
    if sys.argv[1] == 'enc':
        encryptChaCha20(sys.argv[2])
    elif sys.argv[1] == 'dec':
        decryptChaCha20(sys.argv[2])

if __name__ == '__main__':
    main()