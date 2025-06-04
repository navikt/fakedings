# fakedings - for local development

If in need of fake tokens, tailored for your needs, look no further!

fakedings is a fake OAuth2 authorization server issuing tokens without any security, made specifically for local development.
It is a wrapper around the https://github.com/navikt/mock-oauth2-server, providing additional endpoints for known NAV use cases in terms of tokens. 

> ⚠️ **DO NOT USE IN PRODUCTION**

## API endpoints

#### HTTP GET - Fake IdPorten token 

https://fakedings.intern.dev.nav.no/fake/idporten

``` http request
GET /fake/idporten
    ?pid=12345678910
    &acr=idporten-loa-high

Host: fakedings.intern.dev.nav.no
```
Response body:
``` 
eyJraWQiOiJtb2NrLW9hdXRoM......
```    

#### HTTP GET - Fake Azure AD token

https://fakedings.intern.dev.nav.no/fake/aad

``` http request
GET /fake/aad
    ?preferred_username=user@email.com
    &name=You Only Live Once
    &azp=consumer-client-id
    &aud=receiver-client-id

Host: fakedings.intern.dev.nav.no
```
Response body:
``` 
eyJraWQiOiJtb2NrLW9hdXRoM......
```    

#### HTTP GET - Fake TokenX token

https://fakedings.intern.dev.nav.no/fake/tokenx

``` http request
GET /fake/tokenx
    ?client_id=someclientid
    &aud=dev-gcp:targetteam:targetapp
    &acr=Level4
    &pid=12345678910

Host: fakedings.intern.dev.nav.no
```
Response body:
``` 
eyJraWQiOiJtb2NrLW9hdXRoM......
```    

#### HTTP POST - Fake any token, i.e. you can post any claims you want

https://fakedings.intern.dev.nav.no/fake/custom

Any form parameter sent in the application/x-www-form-urlencoded request will be added as a claim in the resulting token.

``` http request
POST /fake/custom

Host: fakedings.intern.dev.nav.no
Content-Type: application/x-www-form-urlencoded

sub=mysub&
aud=myaudience&
mycustom=custom1
....
```
Response body:
``` 
eyJraWQiOiJtb2NrLW9hdXRoM......
```    

### OpenID Connect discovery
https://fakedings.intern.dev.nav.no/fake/.well-known/openid-configuration

