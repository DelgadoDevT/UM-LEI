import os, sys, getpass
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC
from cryptography.hazmat.primitives import padding

def deriveKey(password: bytes, salt: bytes) -> bytes:
    kdf = PBKDF2HMAC(
        algorithm=hashes.SHA256(),
        length=64,
        salt=salt,
        iterations=1_200_000,
    )
    return kdf.derive(password)

def encryptAESHMAC(filename):
    password = getpass.getpass("Pass-phrase: ").encode('utf-8')
    salt = os.urandom(16)

    fullKey = deriveKey(password, salt)
    key = fullKey[:32]
    macKey = fullKey[32:]
   
    iv = os.urandom(16)
    cipher = Cipher(algorithms.AES(key), modes.CBC(iv))
    encryptor = cipher.encryptor()
    
    with open(filename, 'rb') as file:
        plaintext = file.read()

    padder = padding.PKCS7(128).padder()
    padded_data = padder.update(plaintext) + padder.finalize()

    cryptography = encryptor.update(padded_data) + encryptor.finalize()

    digest = hashes.Hash(hashes.SHA256())
    digest.update(macKey)
    digest.update(cryptography)
    mac = digest.finalize()

    with open(filename + '.enc', 'wb') as encFile:
        encFile.write(mac)
        encFile.write(salt)
        encFile.write(iv)
        encFile.write(cryptography)

def decryptAESHMAC(filename):
    password = getpass.getpass("Pass-phrase: ").encode('utf-8')
    
    with open(filename, 'rb') as encFile:
        mac  = encFile.read(32)
        salt = encFile.read(16)
        iv = encFile.read(16)
        ciphertext = encFile.read()

    fullKey = deriveKey(password, salt)
    key = fullKey[:32]
    macKey = fullKey[32:]

    digest = hashes.Hash(hashes.SHA256())
    digest.update(macKey)
    digest.update(ciphertext)
    
    if digest.finalize() != mac:
        print("MAC Validation Failed")
        return

    cipher = Cipher(algorithms.AES(key), modes.CBC(iv))
    decryptor = cipher.decryptor()
    padded_plaintext = decryptor.update(ciphertext) + decryptor.finalize()

    unpadder = padding.PKCS7(128).unpadder()
    plaintext = unpadder.update(padded_plaintext) + unpadder.finalize()

    with open(filename.replace('.enc', '') + '.dec', 'wb') as decFile:
        decFile.write(plaintext)
    print("Decryption successful")

def main():
    if len(sys.argv) < 3:
        return
    if sys.argv[1] == 'enc':
        encryptAESHMAC(sys.argv[2])
    elif sys.argv[1] == 'dec':
        decryptAESHMAC(sys.argv[2])

if __name__ == '__main__':
    main()