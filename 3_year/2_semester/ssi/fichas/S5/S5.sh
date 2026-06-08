#!/bin/bash

# Questão 1
# O otp.py gera chaves seguras e imprevisíveis, enquanto o bad_otp.py gera chaves a 
# partir de apenas 65536 sementes possíveis (2^16), tornando a cifra previsível e 
# vulnerável a brute force.

# Questão 2
# Não, pois a segurança perfeita do OTP exige que a chave seja gerada de forma puramente 
# aleatória e não de forma pseudo-aleatória com um espaço de sementes minúsculo.

# Questão 3
# Ao fazer o XOR dos dois criptogramas, a chave anula-se, resultando diretamente no XOR das 
# duas mensagens originais (((M1​ xor K) xor (M2​ xor K)) = M1​ xor M2)​. O ataque torna-se extremamente 
# fácil utilizando técnicas de previsão de palavras, que aplicadas em uma das palavras se estiver
# correta revela parte da segunda palavra.

# As restantes questões encontram-se respondidas sobre formato de código na diretoria