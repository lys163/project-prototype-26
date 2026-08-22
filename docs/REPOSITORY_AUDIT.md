# Repository Audit

## Audit 범위

[CONFIRMED] 이 audit에서는 현재 checkout에서 사용할 수 있는 repository file, Java source, Gradle configuration, Docker file, GitHub Actions workflow, application configuration을 조사했습니다.

[CONFIRMED] audit 중 application code, dependency, infrastructure resource, database, secret value는 변경하지 않았습니다.

## 확인된 상태

1. 이 repository는 Java 21 / Spring Boot 4 backend-only repository입니다.
2. PostgreSQL, Redis, MinIO, OAuth2, JWT, Swagger, Actuator, Prometheus dependency/configuration이 존재합니다.
3. Social login, token refresh/logout, browsing, author activity, reader feature, social interaction, reporting, storage presigned upload URL이 API level에서 구현되어 있습니다.
4. frontend source, test source, migration tooling, AWS IaC, AWS resource configuration은 없습니다.
5. 이 repository에는 search implementation이 없습니다.
6. AI-generation entity는 있지만 AI server 또는 model provider를 호출하는 implementation은 없습니다.
7. Development Compose는 조사한 workspace에 없는 AI-server path를 참조합니다.
8. Production Compose와 CI/CD는 기존 host-oriented self-hosted runner deployment에 결합되어 있습니다.
9. 사용자는 현재 AWS infrastructure가 없음을 확인했습니다.

## 확인된 operational gap

| Gap | Evidence |
| --- | --- |
| Test 부재 | `src/test` directory 또는 test source file이 없습니다. |
| Versioned database migration 부재 | Flyway/Liquibase dependency 또는 migration file이 없습니다. |
| AI implementation 부재 | generation controller/service/client/HTTP call이 없습니다. |
| Search implementation 부재 | API/service/query/indexing integration이 없습니다. |
| repository의 Compose input 불완전 | 참조된 monitoring path와 AI-server path가 없습니다. |
| AWS definition 부재 | AWS code, configuration, IaC가 없습니다. |

## code/configuration에서 기록한 risk

[CONFIRMED]

- Development Compose에는 plaintext secret material이 포함되어 있으며, value는 여기에서 반복하지 않습니다.
- 성공한 OAuth2 login은 access token과 refresh token을 logging합니다.
- MinIO startup logic은 구성된 bucket에 public read를 부여합니다.
- security configuration은 일치하는 규칙이 없는 request를 기본적으로 public access로 설정합니다.
- Production schema management는 Hibernate `update`로 구성되어 있고, default/development setting은 `create`를 사용합니다.

## 추론된 영향

[INFERRED] 프로젝트에는 AWS를 위한 infrastructure implementation과 핵심 AI picture-book creation flow를 위한 application implementation이 모두 필요합니다.

[INFERRED] 안전한 database 이전에는 versioned migration approach와 명시적인 data export/import 및 verification plan이 필요합니다.

## 확보해야 할 미확인 정보

- frontend repository의 location 및 status
- AI-server repository의 location, API contract, status
- 현재 host database, Redis, MinIO object data, backup, volume, credential
- AWS account/region/budget/access model 및 target operational requirement
- Domain/DNS/certificate ownership 및 OAuth provider-console access
- Production traffic, storage size, database size, recovery objective, monitoring/alerting requirement
