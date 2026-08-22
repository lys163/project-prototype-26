# PictureBook Server

사용자가 그림책을 탐색하고 읽으며, 궁극적으로 입력한 이야기로 AI 그림책을 생성할 수 있도록 만드는 웹 서비스의 Spring Boot backend입니다.

현재 repository에는 login, 사용자·작가 정보, 책 탐색·열람, community interaction, ranking, report, reading progress, paid publication, MinIO upload URL 등의 backend 기능이 구현되어 있습니다. AI story/image generation 실행 flow는 아직 구현되지 않았습니다.

현재 상태의 기준은 [Repository Audit](docs/REPOSITORY_AUDIT.md)입니다.

## 1. 프로젝트 소개

PictureBook Server는 AI 그림책 서비스의 REST API와 persistence, authentication, object storage 연동을 담당합니다. Frontend source와 외부 AI server source는 이 repository에 포함되어 있지 않습니다.

서비스의 최종 목표는 사용자의 text input을 이야기와 page content로 구성하고, 일관된 character·style·layout을 적용한 그림책을 생성·저장·공유할 수 있게 하는 것입니다. 이 목표와 현재 구현 상태는 구분해서 관리합니다.

## 2. 현재 개발 상태

### 구현 완료

다음 항목은 source/API level에서 확인됐습니다. Gradle test와 clean build는 실행 검증됐지만 application runtime은 아직 검증되지 않았습니다.

- Kakao/Naver OAuth2 social login
- JWT access token과 Redis refresh token 기반 인증
- User profile, author profile·통계·수익·summary
- 공개·개인·좋아요·bestseller 책 목록과 상세 조회
- 유료 출판 전환과 유료 책 preview/ownership 확인
- Review, like, follow/unfollow, report
- Reading progress와 monthly reading goal
- Category, banner, 월간·주간 ranking
- MinIO presigned upload URL과 object-storage abstraction
- Swagger/OpenAPI, Actuator, Prometheus 연동 구성

### 개발 예정 또는 미구현

- Book/Page/Character 생성·편집 API
- Auto-save 실행 flow와 Purchase 생성·결제 API
- AI story/text/image/cover generation 실행 flow
- AI provider client, asynchronous worker 및 generation orchestration
- Keyword Search 및 별도 search engine integration
- 전체 API contract 및 integration test coverage 확대
- Versioned Database migration
- AWS target architecture 설계와 infrastructure 구축
- 기존 PostgreSQL·Redis·MinIO의 migration 및 cutover
- 누락된 monitoring configuration과 재현 가능한 deployment 검증

AI provider/model, Queue, Vector Database, Database migration 도구, AWS Service와 IaC 도구는 아직 결정되지 않았습니다.

## 3. 기술 Stack

| 영역 | 현재 확인된 기술·상태 |
| --- | --- |
| Backend | Java 21, Spring Boot 4.0.5, Spring MVC, Gradle Wrapper 9.4.1 |
| Persistence | PostgreSQL, Spring Data JPA, Hibernate, 일부 `jsonb` mapping |
| Redis | Spring Data Redis, refresh token 저장·조회·삭제 용도 |
| Object Storage | MinIO Java SDK 8.6.0, presigned URL, storage abstraction |
| Authentication | Spring Security, OAuth2 Client, Kakao/Naver, JWT |
| API 문서 | SpringDoc OpenAPI, Swagger UI |
| Container | Docker, Docker Compose |
| CI/CD | GitHub-hosted `ubuntu-latest`에서 build/test를 수행하는 CI. 별도의 legacy self-hosted Compose deployment workflow는 runner 부재로 실행되지 않음 |
| Monitoring | Spring Actuator, Prometheus registry, Prometheus/Grafana Compose 정의. Repository 내부 monitoring file은 누락됨 |
| AI | AI-generation entity와 development Compose reference만 존재. 실행 integration은 미구현 |
| AWS | 현재 infrastructure·SDK·IaC 없음. Target architecture 설계 전 |
| Test | JUnit Platform, Web MVC/Security test support, 4개 test class와 23개 automated test |
| Database migration | Versioned migration 도구와 migration file 없음 |

## 4. Architecture 개요

현재 application은 package-oriented 단일 Spring Boot service입니다.

```text
Frontend / External Client (이 repository에 없음)
  -> Spring MVC REST API
       -> Spring Security: OAuth2 + JWT filter
       -> Services / Spring Data JPA -> PostgreSQL
       -> RedisTemplate -> Redis refresh token
       -> ObjectStoragePort / MinIO SDK -> MinIO
```

현재 `main` push와 `main` 대상 Pull Request에서는 GitHub-hosted `ubuntu-latest` runner가 Gradle build/test CI를 수행하며 deployment는 실행하지 않습니다. 기존 `.github/workflows/deploy.yml`은 `main` push 시 trigger되는 legacy self-hosted Compose deployment 설정이지만, 현재 등록된 self-hosted runner가 없어 Queued 상태가 됩니다. Production Compose는 PostgreSQL, Redis, MinIO가 Compose 외부의 host 또는 external environment에 존재한다고 가정합니다.

AWS infrastructure는 아직 존재하지 않으며 미래 AWS Architecture도 결정되지 않았습니다. 현재 구조와 향후 AWS target을 동일한 Architecture로 취급하지 않습니다.

상세 내용은 [Architecture 문서](docs/ARCHITECTURE.md)를 참고합니다.

## 5. 로컬 개발 및 실행 방법

### 필요한 개발 환경

- Java 21
- Repository에 포함된 Gradle Wrapper
- Docker 및 Docker Compose: Compose 기반 실행 시 필요
- PostgreSQL, Redis, MinIO: application 실행에 필요한 external dependency

### 환경 설정

실행 환경에 따라 다음 종류의 configuration이 필요합니다. 실제 Secret 값은 repository나 문서에 기록하지 않습니다.

- Database: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- Redis: `SPRING_DATA_REDIS_HOST`, `SPRING_DATA_REDIS_PORT`
- OAuth2: `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET`, `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`
- JWT: `JWT_SECRET`, `JWT_ACCESS_TOKEN_EXPIRY`, `JWT_REFRESH_TOKEN_EXPIRY`
- Application: `APP_FRONTEND_URL`, `KAKAO_ADMIN_KEY`
- MinIO: `MINIO_ENDPOINT`, `MINIO_PUBLIC_ENDPOINT`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, `MINIO_BUCKET`
- Profile: `SPRING_PROFILES_ACTIVE`

`application.properties`는 `secret` profile을 include하지만 repository에는 `application-secret.properties`가 없습니다. Environment-specific 값의 실제 제공 방식은 실행 환경에서 준비해야 합니다.

### Spring Boot 실행

macOS/Linux:

```bash
./gradlew bootRun
```

Windows:

```powershell
.\gradlew.bat bootRun
```

Gradle clean build는 검증됐지만 위 `bootRun` 명령과 application runtime은 검증하지 않았습니다. PostgreSQL, Redis, MinIO 및 필요한 environment variable이 먼저 준비돼야 합니다.

### Build 및 Test

macOS/Linux:

```bash
./gradlew bootJar
./gradlew test
```

Windows:

```powershell
.\gradlew.bat bootJar
.\gradlew.bat test
```

현재 `src/test`에는 `SecurityConfig` characterization, `BookService`/`ReviewService` ownership, OAuth2 token-log 비노출을 검증하는 4개 test class와 23개 automated test가 있습니다. 현재 checkout의 focused test, 전체 Gradle test와 clean build가 PASS했습니다. GitHub Actions CI의 마지막 확인 결과는 P0-2 Reading Goal authorization 변경 `main` push PASS이며, 이번 Reading Progress authorization 변경은 아직 원격 CI 실행 전입니다. 이 최소 안전망은 전체 API contract 또는 application runtime 검증을 의미하지 않습니다.

### Docker Compose

Development Compose의 의도된 실행 명령은 다음과 같습니다.

```bash
docker compose -f docker-compose-dev.yml up --build
```

그러나 `docker-compose-dev.yml`이 참조하는 `../picturebook-ai`와 monitoring configuration은 현재 workspace에 없습니다. 따라서 이 repository만으로 development stack의 완전한 실행을 보장할 수 없습니다.

Host-oriented Compose의 구성상 실행 명령은 다음과 같습니다.

```bash
docker compose up -d --build
```

이 구성은 별도의 `.env`, monitoring file, 외부 PostgreSQL·Redis·MinIO를 전제로 합니다. Production 또는 shared environment에서의 실제 실행·중단·재시작에는 대상 환경과 범위가 명시된 사람의 승인이 필요합니다.

## 6. AI Agent 활용 방식

이 프로젝트는 Codex를 주요 AI coding agent로 활용합니다. Agent는 source/configuration evidence를 우선하며, 확인되지 않은 내용은 `[INFERRED]` 또는 `[UNKNOWN]`으로 구분합니다.

| 위치 | 역할 |
| --- | --- |
| [AGENTS.md](AGENTS.md) | 프로젝트 최상위 AI Agent 운영 규칙 |
| [.agents/rules/](.agents/rules/) | Security, Database, AWS, AI 등 작업 영역별 필수 세부 규칙 |
| [.agents/workflows/](.agents/workflows/) | 조사, 계획, 구현, 테스트, 리뷰, 배포 절차 |
| [.agents/skills/](.agents/skills/) | Repository 조사, AWS, AI, image, Database migration, testing, security 전문 지침 |
| [docs/](docs/) | 프로젝트 상태와 Architecture, API, Database, AWS, Security 지식 문서 |

작업 전에는 [AGENTS.md](AGENTS.md)와 해당 작업에 관련된 `.agents/` 및 `docs/` 문서를 확인합니다. 상세 규칙은 README에 복제하지 않습니다.

## 7. 프로젝트 개발 순서

1. **완료 — Repository baseline 및 문서 정리**
2. **예정 — 확인된 보안 문제와 기술 부채 처리**
3. **예정 — AWS target architecture 설계 및 승인**
4. **예정 — Database/Object Storage migration 전략 수립**
5. **예정 — 승인된 방식으로 AWS infrastructure 구축**
6. **예정 — 기존 서비스와 data를 AWS로 이전**
7. **예정 — AWS 환경에서 기존 기능 검증**
8. **예정 — AI 그림책 요구사항과 Architecture 설계**
9. **예정 — AI 그림책 생성 backend/pipeline 구현**
10. **예정 — 기존 Spring Backend와 AI 기능 연동**
11. **예정 — Frontend 연동**
12. **예정 — 통합 test 및 security 검증**
13. **예정 — Production deployment 및 최종 검증**

각 단계의 Architecture, provider, migration 및 infrastructure 기술은 승인된 결정이 생긴 뒤 관련 docs에 기록합니다.

## 8. 프로젝트 문서

- [PROJECT.md](docs/PROJECT.md): 제품 범위, runtime, source layout과 현재 개발 상태
- [ARCHITECTURE.md](docs/ARCHITECTURE.md): application, deployment, authentication, AI 및 storage Architecture
- [DATABASE.md](docs/DATABASE.md): PostgreSQL/JPA mapping, relationship, constraint와 migration 현황
- [API.md](docs/API.md): 구현된 endpoint 그룹, authentication boundary와 누락 API
- [AWS.md](docs/AWS.md): 현재 AWS 부재 상태, 기존 deployment와 이전 전 미결정 사항
- [SECURITY.md](docs/SECURITY.md): 인증, CORS, storage access와 확인된 security finding
- [REPOSITORY_AUDIT.md](docs/REPOSITORY_AUDIT.md): 현재 repository 사실의 기준 baseline

## 9. 보안

- Secret, API Key, Password, Token, Credential의 실제 값을 repository에 commit하거나 문서와 log에 기록하지 않습니다.
- Production 또는 shared environment의 deployment와 external state 변경은 명시적인 승인이 필요합니다.
- 현재 확인된 security finding과 production release gate는 [Security 문서](docs/SECURITY.md)를 확인합니다.
- AWS, Database, OAuth provider, production Secret 변경은 [AGENTS.md](AGENTS.md)의 승인 규칙을 따릅니다.
