#!/bin/bash

# Exercício 1
getfacl porto.txt

# Exercício 2
setfacl -m g:grupo-ssi:w porto.txt

# Exercício 3
getfacl porto.txt
# foi criada uma mascara mask::rw-  e adicionada permissão solicitada group:grupo-ssi:-w- ao ficheiro

# Exercício 4
su user1
cat porto.txt
echo "ola" >> porto.txt
# Ao definir a permissão apenas como 'w' (write), o utilizador consegue alterar o ficheiro (escrever), 
# mas o comando de leitura falha com 'Permissão Negada' porque falta a flag 'r' (read).
