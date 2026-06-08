#!/bin/bash

# Exercício 1
gcc -o backupssi backupssi.c
sudo chown root:root backupssi
chmod 4755 backupssi
./backupssi

# Exercício 2
# Ocorre um "leak" porque o descritor de ficheiro para /root não é encerrado 
# antes de a shell ser iniciada.

# Exercício 3
gcc -o exploit_capability_leaking exploit_capability_leaking.c
./exploit_capability_leaking

# Exercício 4
# Para mitigar o problema pode-se fechar o file descriptor com a função "close(dfd)" 
# imediatamente antes de invocar o execve para a abertura de um terminal
