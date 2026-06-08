#!/bin/bash

# Exercício 1
touch lisboa.txt
echo "text" > lisboa.txt

touch porto.txt
echo "text" > porto.txt

touch braga.txt
echo "text" > braga.txt

# Exercício 2
ls -l

# Exercício 3
chmod 666 lisboa.txt

# Exercício 4
chmod u=rx,u-w porto.txt

# Exercício 5
chmod u=r,u-wx braga.txt

# Exercício 6
mkdir dir1
mkdir dir2
ls -l

# Exercício 7
chmod g-x,o-x dir2/
