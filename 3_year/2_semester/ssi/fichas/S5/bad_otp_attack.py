import sys
import random

def decryptOTP(ciphertext_bytes, key):
    return bytes([a ^ b for a, b in zip(key, ciphertext_bytes)]).decode('utf-8', errors='ignore')

def main():
    lwords = list()
    for i in range(3, len(sys.argv)):
        lwords.append(sys.argv[i])

    with open(sys.argv[2], 'rb') as f:
        ciphertext_bytes = f.read()

    for seed in range(65536):
        random.seed(seed.to_bytes(2, byteorder='big'))
        key = random.randbytes(int(sys.argv[1]))

        plaintext = decryptOTP(ciphertext_bytes, key)

        for word in lwords:
            if word in plaintext:
                print(plaintext)
                return 0

    return -1

if __name__ == '__main__':
    main()