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

[CONFIRMED] `OAuth2SuccessHandler`의 login 성공 log에는 token 실제 값이 포함되지 않습니다. Focused test는 refresh token 값이 log에 포함되지 않으며 기존 Redis 저장, refresh-token cookie와 `isNewUser` redirect가 유지되고 access token이 redirect에 포함되거나 OAuth success 단계에서 생성되지 않는 것을 검증합니다.

[CONFIRMED] OAuth2 success handler는 access token을 생성하거나 frontend callback URL에 전달하지 않습니다. Frontend는 callback에서 refresh endpoint를 호출해 access token을 획득합니다.

[CONFIRMED] `SecurityConfig`는 `anyRequest().permitAll()`로 끝납니다. Authorization은 각 protected route가 해당 rule 앞에 명시적으로 나열되는지에 달려 있습니다.

[CONFIRMED] `POST /api/categories`는 HTTP method와 exact path 기준의 `denyAll` matcher로 User Server의 anonymous 및 authenticated `ROLE_USER` 요청을 모두 차단합니다. Characterization test에서 각각 401과 403을 확인했습니다. `GET /api/categories`는 PUBLIC으로 유지됩니다.

[CONFIRMED] Category 생성 code는 삭제하지 않았으며 Category 관리 기능은 향후 별도 관리자 서버로 이전할 예정입니다. [UNKNOWN] 초기 Category master data 공급 방식은 아직 결정되지 않았습니다.

[CONFIRMED] Reading Goal GET/PUT, Reading Progress GET/PUT/complete POST, Profile/Profile Image PATCH와 Monthly Sales GET은 HTTP method와 구체적인 path pattern 기준으로 authentication을 요구합니다. Monthly Sales Service는 Book 존재 여부를 먼저 확인하고 현재 사용자와 Book owner가 다르면 전용 403 ErrorCode로 거부합니다.

[CONFIRMED] `SecurityConfig` characterization test는 Reading Goal, Reading Progress, Profile 수정과 Monthly Sales endpoint의 authentication, Category POST 비활성화 및 PUBLIC Category GET을 검증하며 anonymous `GET /api/books`, `GET /api/books/{bookId}`와 `GET /api/user/{userId}/profile`이 계속 허용되는 것을 확인합니다.

[CONFIRMED] 모든 authenticated user에는 `ROLE_USER`가 부여됩니다. 다른 role 및 method-level authorization은 발견되지 않았습니다.

[CONFIRMED] Presigned upload request에는 filename과 content type의 `@NotBlank` validation만 있습니다. MIME allowlist, upload size, quota 검증은 발견되지 않았고 request의 `contentType`은 presigned request 생성에 사용되지 않습니다.

[CONFIRMED] Legacy `.github/workflows/deploy.yml`은 self-hosted runner host의 `.env`를 workflow workspace로 복사하도록 구성되어 있습니다. 현재 등록된 self-hosted runner가 없어 workflow는 Queued 상태가 됩니다. 새 `.github/workflows/ci.yml`은 GitHub-hosted runner에서 Secret 주입 없이 build/test만 수행합니다. Repository에는 AWS managed-secret integration 또는 다른 production secret-management implementation이 없습니다.

## Production release gate

[CONFIRMED] 승인된 `AGENTS.md` 운영 규칙은 production deployment 전에 secret/token logging 제거, 노출 가능 credential의 rotation 필요 여부, endpoint authorization, object-storage 공개 범위, secret value를 포함하지 않은 verification result를 검토하도록 요구합니다.

[CONFIRMED] 해결되지 않은 중대한 security issue가 있으면 production deployment를 진행하지 않고 사람에게 보고해야 합니다. 실제 credential rotation 또는 production setting 변경에는 별도의 사람 authorization이 필요합니다.

## 미확인 사항

[UNKNOWN] Secret/credential rotation status, legacy self-hosted runner host에 남아 있을 수 있는 `.env` cleanup, provider-console configuration, production TLS termination, WAF rule, network control, log retention/access control, incident response process, dependency vulnerability status는 이 repository에서 확정할 수 없습니다.
