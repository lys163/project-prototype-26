# PictureBook Server: 프로젝트 상태

## 범위

[CONFIRMED] 이 repository에는 AI picture-book platform의 backend server가 포함되어 있습니다. Java/Spring Boot application이며, 이 repository에는 frontend source가 없습니다.

[CONFIRMED] 구현된 backend 영역에는 social login, user profile, book browsing, review, like, follow, reading progress, ranking, report, category, banner, storage upload URL, paid-publication 관련 record가 포함됩니다.

[CONFIRMED] repository에는 AI-generation domain entity가 있지만, AI text 또는 image generation을 수행하는 controller, service, client, HTTP integration은 없습니다.

[CONFIRMED] 이 repository에는 search API, search service, title/content keyword query, PostgreSQL full-text search, Elasticsearch, OpenSearch implementation이 없습니다.

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

## Test 상태

[CONFIRMED] 이 repository에는 `src/test` directory 및 test source file이 없습니다.

[UNKNOWN] 현재 compile, test, runtime status는 repository audit의 일부로 실행되지 않았습니다.
