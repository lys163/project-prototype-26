# Security

## Authentication 및 session model

[CONFIRMED]

- application configuration에 구성된 OAuth2 provider는 Kakao 및 Naver입니다.
- server는 stateless로 구성되며 form login과 HTTP Basic authentication은 비활성화되어 있습니다.
- Access token은 `Authorization: Bearer` header로 전송되는 JWT입니다.
- OAuth2 성공 시 refresh token은 user ID에서 도출된 key 아래에 Redis에 저장됩니다.
- refresh token은 HttpOnly, Secure, SameSite=Lax cookie로 발급됩니다.
- refresh endpoint는 JWT를 validate하고 Redis value와 비교합니다.
- logout endpoint는 access token 없이 호출할 수 있으며, 요청 refresh token이 유효하고 Redis의 현재 value와 exact match일 때만 해당 value를 원자적으로 삭제합니다. Cookie 부재·invalid·expired·mismatch는 다른 refresh session에 영향을 주지 않고 idempotent하게 cookie를 만료시킵니다.
- 현재 producer/consumer가 없는 legacy `token` cookie cleanup은 제거됐습니다. Logout은 `refreshToken` cookie만 처리합니다.

[INFERRED] OAuth2 authorization request 과정에서는 Spring Security의 default session-backed state로 JSESSIONID가 사용될 수 있지만, 일반 API의 SecurityContext 인증은 JWT bearer와 Redis refresh token 구조입니다. JSESSIONID lifecycle을 명시적으로 정리하는 설정은 없습니다.

## CORS

[CONFIRMED] CORS는 `app.frontend-url`의 단일 origin 및 credential을 허용하고, GET/POST/PUT/DELETE/OPTIONS/PATCH를 permit하며, `X-Request-Id`를 노출합니다.

## Storage access

[CONFIRMED] storage module은 authenticated user에게 600초 presigned POST form을 발급합니다. MinIO initialization service는 확정된 public image 정책에 따라 구성된 bucket에 anonymous `GetObject` public-read policy를 적용합니다.

## 확인된 security finding

[CONFIRMED] tracked `application.properties` 및 Compose configuration의 plaintext credential material은 제거되었습니다. Local development credential은 ignored `.env`에서 제공하며 tracked `.env.example`에는 variable name만 있습니다. Historical exposure의 rotation 여부는 별도 운영 follow-up입니다.

[CONFIRMED] Development Compose의 Redis port는 host loopback `127.0.0.1:6379`에만 publish됩니다. [UNKNOWN] Production Redis authentication, TLS, network boundary 및 persistence 정책은 repository에서 확정할 수 없습니다.

[CONFIRMED] `OAuth2SuccessHandler`의 login 성공 log에는 token 실제 값이 포함되지 않습니다. Focused test는 refresh token 값이 log에 포함되지 않으며 기존 Redis 저장, refresh-token cookie와 `isNewUser` redirect가 유지되고 access token이 redirect에 포함되거나 OAuth success 단계에서 생성되지 않는 것을 검증합니다.

[CONFIRMED] OAuth2 success handler는 access token을 생성하거나 frontend callback URL에 전달하지 않습니다. Frontend는 callback에서 refresh endpoint를 호출해 access token을 획득합니다.

[CONFIRMED] `SecurityConfig`는 의도된 PUBLIC GET endpoint를 method/path별 `permitAll` matcher로 명시하고 `anyRequest().denyAll()`로 끝납니다. 명시적인 authorization rule과 일치하지 않는 새 request는 anonymous 401, authenticated 403으로 차단됩니다.

[CONFIRMED] `POST /api/categories`는 HTTP method와 exact path 기준의 `denyAll` matcher로 User Server의 anonymous 및 authenticated `ROLE_USER` 요청을 모두 차단합니다. Characterization test에서 각각 401과 403을 확인했습니다. `GET /api/categories`는 PUBLIC으로 유지됩니다.

[CONFIRMED] Category 생성 code는 삭제하지 않았으며 Category 관리 기능은 향후 별도 관리자 서버로 이전할 예정입니다. [UNKNOWN] 초기 Category master data 공급 방식은 아직 결정되지 않았습니다.

[CONFIRMED] Reading Goal GET/PUT, Reading Progress GET/PUT/complete POST, Profile/Profile Image PATCH와 Monthly Sales GET은 HTTP method와 구체적인 path pattern 기준으로 authentication을 요구합니다. Monthly Sales Service는 Book 존재 여부를 먼저 확인하고 현재 사용자와 Book owner가 다르면 전용 403 ErrorCode로 거부합니다.

[CONFIRMED] `SecurityConfig` characterization test는 Reading Goal, Reading Progress, Profile 수정과 Monthly Sales endpoint의 authentication, Category POST 비활성화, 17개 explicit PUBLIC GET endpoint와 public user profile의 anonymous 허용을 검증합니다. Matcher와 일치하지 않는 test-only endpoint는 anonymous 401, authenticated 403으로 차단되는 것도 확인합니다.

[CONFIRMED] 모든 authenticated user에는 `ROLE_USER`가 부여됩니다. 다른 role 및 method-level authorization은 발견되지 않았습니다.

[CONFIRMED] Presigned upload는 JPEG/PNG/WebP exact MIME allowlist, 1 byte~5 MiB policy, current-user object namespace를 적용합니다. MinIO POST policy가 exact key와 Content-Type 및 `content-length-range`를 서명하므로 client가 선언한 `fileSize`와 다른 oversized body를 보내도 Storage가 거부할 수 있는 구조입니다.

[CONFIRMED] 새 profile image는 현재 사용자의 configured Storage URL만 허용합니다. 기존 OAuth provider external image URL은 DB 값과 완전히 동일하게 유지하는 경우에만 허용합니다. null/blank profile image의 기존 허용 동작은 유지합니다.

[CONFIRMED] 사용자 quota/rate limit과 old/failed/orphan image cleanup은 이 Storage 변경에 포함되지 않았습니다.

[CONFIRMED] Legacy `.github/workflows/deploy.yml`은 self-hosted runner host의 `.env`를 workflow workspace로 복사하도록 구성되어 있습니다. 새 `.github/workflows/ci.yml`은 GitHub-hosted runner에서 Secret 주입 없이 build/test만 수행합니다. Repository에는 AWS managed-secret integration 또는 다른 production secret-management implementation이 없습니다. 현재 self-hosted runner 및 원격 workflow 상태는 repository만으로 확인할 수 없습니다.

## AUTH_SECURITY 후속 작업

[FUTURE]

- Refresh Token Rotation 및 replay detection
- 명시적인 multi-device/session 정책
- Frontend access token localStorage 개선과 memory 전환 검토
- OAuth 과정의 JSESSIONID lifecycle 정리
- CSRF 보강
- refresh/logout의 Origin/Referer 검증 정책

[CONFIRMED] 현재 refresh는 Redis refresh token을 회전시키지 않고 access token만 재발급합니다. User별 Redis key 하나를 사용하므로 새 login이 기존 value를 덮어쓸 수 있지만 이를 제품의 multi-device/session 정책으로 확정한 문서는 없습니다.

## Credential history 및 rotation

[CONFIRMED] 현재 tracked source/config의 plaintext credential 제거는 완료됐습니다.

[UNKNOWN] 과거 Git history에 노출됐을 수 있는 credential의 실제 rotation, provider-side revoke/reissue, legacy runner workspace cleanup 여부는 repository에서 확인할 수 없습니다. Git history rewrite도 수행된 근거가 없습니다.

## Production release gate

[CONFIRMED] 승인된 `AGENTS.md` 운영 규칙은 production deployment 전에 secret/token logging 제거, 노출 가능 credential의 rotation 필요 여부, endpoint authorization, object-storage 공개 범위, secret value를 포함하지 않은 verification result를 검토하도록 요구합니다.

[CONFIRMED] 해결되지 않은 중대한 security issue가 있으면 production deployment를 진행하지 않고 사람에게 보고해야 합니다. 실제 credential rotation 또는 production setting 변경에는 별도의 사람 authorization이 필요합니다.

## 미확인 사항

[UNKNOWN] Secret/credential rotation status, legacy self-hosted runner host에 남아 있을 수 있는 `.env` cleanup, provider-console configuration, production TLS termination, WAF rule, network control, log retention/access control, incident response process, dependency vulnerability status는 이 repository에서 확정할 수 없습니다.

[FUTURE] Production domain, Cookie Domain/SameSite, TLS, CORS, CSRF/Origin 정책, Redis auth/TLS/network, AWS secret management 및 production monitoring topology는 local 설정과 분리해 결정해야 합니다.
