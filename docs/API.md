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
| `/api/categories`, `/api/banners`, `/api/ranking` | Category, banner, ranking 조회입니다. |
| `/api/reading-goals`, `/api/report` | Reading goal 및 report입니다. |
| `/api/storage` | Presigned object-upload URL을 발급합니다. |

## Authentication

[CONFIRMED] Security configuration은 선택된 user, storage, report, follow, review, like, paid-publication, personal-book endpoint에 명시적으로 authentication을 요구합니다. JWT filter는 `Authorization` request header에서 bearer token을 읽습니다.

[CONFIRMED] OAuth2 login과 `/api/auth/refresh`는 configuration에 따라 public입니다.

[CONFIRMED] 마지막 authorization rule은 `anyRequest().permitAll()`이며, 앞선 rule과 일치하지 않는 endpoint는 public입니다.

## 누락된 API 영역

[CONFIRMED] book, page, character, auto-save snapshot 생성 또는 편집, AI text refinement, AI image generation, cover generation, 일반 keyword search를 위한 endpoint는 발견되지 않았습니다.

[CONFIRMED] `purchases` entity와 service/repository는 있지만 purchase controller endpoint는 발견되지 않았습니다.
