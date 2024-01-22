# Theater Ticketing System API

API For Theater Ticket Reservation App

The API is implemented using Spring Boot. It is supposed to be used with the Theater Ticketing System Angular project as it's backend.

## Required User Input

Add Database Credentials, a Google OAuth2 Client ID and the credentials for an email address which the api will use to send user emails to application.properties

## Generate Public And Private Keys

`openssl genrsa -out keypair.pem 2048`

`openssl rsa -in keypair.pem -pubout -out public.pem`

`openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in keypair.pem -out private.pem`

Create folder certs in resources and add private.pem and public.pem 
