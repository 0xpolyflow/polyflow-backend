#!/bin/sh

openssl genrsa -out private.pem 2048
openssl pkcs8 -topk8 -inform PEM -in private.pem -out private_key.pem -nocrypt
