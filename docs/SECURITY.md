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

[CONFIRMED] `OAuth2SuccessHandler`의 login 성공 log에서 access token과 refresh token 실제 값 출력은 제거됐습니다. Focused test는 token 값이 log에 포함되지 않으며 기존 Redis 저장, refresh-token cookie, frontend redirect와 `isNewUser` 전달이 유지되는 것을 검증합니다.

[CONFIRMED] OAuth2 success handler는 access token을 frontend callback URL의 query parameter로 전달합니다.

[CONFIRMED] `SecurityConfig`는 `anyRequest().permitAll()`로 끝납니다. Authorization은 각 protected route가 해당 rule 앞에 명시적으로 나열되는지에 달려 있습니다.

[CONFIRMED] `POST /api/categories`는 `SecurityConfig`의 명시적인 authenticated matcher에 포함되어 있지 않습니다.

[CONFIRMED] `/api/reading-goals`, `/api/books/{bookId}/reading-progress`, `/api/books/{bookId}/reading-progress/complete`도 명시적인 authenticated matcher에 포함되어 있지 않습니다. 해당 controller는 `@AuthenticationPrincipal`을 사용하지만 route는 `anyRequest().permitAll()` default에 남아 있습니다.

[CONFIRMED] P0-1 `SecurityConfig` characterization test는 `/api/reading-goals`가 현재 anonymous request를 허용하는 상태를 확인합니다. 이 test는 현재 behavior를 기록하는 최소 안전망이며 authorization gap을 해결하지 않습니다.

[CONFIRMED] 모든 authenticated user에는 `ROLE_USER`가 부여됩니다. 다른 role 및 method-level authorization은 발견되지 않았습니다.

[CONFIRMED] Presigned upload request에는 filename과 content type의 `@NotBlank` validation만 있습니다. MIME allowlist, upload size, quota 검증은 발견되지 않았고 request의 `contentType`은 presigned request 생성에 사용되지 않습니다.

[CONFIRMED] Legacy `.github/workflows/deploy.yml`은 self-hosted runner host의 `.env`를 workflow workspace로 복사하도록 구성되어 있습니다. 현재 등록된 self-hosted runner가 없어 workflow는 Queued 상태가 됩니다. 새 `.github/workflows/ci.yml`은 GitHub-hosted runner에서 Secret 주입 없이 build/test만 수행합니다. Repository에는 AWS managed-secret integration 또는 다른 production secret-management implementation이 없습니다.

## Production release gate

[CONFIRMED] 승인된 `AGENTS.md` 운영 규칙은 production deployment 전에 secret/token logging 제거, 노출 가능 credential의 rotation 필요 여부, endpoint authorization, object-storage 공개 범위, secret value를 포함하지 않은 verification result를 검토하도록 요구합니다.

[CONFIRMED] 해결되지 않은 중대한 security issue가 있으면 production deployment를 진행하지 않고 사람에게 보고해야 합니다. 실제 credential rotation 또는 production setting 변경에는 별도의 사람 authorization이 필요합니다.

## 미확인 사항

[UNKNOWN] Secret/credential rotation status, legacy self-hosted runner host에 남아 있을 수 있는 `.env` cleanup, provider-console configuration, production TLS termination, WAF rule, network control, log retention/access control, incident response process, dependency vulnerability status는 이 repository에서 확정할 수 없습니다.
