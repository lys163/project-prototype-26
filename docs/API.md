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
| `/api/categories` | PUBLIC Category 조회와 User Server에서 비활성화된 Category 생성 endpoint입니다. |
| `/api/banners`, `/api/ranking` | Banner 및 ranking 조회입니다. |
| `/api/reading-goals`, `/api/report` | Reading goal 및 report입니다. |
| `/api/storage` | Presigned object-upload URL을 발급합니다. |

## Authentication

[CONFIRMED] Security configuration은 선택된 user, storage, report, follow, review, like, paid-publication, personal-book endpoint에 명시적으로 authentication을 요구합니다. JWT filter는 `Authorization` request header에서 bearer token을 읽습니다.

[CONFIRMED] OAuth2 login, `/api/auth/refresh`, `POST /api/auth/logout`은 configuration에 따라 public입니다. Logout은 optional refresh-token cookie를 검증하고 Redis의 현재 token과 exact match일 때만 원자적으로 삭제하며, cookie가 없거나 invalid/expired/mismatch여도 cookie를 만료시키고 HTTP 204를 반환합니다.

[CONFIRMED] Banner, 공개 Book/Review/Like 조회, Category GET, Author 공개 조회와 Ranking 조회 6개는 HTTP GET과 구체적인 path 기준의 `permitAll` matcher에 명시되어 있습니다. 마지막 authorization rule은 `anyRequest().denyAll()`이며, 앞선 rule과 일치하지 않는 request는 차단됩니다.

[CONFIRMED] Reading Goal GET/PUT, Reading Progress GET/PUT/complete POST, Profile/Profile Image PATCH와 Monthly Sales GET은 명시적인 authenticated matcher에 포함됩니다. `POST /api/categories`는 HTTP method와 exact path 기준의 `denyAll` matcher로 User Server의 anonymous 및 authenticated user 모두에게 비활성화되어 있으며, `GET /api/categories`는 PUBLIC으로 유지됩니다.

[CONFIRMED] Category 생성 code는 현재 User Server에 남아 있지만 Category 생성·수정·비활성화는 향후 별도 관리자 서버로 이전할 예정입니다. [UNKNOWN] 초기 Category master data 공급 방식은 아직 결정되지 않았습니다.

[CONFIRMED] `GET /api/books/{bookId}/sales/monthly`는 현재 사용자가 소유한 Book만 조회할 수 있습니다. 본인 Book에 판매가 없으면 HTTP 200과 1~12월 판매량 0을 반환하고, 다른 사용자 소유 Book은 `BOOK_SALES_FORBIDDEN`으로 HTTP 403, 존재하지 않는 Book은 `BOOK_NOT_FOUND`로 HTTP 404를 반환하도록 구현되어 있습니다.

[CONFIRMED] 모든 authenticated user에는 `ROLE_USER`가 부여됩니다. 다른 role 및 method-level authorization은 발견되지 않았습니다.

[CONFIRMED] OAuth2 login 성공 시 refresh token은 HttpOnly, Secure, SameSite=Lax cookie로 전달되고 callback URL에는 `isNewUser`만 포함됩니다. Frontend는 callback에서 `POST /api/auth/refresh`를 호출해 access token을 응답으로 획득합니다.

## Storage API 상태

[CONFIRMED] `POST /api/storage/presigned-upload`는 authenticated user에게 MinIO presigned POST form을 발급합니다. Request는 `filename`, `contentType`, `fileSize`를 받고 response는 `objectKey`, `uploadUrl`, signed `fields`, `publicUrl`, `expiresInSeconds`를 반환합니다. Frontend는 fields를 그대로 `multipart/form-data`에 넣은 뒤 `file` field를 추가해 MinIO로 POST합니다. Object-storage abstraction에는 presigned GET 기능이 있지만 이를 노출하는 download controller endpoint는 없습니다.

[CONFIRMED] Upload는 `image/jpeg`, `image/png`, `image/webp`만 허용하고 1 byte 이상 5 MiB 이하로 제한합니다. Backend의 `fileSize` 검사는 조기 거부용이며 MinIO POST policy의 exact key, exact Content-Type, `content-length-range`가 actual upload를 검증합니다. Object key는 `users/{currentUserId}/{UUID}.{canonicalExtension}`이고 policy 만료는 600초입니다.

[CONFIRMED] Profile update는 기존 DB의 OAuth external image URL을 그대로 유지하거나 현재 사용자의 configured Storage namespace URL로 교체하는 경우만 허용합니다. 신규 arbitrary external URL과 다른 사용자 namespace URL은 거부합니다.

## 누락된 API 영역

[CONFIRMED] book, page, character, auto-save snapshot 생성 또는 편집, AI text refinement, AI image generation, cover generation, 일반 keyword search를 위한 endpoint는 발견되지 않았습니다.

[CONFIRMED] `purchases` entity와 service/repository는 있지만 purchase controller endpoint는 발견되지 않았습니다.

## 검증 상태

[CONFIRMED] 이 repository에는 11개 test class와 91개 automated test가 있습니다. Characterization test는 17개 explicit PUBLIC GET endpoint와 public user profile의 anonymous 허용, 기존 authenticated endpoint의 anonymous 차단, access token 없는 logout 허용, Category POST 비활성화 및 unmatched request의 fail-closed 동작을 검증합니다. Logout focused test는 invalid/expired/missing refresh token의 idempotency, Redis exact-match compare-and-delete와 cookie 만료를 검증합니다. Monthly Sales Service test는 본인 Book의 판매 없음, 다른 사용자 소유 Book, 존재하지 않는 Book 정책을 검증합니다. 전체 API contract test coverage는 아직 없습니다.

[CONFIRMED] 2026-08-29 전체 Gradle test 91개를 재실행해 PASS했습니다. Characterization 결과 17개 explicit PUBLIC GET은 anonymous 요청을 허용하고, Category POST는 anonymous 401/authenticated 403, unmatched test-only endpoint는 anonymous 401/authenticated 403입니다. Monthly Sales의 403/404는 Service ErrorCode와 기존 `GlobalExceptionHandler` mapping으로 검증되며 실제 `BookController` HTTP contract를 직접 실행하는 MVC test는 없습니다. 현재 GitHub Actions의 원격 실행 상태는 repository만으로 확인할 수 없습니다.

[UNKNOWN] Application runtime 및 external dependency를 포함한 API 통합 동작은 아직 검증되지 않았습니다.
