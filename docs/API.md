# API

## 공통 API 규칙

[CONFIRMED] REST controller는 주로 `/api` 아래에 있습니다. 성공한 application response는 일반적으로 `ApiResponse`를 사용하며, pageable response는 `PageResponse`를 사용합니다.

[CONFIRMED] Swagger UI는 `/swagger-ui.html`에, OpenAPI document는 `/v3/api-docs`에 구성되어 있습니다.

## Endpoint 그룹

[CONFIRMED]

| Prefix | 확인된 역할 |
| --- | --- |
| `/api/auth` | Logout 및 access-token refresh. OAuth2 login은 `/oauth2/**` 및 `/login/**` 아래의 Spring Security route를 사용합니다. |
| `/api/user` | 현재 user profile, profile update, reader dashboard, revenue/summary, public profile입니다. |
| `/api/authors` | Author 통계 및 author book입니다. |
| `/api/authors/{authorId}/follow` | Follow, unfollow, follow status입니다. |
| `/api/books` | Public list, personal list, detail, bestseller data, paid publication, reading progress, performance입니다. |
| `/api/books/{bookId}/likes` | Like, unlike, like status입니다. |
| `/api/books/{bookId}/reviews` 및 `/api/reviews` | Review list, personal list, create, update, delete입니다. |
| `/api/categories` | Category 생성 및 조회입니다. |
| `/api/banners`, `/api/ranking` | Banner 및 ranking 조회입니다. |
| `/api/reading-goals`, `/api/report` | Reading goal 및 report입니다. |
| `/api/storage` | Presigned object-upload URL을 발급합니다. |

## Authentication

[CONFIRMED] Security configuration은 선택된 user, storage, report, follow, review, like, paid-publication, personal-book endpoint에 명시적으로 authentication을 요구합니다. JWT filter는 `Authorization` request header에서 bearer token을 읽습니다.

[CONFIRMED] OAuth2 login과 `/api/auth/refresh`는 configuration에 따라 public입니다.

[CONFIRMED] 마지막 authorization rule은 `anyRequest().permitAll()`이며, 앞선 rule과 일치하지 않는 endpoint는 public입니다.

[CONFIRMED] Reading Goal GET/PUT, Reading Progress GET/PUT/complete POST와 Profile/Profile Image PATCH는 명시적인 authenticated matcher에 포함됩니다. `GET /api/books/{bookId}/sales/monthly`, `POST /api/categories`는 여전히 명시적인 authenticated matcher에 포함되지 않습니다. 해당 endpoint는 route level에서 permit-all default에 남아 있습니다.

[CONFIRMED] 모든 authenticated user에는 `ROLE_USER`가 부여됩니다. 다른 role 및 method-level authorization은 발견되지 않았습니다.

[CONFIRMED] OAuth2 login 성공 시 access token은 frontend callback URL의 query parameter로 전달되고 refresh token은 HttpOnly, Secure, SameSite=Lax cookie로 전달됩니다.

## Storage API 상태

[CONFIRMED] `/api/storage/presigned-upload`는 authenticated user에게 MinIO presigned PUT URL을 발급합니다. Object-storage abstraction에는 presigned GET 기능이 있지만 이를 노출하는 download controller endpoint는 발견되지 않았습니다.

[CONFIRMED] Upload request는 filename과 content type의 `@NotBlank`만 검증합니다. MIME allowlist, upload size, quota 검증은 발견되지 않았으며 request의 `contentType`은 presigned request 생성에 사용되지 않습니다.

## 누락된 API 영역

[CONFIRMED] book, page, character, auto-save snapshot 생성 또는 편집, AI text refinement, AI image generation, cover generation, 일반 keyword search를 위한 endpoint는 발견되지 않았습니다.

[CONFIRMED] `purchases` entity와 service/repository는 있지만 purchase controller endpoint는 발견되지 않았습니다.

## 검증 상태

[CONFIRMED] 이 repository에는 `SecurityConfig` matcher characterization, `BookService`/`ReviewService` ownership과 OAuth2 token-log 비노출을 검증하는 4개 test class, 28개 automated test가 있습니다. Characterization test는 Reading Goal, Reading Progress와 Profile 수정 endpoint의 anonymous 차단 및 authenticated 통과를 검증하며 anonymous `GET /api/books`, `GET /api/books/{bookId}`와 `GET /api/user/{userId}/profile`이 계속 허용되는 것을 확인합니다. 전체 API contract test coverage는 아직 없습니다.

[CONFIRMED] 현재 checkout의 focused test, 전체 Gradle test와 clean build가 PASS했습니다. GitHub Actions의 마지막 확인 결과는 P0-2 Reading Progress authorization CI PASS이며, 이번 Profile authorization 변경은 아직 원격 CI 실행 전입니다.

[UNKNOWN] Application runtime 및 external dependency를 포함한 API 통합 동작은 아직 검증되지 않았습니다.
