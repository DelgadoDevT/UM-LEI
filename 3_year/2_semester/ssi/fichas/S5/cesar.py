import sys

def preproc(str):
    l = []
    for c in str:
        if c.isalpha():
            l.append(c.upper())
    return "".join(l)

def processCaeser(mode, key, plaintext):
    ciphertext = list(plaintext)
    for i in range(0, len(plaintext)):
        if mode == 0:
            ciphertext[i] = (chr(((ord(ciphertext[i]) - 65 + (ord(key) - 65)) % 26) + 65))
        else:
            ciphertext[i] = (chr(((ord(ciphertext[i]) - 65 - (ord(key) - 65)) % 26) + 65))

    return "".join(ciphertext)

def main():
    if len(sys.argv) != 4:
        print("Wrong usage: python3 cesar.py enc/dec G 'Ola'")
        return 1
    
    plaintext = preproc(sys.argv[3])
    ciphertext = ''
    if sys.argv[1] == 'enc':
        ciphertext = processCaeser(0, sys.argv[2], plaintext) 
    elif sys.argv[1] == 'dec':
        ciphertext = processCaeser(1, sys.argv[2], plaintext)
    else:
        print("Wrong usage: python3 cesar.py enc/dec G 'Ola'")

    print(ciphertext)
    return 0

if __name__ == "__main__": 
    main()
