# PictureBook Server: 프로젝트 상태

## Baseline

[CONFIRMED] 이 문서는 `docs/REPOSITORY_AUDIT.md`의 2026-08-22, `main`, P0-2 Profile authorization source 기준 commit `f903d13` 및 현재 Monthly Sales authorization working tree audit baseline을 따릅니다.

[CONFIRMED] P0-1에서 focused test, 전체 Gradle test, clean build를 실행해 PASS했으며 GitHub Actions CI의 실제 실행도 PASS했습니다. Application runtime과 Docker Compose 실행은 아직 검증되지 않았습니다.

## 범위

[CONFIRMED] 이 repository에는 AI picture-book platform의 backend server가 포함되어 있습니다. Java/Spring Boot application이며, 이 repository에는 frontend source가 없습니다.

[CONFIRMED] 구현된 backend 영역에는 social login, user profile, book browsing, review, like, follow, reading progress, ranking, report, category, banner, storage upload URL, paid-publication 관련 record가 포함됩니다.

[CONFIRMED] repository에는 AI-generation domain entity가 있지만, AI text 또는 image generation을 수행하는 controller, service, client, HTTP integration은 없습니다.

[CONFIRMED] 이 repository에는 search API, search service, title/content keyword query, PostgreSQL full-text search, Elasticsearch, OpenSearch implementation이 없습니다.

[CONFIRMED] Book/Page/Character 생성·편집 API, auto-save controller/service/repository, purchase 생성·결제 API는 없습니다. Purchase entity/service/repository는 수익 및 구매 여부 조회에 사용되지만 purchase 생성 controller는 없습니다.

## Runtime 및 build

[CONFIRMED]

- Java toolchain: 21
- Build tool: Gradle Wrapper 9.4.1
- Framework: Spring Boot 4.0.5
- Artifact task: `./gradlew bootJar`
- Server port: 8080
- API documentation: `/swagger-ui.html` 및 `/v3/api-docs`

출처: `build.gradle`, `gradle/wrapper/gradle-wrapper.properties`, `src/main/resources/application.properties`입니다.

## Local execution input

[CONFIRMED] default application configuration은 localhost의 PostgreSQL 및 Redis를 전제로 합니다. development Compose file은 PostgreSQL, Redis, MinIO, ChromaDB, AI server, Prometheus, Grafana container를 정의합니다.

[CONFIRMED] Local credential은 repository root의 ignored `.env`에서 제공할 수 있으며, tracked `.env.example`은 필요한 variable name만 제공합니다. Docker Compose는 `.env` substitution을 사용하고 native Spring Boot 실행은 optional `.env` config import를 사용합니다. Production/shared credential은 default 없는 external environment injection을 유지합니다.

[CONFIRMED] development Compose file은 `../picturebook-ai`를 참조하지만, 조사한 workspace의 해당 위치에는 존재하지 않습니다.

[CONFIRMED] Compose file은 이 repository에 없는 monitoring configuration path를 참조합니다.

[CONFIRMED] Production Compose는 application, Prometheus, Grafana를 정의하고 PostgreSQL, Redis, MinIO가 Compose 외부의 host 또는 external environment에 이미 존재한다고 가정합니다.

[CONFIRMED] `.github/workflows/ci.yml`은 `main` 대상 Pull Request와 `main` push에서 GitHub-hosted `ubuntu-latest` runner로 Gradle clean build/test를 수행하며 deployment는 실행하지 않습니다. 실제 `main` push CI 실행은 PASS했습니다. 기존 `.github/workflows/deploy.yml`은 host의 고정 path와 self-hosted runner를 전제로 하는 legacy CD 설정으로 남아 있으며, 현재 등록된 self-hosted runner가 없어 trigger 후 Queued 상태가 됩니다. Legacy workflow에는 application health verification, production approval, rollback 단계가 없습니다.

[UNKNOWN] 누락된 AI-server 및 monitoring path가 repository 외부에서 제공될 수 있으므로, 이 repository만으로 현재 완전하게 동작하는 local startup procedure를 확정할 수 없습니다.

## Source layout

[CONFIRMED]

```text
src/main/java/com/picturebook/
  auth/          authentication token operations
  user/          users and author profiles
  book/          book browsing, detail, publishing, performance
  storage/       MinIO object storage and presigned URLs
  aigeneration/  AI-generation log entities only
  global/        security, persistence, Redis, logging, API responses
  ...            reviews, likes, follows, ranking, reading, reports, etc.
```

## Operational dependency 상태

[CONFIRMED] Redis는 현재 OAuth2 login의 refresh token을 user별 key로 저장·조회·삭제하는 데 사용됩니다. Cache, queue, session 또는 distributed lock 용도는 발견되지 않았습니다.

[CONFIRMED] 이 repository에는 AWS infrastructure, AWS IaC, versioned database migration tool/file이 없습니다.

## Test 상태

[CONFIRMED] `src/test`에는 explicit PUBLIC/authenticated/deny/fallback `SecurityConfig` characterization, `BookService`/`ReviewService` ownership, OAuth2 token-log 비노출을 다루는 4개 test class와 55개 automated test가 있습니다.

[CONFIRMED] 현재 checkout의 Category Security focused test, 전체 Gradle test와 clean build가 PASS했습니다. GitHub Actions의 마지막 확인 결과는 P0-2 Reading Progress authorization `main` push CI PASS이며, 현재 working-tree 변경은 아직 원격 CI 실행 전입니다.

[UNKNOWN] Application runtime과 external dependency가 필요한 통합 동작은 아직 검증되지 않았습니다.
