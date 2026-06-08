import sys

def preproc(str):
    l = []
    for c in str:
        if c.isalpha():
            l.append(c.upper())
    return "".join(l)

def processCaeser(plaintext, lwords):
    
    for i in range(0, 26):
        ciphertext = list(plaintext)
        for j in range(len(plaintext)):
            ciphertext[j] = chr(((ord(ciphertext[j]) - 65 - i) % 26) + 65)
        cipher_set = set(ciphertext)
        for word in lwords:
            if set(word).issubset(cipher_set):
                return (chr(i + 65), ''.join(ciphertext))

    return ('', '')

def main():
    if len(sys.argv) < 2:
        print("Wrong usage")
        return -1

    lwords = list()
    for i in range(2, len(sys.argv)):
        lwords.append(preproc(sys.argv[i]))
    
    (x, y) = processCaeser(preproc(sys.argv[1]), lwords)
    print(x)
    print(y)

    return 0
    
if __name__ == '__main__':
    main()