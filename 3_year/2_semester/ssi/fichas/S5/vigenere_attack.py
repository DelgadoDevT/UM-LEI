import sys
import string 
from itertools import product

def preproc(str):
    l = []
    for c in str:
        if c.isalpha():
            l.append(c.upper())
    return "".join(l)

def processKey(preKey, text):
    key = preKey
    while len(key) < len(text):
        key = key + preKey    
    return key

def processVigenere(text, keysize, lwords):
    li = string.ascii_uppercase
    keys = (''.join(key) for key in product(li, repeat=keysize))
    
    for key in keys:
        tkey = processKey(key, text) 
        ptext = list(text)
        for i in range(len(ptext)):
            ptext[i] = chr(((ord(ptext[i]) - 65 - (ord(tkey[i]) - 65)) % 26) + 65)
        
        decrypted_string = "".join(ptext)
        
        for word in lwords:
            if word in decrypted_string:
                return (key, decrypted_string)

    return ('', '')

def main():
    if len(sys.argv) < 3:
        print("Wrong usage")
        return -1

    lwords = list()
    for i in range(3, len(sys.argv)):
        lwords.append(preproc(sys.argv[i]))
    
    (x, y) = processVigenere(preproc(sys.argv[2]), int(sys.argv[1]), lwords)
    print(x)
    print(y)

    return 0
    
if __name__ == '__main__':
    main()