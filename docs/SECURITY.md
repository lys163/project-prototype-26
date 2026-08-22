# Security

## Authentication 및 session model

[CONFIRMED]

- application configuration에 구성된 OAuth2 provider는 Kakao 및 Naver입니다.
- server는 stateless로 구성되며 form login과 HTTP Basic authentication은 비활성화되어 있습니다.
- Access token은 `Authorization: Bearer` header로 전송되는 JWT입니다.
- OAuth2 성공 시 refresh token은 user ID에서 도출된 key 아래에 Redis에 저장됩니다.
- refresh token은 HttpOnly, Secure, SameSite=Lax cookie로 발급됩니다.
- refresh endpoint는 JWT를 validate하고 Redis value와 비교합니다.

## CORS

[CONFIRMED] CORS는 `app.frontend-url`의 단일 origin 및 credential을 허용하고, GET/POST/PUT/DELETE/OPTIONS/PATCH를 permit하며, `X-Request-Id`를 노출합니다.

## Storage access

[CONFIRMED] storage module은 time-limited presigned upload URL을 발급합니다. MinIO initialization service는 구성된 bucket에 public-read policy를 적용합니다.

## 확인된 security finding

[CONFIRMED] `docker-compose-dev.yml`에는 local development 관련 service 및 OAuth/JWT 관련 configuration을 위한 commit된 plaintext credential/secret이 포함되어 있습니다. Secret value는 이 document에 의도적으로 재현하지 않습니다.

[CONFIRMED] `OAuth2SuccessHandler`는 login 성공 후 access token과 refresh token을 모두 logging합니다.

[CONFIRMED] `SecurityConfig`는 `anyRequest().permitAll()`로 끝납니다. Authorization은 각 protected route가 해당 rule 앞에 명시적으로 나열되는지에 달려 있습니다.

[CONFIRMED] `POST /api/categories`는 `SecurityConfig`의 명시적인 authenticated matcher에 포함되어 있지 않습니다.

## 미확인 사항

[UNKNOWN] Secret rotation status, provider-console configuration, production TLS termination, WAF rule, network control, log retention/access control, incident response process, dependency vulnerability status는 이 repository에서 확정할 수 없습니다.
