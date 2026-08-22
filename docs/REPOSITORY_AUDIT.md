# Repository Audit

## Audit 기준

| 항목 | 기준 |
| --- | --- |
| Audit 날짜 | 2026-08-22 |
| Branch | `main` |
| 최초 audit source 기준 commit | `d6918d6` |
| 최초 audit working tree | 재조사 시작 시 clean |
| P0-1 source 기준 commit | `1b56e47` |
| P0-1 상태 갱신 | 2026-08-22, 최소 test/CI 안전망 검증 결과 반영 |
| P0-2 OAuth logging source 기준 commit | `bc1bfd8` |
| P0-2 OAuth logging CI | `main` push PASS 확인 |
| P0-2 Reading Goal source 기준 commit | `ab96932` |
| P0-2 Reading Goal CI | `main` push PASS 확인 |

[CONFIRMED] 이 audit에서는 현재 checkout에서 사용할 수 있는 repository file, Java source, Gradle configuration, Docker file, GitHub Actions workflow, application configuration을 조사했습니다.

[CONFIRMED] 최초 audit 중에는 application code, dependency, infrastructure resource, database, Docker container, external service, secret value를 변경하지 않았습니다. 이후 승인된 P0-1에서 test dependency, test source와 독립 CI workflow가 추가됐으며 application behavior와 external environment는 변경하지 않았습니다.

[CONFIRMED] P0-1에서 focused test, 전체 Gradle test, clean build를 실행해 PASS했으며 GitHub Actions의 실제 `main` push CI도 PASS했습니다. Application runtime, Docker Compose, external dependency 연결 상태는 검증되지 않았습니다.

## 확인된 상태

[CONFIRMED]

1. 이 repository는 Java 21 / Spring Boot 4 backend-only repository입니다.
2. PostgreSQL, Redis, MinIO, OAuth2, JWT, Swagger, Actuator, Prometheus dependency/configuration이 존재합니다.
3. Social login, token refresh/logout, browsing, author activity, reader feature, social interaction, reporting, storage presigned upload URL이 API level에서 구현되어 있습니다.
4. frontend source, migration tooling, AWS IaC, AWS resource configuration은 없습니다.
5. 이 repository에는 search implementation이 없습니다.
6. AI-generation entity는 있지만 AI server 또는 model provider를 호출하는 implementation은 없습니다.
7. Development Compose는 조사한 workspace에 없는 AI-server path를 참조합니다.
8. GitHub-hosted runner 기반 build/test CI와 legacy host-oriented self-hosted CD workflow가 분리되어 있습니다. 현재 self-hosted runner가 없어 legacy workflow는 실행되지 않습니다.
9. 사용자는 현재 AWS infrastructure가 없음을 확인했습니다.
10. `src/main/java/com/picturebook` 아래에는 145개 Java main source, 13개 controller, 17개 service, 12개 repository, 20개 JPA entity가 있습니다.

## 현재 구현 완료 영역

[CONFIRMED] 다음 항목은 source/API level에서 구현되어 있습니다. Gradle test와 clean build는 검증됐지만 application runtime은 검증하지 않았습니다.

- Kakao/Naver OAuth2 social login
- JWT bearer access token 생성 및 검증
- Redis refresh token 저장, access token refresh, logout
- User profile 조회·수정, author profile·통계·수익·summary
- 공개·개인·좋아요·bestseller book 목록과 book detail 조회
- 유료 출판 전환과 유료 book preview/ownership 확인
- Reading progress와 monthly reading goal
- Review 생성·조회·수정·삭제
- Like, follow/unfollow 및 상태 조회
- Report 생성과 사용자별 report 조회
- Category 생성·조회, banner 조회
- 월간·주간 author/book ranking
- MinIO presigned upload URL 발급
- MinIO 직접 upload/delete와 object-storage abstraction
- Swagger/OpenAPI, Actuator, Prometheus dependency/configuration
- MDC 및 AOP logging
- `SecurityConfig` matcher characterization, `BookService`/`ReviewService` ownership과 OAuth2 token-log 비노출을 다루는 4개 test class, 23개 automated test
- OAuth2 login 성공 log의 access token/refresh token 실제 값 출력 제거와 기존 Redis/cookie/redirect 계약 검증
- `GET /api/reading-goals`와 `PUT /api/reading-goals`의 method-specific authentication 및 PUBLIC book endpoint 회귀 검증
- Reading Progress GET/PUT/complete POST의 method-specific authentication 및 PUBLIC book 목록·상세 회귀 검증
- GitHub-hosted runner에서 build/test만 수행하는 독립 CI workflow

## 현재 미구현 영역

[CONFIRMED]

- AI story/text generation
- AI image 및 cover generation
- AI provider/model client, AI-server HTTP integration, queue worker, generation orchestration
- 일반 keyword search, PostgreSQL Full Text Search, Elasticsearch/OpenSearch integration
- Book/Page/Character 생성·편집 API
- Auto-save controller, service, repository
- Purchase 생성·결제 API
- Font, LayoutTemplate, StylePreset 관리 API
- Versioned database migration
- AWS infrastructure, IaC, SDK 및 AWS deployment
- Repository 내부의 완전한 monitoring configuration
- 현재 repository만으로 재현 가능한 standalone local Compose 실행

## Database 및 JPA 구조

[CONFIRMED]

- PostgreSQL은 Spring Data JPA와 Hibernate를 통해 사용됩니다.
- JPA mapping에는 20개 entity, 1개 embeddable ID, 2개 mapped superclass가 있습니다.
- 대부분 identifier에는 generated UUID를 사용하며 category/style/layout/font에는 identity ID를 사용합니다.
- `auto_save_snapshots.snapshot_data`, `pages.layout_override`, `layout_templates.layout_config`는 PostgreSQL `jsonb`로 mapping됩니다.
- `Book`은 JPA relationship으로 `Page`와 `BookCharacter`를 소유합니다. 다수의 다른 aggregate reference는 JPA association이 아니라 scalar UUID/ID field입니다.
- Default/development configuration은 Hibernate `create`, production Compose는 `update`를 설정합니다.
- Flyway, Liquibase, SQL migration directory, versioned migration file은 없습니다.

[UNKNOWN] 실제 database schema, migration history, data, record count, backup, restore 가능 여부, production connection 상태는 repository만으로 확인할 수 없습니다.

## Redis 사용 구조

[CONFIRMED]

- Redis는 현재 OAuth2 login 성공 시 발급한 refresh token을 `RT:{userId}` key로 저장하는 데 사용됩니다.
- Refresh 요청은 JWT를 검증한 뒤 Redis의 저장 value와 비교합니다.
- Logout은 해당 user의 refresh-token key를 삭제합니다.
- 이 repository에서 cache, session, queue, distributed lock 또는 다른 domain data를 위한 Redis 사용은 발견되지 않았습니다.

[UNKNOWN] 실제 Redis data, persistence, backup, availability, memory usage, network/security configuration은 확인할 수 없습니다.

## MinIO 및 Object Storage 구조

[CONFIRMED]

- `ObjectStoragePort`와 `MinioStorageAdapter`는 presigned PUT/GET URL 및 object delete abstraction을 제공합니다.
- `StorageUseCase`는 `users/{userId}/` namespace 아래에 object key를 생성하고 authenticated API를 통해 presigned upload URL을 발급합니다.
- `MinioStorageService`는 application startup 시 bucket 존재 여부를 확인하고, 없으면 생성한 뒤 bucket 전체에 public-read policy를 적용합니다.
- API로 노출된 storage endpoint는 presigned upload URL 발급입니다. Presigned download API endpoint는 발견되지 않았습니다.
- Upload request는 filename과 content type이 비어 있지 않은지만 검증합니다. MIME allowlist, file size, quota 검증은 발견되지 않았으며 request의 `contentType`은 presigned request 생성에 사용되지 않습니다.

[INFERRED] Bucket 전체 public-read와 제한이 충분하지 않은 upload 정책은 user-generated/private content를 production에서 처리하기 전에 security 및 product 검토가 필요합니다.

## Authentication 및 Authorization 구조

[CONFIRMED]

- Spring Security는 stateless session policy를 사용하며 form login과 HTTP Basic은 비활성화되어 있습니다.
- Kakao/Naver OAuth2 login이 구성되어 있습니다.
- Access token은 `Authorization: Bearer` header의 JWT로 처리됩니다.
- Refresh token은 Redis에 저장되고 HttpOnly, Secure, SameSite=Lax cookie로 전달됩니다.
- 선택된 user, storage, report, review, like, follow, paid-publication endpoint는 `SecurityConfig`에서 명시적으로 authentication을 요구합니다.
- `SecurityConfig`의 마지막 rule은 `anyRequest().permitAll()`입니다.
- `GET /api/reading-goals`와 `PUT /api/reading-goals`는 명시적인 authenticated matcher에 포함됩니다.
- Reading Progress GET/PUT/complete POST는 명시적인 authenticated matcher에 포함됩니다.
- `PATCH /api/user/profile`, `PATCH /api/user/profile-image`, `GET /api/books/{bookId}/sales/monthly`, `POST /api/categories`는 명시적인 authenticated matcher에 포함되지 않습니다.
- 모든 authenticated user에는 `ROLE_USER`가 부여됩니다. 다른 role 또는 method-level authorization은 발견되지 않았습니다.
- 일부 service는 user ID 또는 resource ownership을 검사합니다. `BookService`와 `ReviewService`의 대표 ownership unit test 및 `SecurityConfig` matcher characterization test는 존재하지만, 전체 endpoint에 대한 authorization test는 존재하지 않습니다.

[INFERRED] Authentication principal을 사용하는 endpoint가 permit-all default에 남아 있으므로 production 전에 전체 route의 authentication, role 및 object-level authorization 검토가 필요합니다.

## Secret 및 Credential 관리

[CONFIRMED]

- Development Compose에는 OAuth, JWT, database, MinIO 및 관리 service와 관련된 plaintext credential/secret material이 포함되어 있습니다. 실제 value는 이 문서에 기록하지 않습니다.
- Default application configuration은 OAuth/JWT/admin secret의 일부를 environment variable로 참조합니다.
- Production Compose는 주요 credential을 environment variable로 참조합니다.
- Legacy `.github/workflows/deploy.yml`은 self-hosted runner host의 `.env`를 workflow workspace로 복사하도록 구성되어 있습니다. 현재 repository에 등록된 self-hosted runner는 없습니다.
- Discord webhook은 GitHub Actions secret reference를 통해 전달됩니다.
- AWS managed-secret integration 또는 다른 production secret-management implementation은 없습니다.

[UNKNOWN] 노출 가능성이 있었던 credential의 rotation 여부, self-hosted runner의 `.env` cleanup, production secret source와 접근 통제는 확인할 수 없습니다.

## Docker 및 CI/CD 구조

[CONFIRMED]

- Dockerfile은 Java 21 JDK/JRE를 사용하는 multi-stage build이며 Gradle `bootJar`로 application artifact를 생성합니다.
- Development Compose는 application, PostgreSQL, Redis, MinIO, external AI server, ChromaDB, Prometheus, Grafana를 정의합니다.
- Development Compose가 참조하는 `../picturebook-ai`와 monitoring path는 조사한 workspace에 존재하지 않습니다.
- Production Compose는 application, Prometheus, Grafana만 정의하며 PostgreSQL, Redis, MinIO가 Compose stack 외부의 host 또는 external environment에 이미 존재한다고 가정합니다.
- Production Compose는 `host.docker.internal` 및 environment variable을 통해 외부 PostgreSQL, Redis, MinIO에 연결합니다.
- Production Compose가 mount하는 monitoring configuration은 repository에 없습니다.
- `.github/workflows/ci.yml`은 `main` 대상 Pull Request와 `main` push에서 GitHub-hosted `ubuntu-latest` runner로 `./gradlew clean build --no-daemon`을 실행합니다. Secret 주입과 deployment step은 없으며 실제 `main` push 실행이 PASS했습니다.
- `.github/workflows/deploy.yml`은 `main` push 시 trigger되는 legacy self-hosted CD 설정입니다. 현재 등록된 self-hosted runner가 없어 Queued 상태가 되며 deployment는 실행되지 않습니다.
- Legacy workflow는 runner가 있을 경우 host의 고정 path에서 `.env`, `docker-compose.yml`, 선택적으로 monitoring directory를 복사한 뒤 `docker compose down`과 `docker compose up -d --build`를 실행하도록 구성되어 있습니다.
- Legacy workflow에는 application health verification, deployment approval, rollback 단계가 없으며 결과를 Discord webhook으로 알리도록 구성되어 있습니다.

[INFERRED] Build/test CI는 host deployment와 분리됐지만 legacy CD는 특정 host layout과 사전 구성된 external service에 결합되어 있어 AWS 이전 전에 재설계 또는 명시적인 migration이 필요합니다.

## 확인된 operational gap

| Gap | Evidence |
| --- | --- |
| Test coverage 제한 | 4개 test class와 23개 automated test는 최소 안전망이며 전체 endpoint/API/runtime를 검증하지 않습니다. |
| Runtime 미검증 | Gradle test와 clean build는 PASS했지만 application과 Compose는 실행하지 않았습니다. |
| Versioned database migration 부재 | Flyway/Liquibase dependency 또는 migration file이 없습니다. |
| AI implementation 부재 | generation controller/service/client/HTTP call/queue worker가 없습니다. |
| Search implementation 부재 | API/service/query/indexing integration이 없습니다. |
| Book creation/editing 부재 | Book/Page/Character 생성·편집 controller/service flow가 없습니다. |
| Auto-save 실행 부재 | `AutoSaveSnapshot` entity만 있고 controller/service/repository가 없습니다. |
| Purchase 생성 API 부재 | Purchase entity/service/repository는 있지만 purchase 생성 controller/API가 없습니다. |
| repository의 Compose input 불완전 | 참조된 monitoring path와 AI-server path가 없습니다. |
| CD safety gate 부재 | 독립 CI의 build/test는 PASS했지만 legacy CD에는 health verification, approval, rollback 단계가 없습니다. |
| AWS definition 부재 | AWS code, configuration, IaC가 없습니다. |

## code/configuration에서 기록한 risk

[CONFIRMED]

- Development Compose에는 plaintext secret material이 포함되어 있으며, value는 여기에서 반복하지 않습니다.
- OAuth2 success redirect URL은 access token을 query parameter로 전달합니다.
- MinIO startup logic은 구성된 bucket에 public read를 부여합니다.
- Storage upload에는 MIME allowlist, file size 및 quota 검증이 없습니다.
- security configuration은 일치하는 규칙이 없는 request를 기본적으로 public access로 설정합니다.
- Authentication principal을 사용하는 profile·monthly-sales endpoint와 `POST /api/categories`가 명시적인 authenticated matcher에 없습니다.
- Production schema management는 Hibernate `update`로 구성되어 있고, default/development setting은 `create`를 사용합니다.
- GitHub Actions deployment는 host `.env`를 workspace로 복사하고 service를 먼저 중단한 뒤 재기동합니다.
- 일부 Compose image는 immutable version/digest 대신 `latest` tag를 사용합니다.

## 추론된 영향

[INFERRED] 프로젝트에는 AWS를 위한 infrastructure implementation과 핵심 AI picture-book creation flow를 위한 application implementation이 모두 필요합니다.

[INFERRED] 안전한 database 이전에는 versioned migration approach와 명시적인 data export/import 및 verification plan이 필요합니다.

[INFERRED] Production release 전에는 access-token 전달 방식, endpoint authorization, plaintext secret, object-storage 공개 범위 및 upload 제한을 해결하거나 승인된 security decision으로 기록해야 합니다.

## AWS 이전 전 해결 또는 결정 필요 사항

[INFERRED]

- 노출 가능 credential의 rotation 필요 여부 확인
- 전체 endpoint authentication, role 및 object-level authorization 검토
- MinIO public-read와 generated/user-uploaded asset의 공개 범위 결정
- Presigned upload의 MIME, file size, quota 및 lifecycle policy 결정
- Production에서 Hibernate `create/update` 의존을 제거하기 위한 versioned migration approach 결정
- 기존 PostgreSQL export/import, backup, rollback 및 data verification plan
- 기존 Redis refresh token cutover plan
- 기존 MinIO object inventory, migration, URL compatibility 및 rollback plan
- Host 고정 path와 `host.docker.internal` 의존 제거
- 독립 build/test CI 유지와 CD health verification, approval 및 rollback 절차
- OAuth redirect URL, CORS, Domain/DNS/certificate migration plan
- Monitoring, logging, alerting, backup, recovery objective 및 cost-control requirement

[UNKNOWN] AWS target architecture, AWS service 선택, account/region/network/IAM design, IaC 도구는 아직 결정되지 않았습니다.

## AI 그림책 구현 전 결정 필요 사항

[UNKNOWN]

- AI provider/model 및 provider API contract
- 기존 external AI-server를 복원·사용할지 여부
- Story, page, image, cover generation request/response contract
- Asynchronous boundary, queue 필요 여부 및 queue 기술
- Generation lifecycle state, cancellation, retry, failure handling 및 idempotency
- User/book ownership authorization, quota, rate limit, 비용/과금 정책
- Prompt version, character/style/layout/font consistency input
- Generated asset 저장, 선택, alternative, retention policy
- 필요한 database schema 변경과 migration approach
- Content safety, copyright, privacy, moderation 및 user-reporting policy
- ChromaDB 또는 다른 vector database 사용 여부
- Provider contract, failure, retry, authorization, persistence test requirement

## 확보해야 할 미확인 정보

[UNKNOWN]

- frontend repository의 location 및 status
- AI-server repository의 location, API contract, status
- 현재 application runtime status
- 현재 production server 및 Docker container status
- 현재 host database schema/data, Redis data, MinIO object data, backup, volume, credential rotation status
- AWS account/region/budget/access model 및 target operational requirement
- Domain/DNS/certificate ownership 및 OAuth provider-console access
- Production traffic, storage size, database size, recovery objective, monitoring/alerting requirement
- Legacy self-hosted runner host의 security 및 과거 workspace cleanup 상태
- Production TLS termination, WAF, network control, log retention/access control, incident-response process
- Dependency vulnerability status
