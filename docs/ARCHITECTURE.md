# Architecture

## 현재 application architecture

[CONFIRMED] application은 `PictureBookApplication`으로 시작되는 단일 Spring Boot service입니다.

```text
External client / frontend (not in this repository)
  -> Spring MVC REST API
       -> Spring Security (OAuth2 and JWT filter)
       -> Services and Spring Data JPA repositories -> PostgreSQL
       -> RedisTemplate -> Redis
       -> MinIO SDK -> MinIO
```

[CONFIRMED] application은 별도로 deploy 가능한 service가 아닌 package-oriented module을 사용합니다. 구현된 각 domain은 일반적으로 필요한 경우 controller, service, repository, entity, DTO package를 가집니다.

## 현재 deployment topology

```text
GitHub Actions CI (main push / main 대상 Pull Request, ubuntu-latest)
  -> Gradle Wrapper clean build / test
  -> deployment 없음

Legacy Build & Deploy (main push, self-hosted runner 필요)
  -> 현재 등록 runner 없음: Queued
  -> runner가 있다면 host 고정 path의 .env / Compose / optional monitoring 복사
  -> docker compose down / docker compose up -d --build

Production Compose
  -> Spring Boot application
  -> Prometheus / Grafana
  -> host 또는 external PostgreSQL / Redis / MinIO
```

[CONFIRMED] Production Compose는 `host.docker.internal` 또는 environment variable을 통해 Compose stack 외부의 PostgreSQL, Redis, MinIO에 접근합니다.

[CONFIRMED] `.github/workflows/ci.yml`은 GitHub-hosted runner에서 build/test만 수행하며 실제 `main` push 실행이 PASS했습니다. `.github/workflows/deploy.yml`은 application health verification, production approval, rollback 단계가 없는 legacy CD 설정이며 현재 등록된 self-hosted runner가 없어 실행되지 않습니다.

## 주요 component

[CONFIRMED]

| Component | code에서 확인된 역할 |
| --- | --- |
| Spring MVC controllers | `/api` 아래에 REST endpoint를 노출합니다. |
| Spring Security | OAuth2 login, JWT bearer-token processing, CORS configuration, authorization rule입니다. |
| JPA/Hibernate | domain entity를 PostgreSQL에 persist합니다. |
| Redis | user별 key 아래에 refresh token을 저장합니다. |
| MinIO | upload/download presigned URL을 생성하고 bucket을 initialize합니다. |
| Spring Actuator / Prometheus registry | dependency와 Compose monitoring configuration이 존재합니다. |

## Authentication 및 authorization boundary

[CONFIRMED] Spring Security는 stateless OAuth2/JWT 구조이며 선택된 endpoint에 명시적으로 authentication을 요구합니다. 마지막 authorization rule은 `anyRequest().permitAll()`입니다.

[CONFIRMED] Reading Goal GET/PUT, Reading Progress GET/PUT/complete POST, Profile/Profile Image PATCH와 Monthly Sales GET은 HTTP method와 구체적인 path pattern 기준으로 authentication을 요구합니다. Monthly Sales는 Service에서 Book 존재 여부와 ownership을 검사합니다. `POST /api/categories`는 명시적인 authenticated matcher에 포함되지 않습니다. 모든 authenticated user에는 `ROLE_USER`가 부여되며 다른 role 또는 method-level authorization은 발견되지 않았습니다.

## AI-generation 경계

[CONFIRMED] `aigeneration`에는 `TextRefinementLog`, `ImageGenerationLog`, `CoverGenerationLog` entity가 포함됩니다. `Book`과 `Page`에도 AI-assisted content를 위한 field가 포함됩니다.

[CONFIRMED] `src/main/java`에서 AI-generation API endpoint, AI client, model-provider SDK, HTTP client call, queue worker, service implementation은 발견되지 않았습니다.

[CONFIRMED] `docker-compose-dev.yml`은 Gemini API-key environment variable을 사용하는 `ai-server` service와 ChromaDB service를 정의합니다. Spring application code는 AI-server URL 또는 ChromaDB를 참조하지 않습니다.

[CONFIRMED] Development Compose가 참조하는 `../picturebook-ai`와 Prometheus/Grafana monitoring configuration path는 조사한 workspace에 존재하지 않습니다.

[UNKNOWN] 참조된 AI server의 source, API contract, deployment process, operational status는 이 repository에서 확인할 수 없습니다.

## Storage architecture

[CONFIRMED] storage module은 `MinioStorageAdapter`로 구현된 `ObjectStoragePort`에 의존합니다. `StorageUseCase`는 `users/{userId}/` 아래에 object key를 생성하고 presigned upload URL을 반환합니다.

[CONFIRMED] `MinioStorageService`는 구성된 bucket이 없으면 생성하고 application startup 시 public-read bucket policy를 설정합니다.

[CONFIRMED] `ObjectStoragePort`/`MinioStorageAdapter`는 presigned PUT/GET 및 delete abstraction을 제공하고, 별도의 `StorageService`/`MinioStorageService`는 직접 upload/delete/public URL과 startup bucket initialization을 제공합니다. 현재 API는 `StorageUseCase`의 presigned upload flow를 사용합니다.

## 현재 operational gap

[CONFIRMED] 이 repository에는 4개 test class와 33개 automated test가 있으며 focused test, 전체 Gradle test, clean build가 PASS했습니다. 현재 test는 `SecurityConfig` matcher characterization, `BookService`/`ReviewService` ownership과 OAuth2 token-log 비노출의 최소 안전망이며 전체 API/runtime coverage는 아닙니다.

[CONFIRMED] Flyway, Liquibase, versioned migration directory/file은 없으며 default/development는 Hibernate `create`, production Compose는 `update`를 설정합니다.

[UNKNOWN] AWS target architecture, AWS Service, IaC 도구는 아직 결정되지 않았습니다.

## Search architecture

[CONFIRMED] 이 repository에는 application-level search implementation이 없습니다.
