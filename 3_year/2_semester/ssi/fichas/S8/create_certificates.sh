#!/bin/bash

openssl genrsa -out CA.key 2048
openssl genrsa -out Alice.key 2048
openssl genrsa -out Bob.key 2048

openssl req -x509 -new -nodes -key CA.key -sha256 -days 365 -out CA.crt -subj "/CN=CA"
openssl req -new -key Alice.key -out Alice.csr -subj "/CN=Alice"
openssl x509 -req -in Alice.csr -CA CA.crt -CAkey CA.key -CAcreateserial -out Alice.crt -days 365 -sha256
openssl req -new -key Bob.key -out Bob.csr -subj "/CN=Bob"
openssl x509 -req -in Bob.csr -CA CA.crt -CAkey CA.key -CAcreateserial -out Bob.crt -days 365 -sha256
