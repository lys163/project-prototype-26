# Security 규칙

## 확인된 상태

- Kakao 및 Naver OAuth2 login이 구성되어 있습니다.
- JWT bearer access token과 Redis 기반 refresh token이 구현되어 있습니다.
- refresh token은 HttpOnly, Secure, SameSite=Lax cookie를 사용합니다.
- MinIO initialization code는 구성된 bucket에 public read access를 적용합니다.
- 기존 configuration에는 commit된 plaintext secret material이 있고 OAuth success handling은 access/refresh token을 logging합니다. 해당 value를 절대 재현하지 않습니다.
- `SecurityConfig`는 `anyRequest().permitAll()`로 끝납니다.

## 규칙

1. secret, token, password, private endpoint, credential, webhook URL을 절대로 출력하거나 commit하거나 documentation에 기록하거나 logging하지 않습니다.
2. 기능을 구현하기 위해 authentication, authorization, CORS, cookie, TLS, validation, storage policy를 약화하지 않습니다.
3. 모든 새 endpoint에는 명시적인 authorization 결정을 부여하고 test합니다.
4. public-read object storage를 새 asset의 기본값이 아닌 design/security 결정으로 취급합니다.
5. authentication 변경 시 OAuth redirect, CORS, JWT behavior, cookie attribute, Redis refresh-token behavior를 함께 검토합니다.
6. token logging과 commit된 secret material을 sensitive value를 반복하지 않고 finding으로 보고합니다.
