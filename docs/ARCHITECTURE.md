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

## AI-generation 경계

[CONFIRMED] `aigeneration`에는 `TextRefinementLog`, `ImageGenerationLog`, `CoverGenerationLog` entity가 포함됩니다. `Book`과 `Page`에도 AI-assisted content를 위한 field가 포함됩니다.

[CONFIRMED] `src/main/java`에서 AI-generation API endpoint, AI client, model-provider SDK, HTTP client call, queue worker, service implementation은 발견되지 않았습니다.

[CONFIRMED] `docker-compose-dev.yml`은 Gemini API-key environment variable을 사용하는 `ai-server` service와 ChromaDB service를 정의합니다. Spring application code는 AI-server URL 또는 ChromaDB를 참조하지 않습니다.

[UNKNOWN] 참조된 AI server의 source, API contract, deployment process, operational status는 이 repository에서 확인할 수 없습니다.

## Storage architecture

[CONFIRMED] storage module은 `MinioStorageAdapter`로 구현된 `ObjectStoragePort`에 의존합니다. `StorageUseCase`는 `users/{userId}/` 아래에 object key를 생성하고 presigned upload URL을 반환합니다.

[CONFIRMED] `MinioStorageService`는 구성된 bucket이 없으면 생성하고 application startup 시 public-read bucket policy를 설정합니다.

## Search architecture

[CONFIRMED] 이 repository에는 application-level search implementation이 없습니다.
