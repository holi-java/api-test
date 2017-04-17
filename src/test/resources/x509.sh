#!/bin/bash

#######################################################
## Remove all generated files                        ##
#######################################################

rm -f server.key server.crt keystore.p12 keystore.jks

#######################################################
## generate x509 certificate & private key           ##
#######################################################

openssl req -new -x509 -nodes -keyout server.key -sha256 -out server.crt -days 730 -config openssl.cnf
openssl x509 -in server.crt -text -noout

#######################################################
## adding self-signed certificate into jks key store ##
#######################################################

PASSWORD="password"
openssl pkcs12 -export -name test -in server.crt -inkey server.key -out keystore.p12 -password "pass:$PASSWORD"
keytool -importkeystore -destkeystore keystore.jks -srckeystore keystore.p12 -srcstorepass $PASSWORD -srcstoretype pkcs12 -alias test -storepass $PASSWORD

#######################################################
## Remove pkcs12 keystore & private key              ##
#######################################################
rm -f keystore.p12  server.key
