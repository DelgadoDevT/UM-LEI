import os, sys
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes

def setupKey(filename):
    key = os.urandom(32)
    with open(filename, 'wb') as file:
        file.write(key)

def encryptAES_CTR(filename, fileKeyName):
    with open(fileKeyName, 'rb') as fileKey:
        key = fileKey.read()
   
    iv = os.urandom(16)
    cipher = Cipher(algorithms.AES(key), modes.CTR(iv))
    encryptor = cipher.encryptor()
    
    with open(filename, 'rb') as file:
        plaintext = file.read()

    ciphertext = encryptor.update(plaintext) + encryptor.finalize()
    
    with open(filename + '.enc', 'wb') as encFile:
        encFile.write(iv)
        encFile.write(ciphertext)

def decryptAES_CTR(filename, fileKeyName):
    with open(fileKeyName, 'rb') as fileKey:
        key = fileKey.read()
    
    with open(filename, 'rb') as encFile:
        iv = encFile.read(16)
        ciphertext = encFile.read()

    cipher = Cipher(algorithms.AES(key), modes.CTR(iv))
    decryptor = cipher.decryptor()

    plaintext = decryptor.update(ciphertext) + decryptor.finalize()

    with open(filename + '.dec', 'wb') as decFile:
        decFile.write(plaintext) 

def main():
    if sys.argv[1] == 'setup':
        setupKey(sys.argv[2])
    elif sys.argv[1] == 'enc':
        encryptAES_CTR(sys.argv[2], sys.argv[3])
    elif sys.argv[1] == 'dec':
        decryptAES_CTR(sys.argv[2], sys.argv[3])

if __name__ == '__main__':
    main()