# ticket_api

API For Theater Ticket Reservation App

## Required User Input

Add Database Credentials And Google OAuth2 Client ID To application.properties

## Generate Public And Private Keys

'openssl genrsa -out keypair.pem 2048'

'openssl rsa -in keypair.pem -pubout -out public.pem'

'openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in keypair.pem -out private.pem'

Create folder certs in resources and add private.pem and public.pem 
