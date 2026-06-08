import sys
import random

def bad_prng(n):
    """ an INSECURE pseudo-random number generator """
    random.seed(random.randbytes(2))
    return random.randbytes(n)

def encryptOTP(plaintextPath, keyPath):
    with open(keyPath, 'rb') as keyFile:
        key = keyFile.read()

    with open(plaintextPath, 'r') as plaintextFile:
        plaintext = plaintextFile.read()

    ciphertext_bytes = bytes([a ^ b for a, b in zip(key, plaintext.encode())])

    with open(plaintextPath + '.enc', 'wb') as output:
        output.write(ciphertext_bytes)

def decryptOTP(ciphertextPath, keyPath):
    with open(keyPath, 'rb') as keyFile:
        key = keyFile.read()
    
    with open(ciphertextPath, 'rb') as ciphertextFile:
        ciphertext = ciphertextFile.read()

    plaintext = bytes([a ^ b for a, b in zip(key, ciphertext)])
    
    with open(ciphertextPath + '.dec', 'w') as output:
        output.write(plaintext.decode())

def main():
    if sys.argv[1] == 'setup':
        key = bad_prng(int(sys.argv[2]))
        with open(sys.argv[3], 'wb') as keyFile:
            keyFile.write(key)
    elif sys.argv[1] == 'enc':
        encryptOTP(sys.argv[2], sys.argv[3]) 
    elif sys.argv[1] == 'dec':
        decryptOTP(sys.argv[2], sys.argv[3]) 
    else:
        print("Wrong usage")

    return 0

if __name__ == '__main__':
    main()