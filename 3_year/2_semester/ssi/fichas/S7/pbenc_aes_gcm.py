import os, sys, getpass
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC
from cryptography.hazmat.primitives.ciphers.aead import AESGCM

def deriveKey(password: bytes, salt: bytes) -> bytes:
    kdf = PBKDF2HMAC(
        algorithm=hashes.SHA256(),
        length=32, 
        salt=salt,
        iterations=1_200_000,
    )
    return kdf.derive(password)

def encryptAESGCM(filename):
    password = getpass.getpass("Pass-phrase: ").encode('utf-8')
    salt = os.urandom(16)
    key = deriveKey(password, salt)
    
    nonce = os.urandom(12) 
    aesgcm = AESGCM(key)
    
    with open(filename, 'rb') as file:
        plaintext = file.read()
        
    ciphertext = aesgcm.encrypt(nonce, plaintext, associated_data=None)
    
    with open(filename + '.enc', 'wb') as encFile:
        encFile.write(salt)
        encFile.write(nonce)
        encFile.write(ciphertext)

def decryptAESGCM(filename):        
    password = getpass.getpass("Pass-phrase: ").encode('utf-8')
    
    with open(filename, 'rb') as encFile:
        salt = encFile.read(16)
        nonce = encFile.read(12)
        ciphertext = encFile.read()
        
    key = deriveKey(password, salt)
    aesgcm = AESGCM(key)
    
    plaintext = aesgcm.decrypt(nonce, ciphertext, associated_data=None)
        
    out_filename = filename.replace('.enc', '') + '.dec'
    with open(out_filename, 'wb') as decFile:
        decFile.write(plaintext)

def main():
    if len(sys.argv) < 3:
        return
    if sys.argv[1] == 'enc':
        encryptAESGCM(sys.argv[2])
    elif sys.argv[1] == 'dec':
        decryptAESGCM(sys.argv[2])

if __name__ == '__main__':
    main()