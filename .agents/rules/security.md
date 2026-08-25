# Security 규칙

## 확인된 상태

- Kakao 및 Naver OAuth2 login이 구성되어 있습니다.
- JWT bearer access token과 Redis 기반 refresh token이 구현되어 있습니다.
- refresh token은 HttpOnly, Secure, SameSite=Lax cookie를 사용합니다.
- MinIO initialization code는 구성된 bucket에 public read access를 적용합니다.
- 현재 tracked application/Compose configuration의 credential은 environment reference로 외부화되어 있으며 local value는 ignored `.env`에서 제공합니다. 과거 Git history에 노출됐을 수 있는 credential의 rotation 여부는 확인되지 않았습니다.
- OAuth success handling은 token 실제 값을 logging하거나 callback query로 access token을 전달하지 않습니다.
- `SecurityConfig`는 의도된 PUBLIC endpoint를 명시하고 `anyRequest().denyAll()`로 끝납니다.

## 규칙

1. secret, token, password, private endpoint, credential, webhook URL을 절대로 출력하거나 commit하거나 documentation에 기록하거나 logging하지 않습니다.
2. 기능을 구현하기 위해 authentication, authorization, CORS, cookie, TLS, validation, storage policy를 약화하지 않습니다.
3. 모든 새 endpoint에는 명시적인 authorization 결정을 부여하고 test합니다.
4. public-read object storage를 새 asset의 기본값이 아닌 design/security 결정으로 취급합니다.
5. authentication 변경 시 OAuth redirect, CORS, JWT behavior, cookie attribute, Redis refresh-token behavior를 함께 검토합니다.
6. 과거 token logging 및 commit된 secret material의 history/rotation risk를 sensitive value를 반복하지 않고 finding으로 보고합니다.
7. Frontend가 소비하는 OAuth redirect/callback, token 전달, refresh/cookie, Bearer 인증, CORS/credentials/frontend URL 또는 endpoint authorization contract를 변경할 때는 실제 Frontend auth flow와 config를 선택적으로 함께 조사합니다. Backend 내부 security 구현 세부만 변경되고 외부 contract가 유지되면 Frontend 조사는 필요하지 않습니다.
