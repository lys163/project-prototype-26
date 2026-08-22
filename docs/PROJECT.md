# PictureBook Server: 프로젝트 상태

## Baseline

[CONFIRMED] 이 문서는 `docs/REPOSITORY_AUDIT.md`의 2026-08-22, `main`, source 기준 commit `d6918d6` audit baseline을 따릅니다.

[CONFIRMED] 해당 audit에서는 build, test, application runtime, Docker Compose를 실행하지 않았으므로 compile 및 runtime 상태는 검증되지 않았습니다.

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

[CONFIRMED] development Compose file은 `../picturebook-ai`를 참조하지만, 조사한 workspace의 해당 위치에는 존재하지 않습니다.

[CONFIRMED] Compose file은 이 repository에 없는 monitoring configuration path를 참조합니다.

[CONFIRMED] Production Compose는 application, Prometheus, Grafana를 정의하고 PostgreSQL, Redis, MinIO가 Compose 외부의 host 또는 external environment에 이미 존재한다고 가정합니다.

[CONFIRMED] 현재 CI/CD는 `main` push 시 self-hosted GitHub Actions runner에서 host의 고정 path에 있는 `.env`, Compose file, 선택적 monitoring directory를 복사하고 `docker compose down`과 `docker compose up -d --build`를 실행합니다. 별도의 automated test, health verification, production approval, rollback 단계는 없습니다.

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

[CONFIRMED] 이 repository에는 AWS infrastructure, AWS IaC, versioned database migration tool/file, automated test source가 없습니다.

## Test 상태

[CONFIRMED] 이 repository에는 `src/test` directory 및 test source file이 없습니다.

[UNKNOWN] 현재 compile, test, runtime status는 repository audit의 일부로 실행되지 않았습니다.
