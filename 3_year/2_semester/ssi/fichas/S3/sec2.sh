#!/bin/bash

# Exercício 0
cat /etc/passwd
cat /etc/group

# Exercício 1
sudo adduser user1
sudo adduser user2
sudo adduser user3

# Exercício 2
sudo groupadd grupo-ssi
sudo groupadd par-ssi
sudo groupmod grupo-ssi user1,user2,user3
sudo groupmod par-ssi user1,user2

# Exercício 3
cat /etc/passwd
cat /etc/group
# Os dados refertes aos novos grupos e utilizadores foram introduzidos nos ficheiros
# passwd e group

# Exercício 4
sudo chown user1 braga.txt

# Exercício 5
cat braga.txt

# Exercício 6
su user1

# Exercício 7
# Apenas é possível verificar os groupos e ids que de alguma fora se encontram dentro
# dos limites do user, neste caso os que ele está envolvido

# Exercício 8
# ao usar cat braga.txt, ocorre o erro permission denied

# Exercício 9
# ao usar cd dir2, ocorre o erro permission denied 
