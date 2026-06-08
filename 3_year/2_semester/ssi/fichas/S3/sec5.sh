#!/bin/bash

# Exercício 1
capsh --print

# Exercício 2
./webserver 4050

# Exercício 3
./webserver 80
# Errr on bind: Permission denied
# Esta porta é uma well known port 0-1023.
# O programa não tem a capabilitie "CAP_NET_BIND_SERVICE" responsável por permitir 
# binding a portas TCP/UDP < 1024
# Para executar o programa com sucesso poderia-se garantir o acesso a capabilitie
# "CAP_NET_BIND_SERVICE" o que pode ser feito com o comando:
sudo setcap cap_net_bind_service=+ep webserver
