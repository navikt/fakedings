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
[Sample token](https://jwt.io?token=eyJraWQiOiJtb2NrLW9hdXRoMi1zZXJ2ZXIta2V5IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJhdF9oYXNoIjoiZDFjY2Q1NTItYjVkZi00YTVlLTgwNGMtMDg2MDE1NmZkM2UyIiwic3ViIjoiYmNkMzcyNzMtMDQ2Ni00MTEzLWJjYjgtMmVhNzFlODk2N2JhIiwiYW1yIjpbIkJhbmtJZCJdLCJpc3MiOiJodHRwczpcL1wvZmFrZWRpbmdzLmRldi1nY3AubmFpcy5pb1wvZmFrZSIsInBpZCI6Im5vdGZvdW5kIiwibG9jYWxlIjoibmIiLCJzaWQiOiJmNTc4YmRkOS00ZjVlLTQxNjktYjViOS1lOTU0YTM3MGRlYTIiLCJhdWQiOiJub3Rmb3VuZCIsImFjciI6IkxldmVsNCIsIm5iZiI6MTYwOTgzNzM5MSwiYXV0aF90aW1lIjoxNjA5ODM3MzkxLCJleHAiOjE2MTM0MzczOTEsImlhdCI6MTYwOTgzNzM5MSwianRpIjoiNWM4MmNmNTEtYTI1My00MTQwLWEzY2ItM2Y3MGZiMTc3ZTJiIn0.Mla894p6tuoir17CGd3sBCd5so0bht-8qBJ2GGq7ARX-RF5ZNVY_4nsUopWvlqp1sF7k48CM4uPeX5KG9RFxy5xgN3z-w-eJAffgBQLj6J2DxPHQ54PXKNC10d1MjtUlTJUeURtfY6M6ixIUAJZMT8wWThJEx-kr376CplcCackcSY8opOAFS6UsOx1KITiXy8qOjByNOE_S8CcB29cq3pHOcxPtukJIgXPaLPO84z2l3I48alSowHrLRVsf391hCGokKG0GsDA-PrIZED9O4INkdAEPz9ii4_0OsBYkp6JD9KCR8AznV-L_G1qLmlsSKAArcHWEgyUC2vuR3A5q4A)

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

[Sample token](https://jwt.io?token=eyJraWQiOiJtb2NrLW9hdXRoMi1zZXJ2ZXIta2V5IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiIwY2Y0MzQzMi1lZWRkLTQyOWUtYmY5MS1iM2E1NzQ2ZTgyNTgiLCJzY3AiOiJVc2VyLlJlYWQiLCJ2ZXIiOiIyLjAiLCJhaW8iOiJkZjcxMWIyNy0xOWZiLTQzYTQtOGY4OS0yOWUyNTA4YTYzMWIiLCJhenBhY3IiOiIxIiwiaXNzIjoiaHR0cHM6XC9cL2Zha2VkaW5ncy5kZXYtZ2NwLm5haXMuaW9cL2Zha2UiLCJvaWQiOiI4OGIxY2E1NS04YmQ0LTQ2NDEtYjFiZC1mMDA0YWNmNjQ1ZmQiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJub3Rmb3VuZCIsImF1ZCI6Im5vdGZvdW5kIiwibmJmIjoxNjA5ODM3NjI0LCJhenAiOiJjbGllbnQgaWQgcMOlIGRlbiBzb20gc3DDuHIiLCJuYW1lIjoibm90Zm91bmQiLCJleHAiOjE2MTM0Mzc2MjQsImlhdCI6MTYwOTgzNzYyNCwianRpIjoiMzYxNzNhMGQtZjUwMi00MmE5LTliY2QtOGZkY2I4MTVkN2NkIn0.dTVyG7aXiNaTiwiYZnZEcvx2kAHu8xyM6EjGx4-Xl6Lh-u3G9jXLwfJvOCMDyYJTpP8AMdnndjsI-yiQEroU2qds2QCHbRTR7ZDXCmjbslk-u-yLF-6f6y1xen749hSultAtCoY5UGfWT9et5368UqFgs2x3mbqZQQ3DnetAeBX9RIUkjugObaJo30kdWj4oduopIYiD0H7kyeCKGxZDbc0LElNQP8kt6RjeMe13HdHYNeXZWuQNSCImMr1R-AdQ2XR_uBDXGDCC7mnyW3ONLQL9BUS4D80qHXNtCWlWD09XsI9k8FOBT_kD3BeIoXA8jD4K-9W4fmaU2_K_8K6eqQ)

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
[Sample token](https://jwt.io?token=)

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
[Sample token](https://jwt.io?token=eyJraWQiOiJtb2NrLW9hdXRoMi1zZXJ2ZXIta2V5IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJhdWQiOiJteWF1ZGllbmNlIiwic3ViIjoibXlzdWIiLCJuYmYiOjE2MDk4Mzg2NjksIm15Y3VzdG9tIjoiY3VzdG9tMSIsImlzcyI6Imh0dHBzOlwvXC9mYWtlZGluZ3MuZGV2LWdjcC5uYWlzLmlvXC9mYWtlIiwiZXhwIjoxNjEzNDM4NjY5LCJpYXQiOjE2MDk4Mzg2NjksImp0aSI6Ijk1NDVmNzllLTA1OWUtNDRmZi1hMmFhLTAwZDNmM2QyYzAyMSJ9.E5-uXUWs6hqpHWXHcv-hC3ZmRqmcCesCxZ7udK2NLGg7uQS6m6JCp29h9dcyqJ_ZWY1SzOyF_NSZsgfAkuo4aPvVa1wK1-PaR3ys_WjdX9UG2NEdnyO5WzHFuPbM8TLEX78yjPRHcCiJxtc3OzLUqSl01yKJXYgAA8X4zcLqbrFPygfJJ-dC1LgBQrAaImsFoRWW5cgYqwk51DIBXeSQfVjvSu7TWO3t0G8R6dUl23ivzPLb5NWNII9FWHNdXPOrrT6xG-gjTu8MrfofwLml8sJD8r7EmgVrp2fKbCFwpYWA_PaEdYHEhZ_LiNUvdn7ohTTGbkkckhk_uJQJEBmGUA)

### OpenID Connect discovery
https://fakedings.intern.dev.nav.no/fake/.well-known/openid-configuration

