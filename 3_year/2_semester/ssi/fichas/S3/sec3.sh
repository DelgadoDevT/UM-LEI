#!/bin/bash

# Exercício 1
# import sys
# if len(sys.argv) > 1:
# 	f = open(sys.argv[1], "r", encoding="utf-8")
#    	print(f.read(), end='')
# else:
#    	raise Exception("Usage ./myCat file")

# Exercício 2
sudo useradd userssi

# Exercício 3
sudo chown userssi braga.txt

# Exercício 4
python myCat.py braga.txt

# Exercício 5
chmod u+s myCat.py

# Exercício 6
python myCat.py braga.txt
# python: can't open file '/home/ssi/tp2/myCat.py': [Errno 13] Permission denied
# O Linux ignora o bit SetUID em scripts interpretados (como Python) por segurança, 
# logo não há elevação de privilégios.
