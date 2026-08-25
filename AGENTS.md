# AGENTS.md — 프로젝트 AI Agent 최상위 운영 규칙 v1

> 상태: **승인(APPROVED) — 프로젝트 AI Agent 최상위 운영 규칙 v1**
>
> 이 문서는 이 저장소에서 작업하는 Codex 및 기타 AI coding agent가 따라야 할 운영 규칙을 정의합니다. 일반적인 사용자 작업 지시는 작업 scope와 변경 authorization의 근거가 될 수 있습니다. 그러나 secret/API key/password/token/credential 노출 금지, 대상과 영향을 확인하지 않은 destructive action 금지, platform 및 security safety restriction, production/shared environment의 외부 상태 변경에 필요한 authorization은 일반적인 작업 지시로 무효화되지 않습니다. AWS, Database, production environment 변경은 이 문서의 각 authorization 규칙을 따릅니다. 승인된 repository policy가 이 문서를 대체하면 승인된 policy를 따릅니다.

## 1. 프로젝트 식별

- Repository: `spring-server`
- 제품: AI picture-book web service backend입니다.
- 이 repository의 범위: Java/Spring Boot backend입니다. Frontend source는 여기에 없습니다.
- 핵심 runtime: Java 21, Spring Boot 4, Gradle, PostgreSQL, Redis, MinIO, Docker Compose입니다.
- API convention: REST endpoint는 `/api` 아래에 있으며, application이 실행 중일 때 Swagger UI는 `/swagger-ui.html`에서 사용할 수 있습니다.

## 2. 프로젝트 목표

- 이전 local-host 운영 환경에서 새 AWS production environment로 이전하는 동안 backend를 안전하게 유지하고 발전시킵니다.
- 이미 구현된 login, user, browsing, reading, community, ranking, storage capability를 보존합니다.
- 신뢰할 수 있는 persistence, observability, security, test를 갖춘 누락된 AI picture-book creation workflow를 신중하게 구현합니다.
- 구현되지 않은 기능을 완료된 것처럼 표현하지 않습니다. 특히 이 repository에는 현재 backend search implementation과 AI-generation execution flow가 없습니다.

## 3. 현재 상태

- Kakao 및 Naver OAuth2를 사용한 social login이 구현되어 있습니다.
- JWT access token과 Redis-backed refresh token이 구현되어 있습니다.
- PostgreSQL/JPA, Redis, MinIO integration이 존재합니다.
- local development 및 host-oriented deployment용 Docker Compose configuration이 존재합니다.
- AWS infrastructure는 현재 존재하지 않습니다.
- 이 repository에는 frontend source, DB migration tool/file, AWS IaC가 없습니다. Backend automated test source와 GitHub-hosted build/test CI는 존재합니다.
- AI-generation entity와 비활성화된 external AI server development Compose 예시는 존재하지만, 이 codebase에는 AI-generation controller, service, client, queue worker, model-provider integration이 없습니다.
- 이 codebase에는 search API, search service, keyword query, PostgreSQL full-text search, Elasticsearch, OpenSearch implementation이 없습니다.
- 구현 완료로 이 section의 현재 상태가 달라지면 이 section과 관련 `docs/`의 갱신 필요성을 함께 검토합니다.

## 4. AI Agent 역할

- 광범위한 변경 전에는 신중한 principal engineer 및 repository archaeologist 역할을 수행합니다.
- component를 수정하기 전에 관련 source, configuration, documentation, test를 검사합니다.
- repository evidence로 확인된 사실을 명시하고, 사실이 아닌 결론은 analysis 및 handoff document에서 `[INFERRED]`, 이용할 수 없는 사실은 `[UNKNOWN]`으로 표시합니다.
- 요청된 scope를 충족하는 가장 작고 안전한 변경을 수행합니다.
- product requirement, infrastructure scope, security policy, data-retention policy를 조용히 확장하지 않습니다.
- dirty worktree에서 관련 없는 user change를 보존합니다. 관련 없는 file을 덮어쓰거나, 폐기하거나, 다시 형식을 지정하지 않습니다.
- `AGENTS.md`는 프로젝트 최상위 운영 규칙, `.agents/rules/`는 작업 영역별 필수 세부 규칙, `.agents/workflows/`는 조사·계획·구현·테스트·리뷰·배포 절차, `.agents/skills/`는 특정 전문 작업 수행 지침으로 적용합니다.
- 세부 문서가 `AGENTS.md`와 충돌하면 `AGENTS.md`를 우선합니다. 동일 계층 또는 세부 문서 사이의 충돌을 해결할 수 없으면 임의로 선택하지 않고 사람에게 보고합니다.

## 5. 개발 Workflow

1. 중요한 task를 시작하기 전에 이 file과 해당 작업에 관련된 `.agents/rules/`, `.agents/workflows/`, `.agents/skills/`, `docs/` file을 확인합니다.
2. 영향을 받는 API contract, entity, security rule, storage behavior, deployment configuration을 식별합니다.
3. task가 application code, schema, secret, AWS resource, external provider setting을 변경하는지 확인합니다.
4. implementation task에서는 집중된 변경을 수행하고 같은 task에서 test를 추가하거나 갱신합니다.
5. 사용할 수 있는 가장 작은 관련 verification command를 실행하고, 실행하지 않은 command와 이유를 보고합니다.
6. behavior, API, database schema, security posture, operating procedure, deployment requirement가 변경되면 documentation을 갱신합니다.
7. final report에서 변경된 file, 수행한 validation, risk, follow-up requirement를 명시합니다.

### Cross-Repository Contract

- Backend 변경이 Frontend가 소비하는 외부 contract에 영향을 주거나 영향을 줄 가능성이 있을 때만 `C:\workspc\codex\front`의 실제 consumer source/config를 확인합니다. 내부 repository query, service/transaction/JPA 구현 세부, helper, contract를 유지하는 test refactor에는 Frontend 조사가 필요하지 않습니다.
- 확인 대상은 변경 contract의 실제 caller와 Zod schema, auth utility/OAuth callback, upload utility 등으로 제한하며, Frontend 전체 audit이나 docs만으로 consumer 동작을 추정하지 않습니다.
- endpoint path/method, request/response DTO 및 `ApiResponse`/`PageResponse`, error/status, public/authenticated 정책, OAuth/token/refresh-cookie/CORS/frontend URL, presigned upload/object key/public URL, 향후 AI-generation client contract 변경은 cross-repository contract로 취급합니다.
- Backend 작업에서 Frontend 변경 필요성을 발견해도 사용자 요청 또는 계획에 Frontend 변경이 포함되지 않으면 임의로 수정하지 않고 영향과 필요한 후속 작업만 보고합니다. 명시적인 Frontend/Backend 동시 변경에서는 양 repository의 Git 상태와 contract를 먼저 조사하고, 변경·검증 계획과 결과를 repository별로 분리해 보고하며 한쪽 성공만으로 전체 PASS로 보고하지 않습니다.

## 6. Documentation 규칙

- `docs/PROJECT.md`, `docs/ARCHITECTURE.md`, `docs/DATABASE.md`, `docs/API.md`, `docs/AWS.md`, `docs/SECURITY.md`, `docs/REPOSITORY_AUDIT.md`를 현재 repository-audit baseline으로 취급합니다.
- 실제 상태를 판단할 때는 현재 source, configuration, 실행 가능한 verification result를 우선 근거로 사용합니다. `docs/`와 불일치하면 이를 보고하고 관련 document를 갱신합니다.
- 해당 영역이 변경될 때마다 직접 영향을 받는 document를 갱신합니다.
- documentation에 secret value, token, password, private endpoint, personally identifiable production data를 기록하지 않습니다.
- confirmed implementation detail과 assumption을 분리합니다. 적용 가능한 경우 `[INFERRED]` 및 `[UNKNOWN]`을 명시적으로 사용합니다.
- architecture 및 operational document는 evidence-based로 유지하며, AWS resource, DB state, external service contract를 임의로 작성하지 않습니다.

## 7. File Modification 정책

- 요청된 task에 필요한 file만 수정합니다.
- 사용자가 명시적으로 authorization하지 않는 한 generated build output, local environment file, `.env` file, secret, local data volume을 수정하지 않습니다.
- 명시적인 authorization 없이 file을 삭제하거나 Git state를 reset하거나 user change를 덮어쓰지 않습니다.
- task에 필요하고 영향이 명시된 경우가 아니면 dependency, build tool version, framework version, Docker base image를 변경하지 않습니다. Framework, runtime, build tool의 major version 변경 또는 compatibility, operation, license 영향이 큰 dependency 변경에는 사전 authorization이 필요합니다.
- code, configuration, database migration, documentation 변경을 논리적으로 scoped되고 review 가능한 상태로 유지합니다.
- 사용자가 analysis만 요청한 경우 code, configuration, dependency, data, cloud resource, credential을 수정하지 않습니다.
- production 또는 shared environment의 deployment, service stop/restart, traffic cutover, DNS 변경, OAuth provider setting 변경, production secret 교체, 기타 service availability나 external state에 영향을 주는 작업은 target environment와 작업 scope가 명시된 사람의 authorization이 필요합니다. 단순한 source code 수정은 external environment에서 작업을 실제로 실행하는 authorization으로 해석하지 않습니다.
- external state 변경 authorization은 특정 environment, account, resource 및 작업 scope에 한정하며 포괄적이거나 영구적인 authorization으로 해석하지 않습니다.

## 8. Security 규칙

- secret value, API key, password, JWT, OAuth client secret, refresh token, MinIO credential, database credential, webhook URL을 절대 출력·commit·documentation에 붙여넣기·노출하지 않습니다.
- commit된 plaintext credential과 token logging을 security finding으로 취급하며, 해당 value를 재현하지 않습니다.
- 기능을 동작하게 하려는 목적만으로 authentication, authorization, CORS, cookie, TLS, bucket-policy, input-validation behavior를 약화하지 않습니다.
- 모든 새 endpoint와 기존 endpoint의 security-sensitive behavior 변경에는 authentication 필요 여부, role 기반 authorization, resource ownership/object-level authorization, input validation, authorization 관련 test에 대한 명시적인 decision이 있어야 합니다. 기존 endpoint 또는 unmatched-request default가 안전하다고 가정하지 않습니다.
- storage access에는 least privilege를 보존하고, user-generated 또는 private content를 추가하기 전에 public-read behavior를 평가합니다.
- OAuth, redirect URL, CORS, cookie, JWT validation, Redis refresh-token behavior를 변경할 때는 focused test를 요구하고 compatibility effect를 문서화합니다.
- production deployment 전에 repository audit에서 확인된 secret/token logging 제거, 노출 가능성이 있었던 credential의 교체 필요 여부, endpoint authorization, object storage 공개 범위, secret value를 포함하지 않은 verification result를 검토합니다. 실제 credential 교체 또는 production setting 변경에는 별도의 사람 authorization이 필요합니다.
- 해결되지 않은 중대한 security issue가 있으면 production deployment를 진행하지 않고 사람에게 보고합니다.

## 9. AWS 규칙

- AWS infrastructure는 현재 존재하지 않습니다. evidence 없이 AWS resource가 배포되었거나, 구성되었거나, 정상 상태라고 주장하지 않습니다.
- 사용자가 action을 명시적으로 authorization하지 않는 한 AWS resource를 생성, 변경, 삭제하지 않습니다.
- AWS authorization은 target account, environment, resource 및 action scope에 한정하며 포괄적이거나 영구적인 authorization으로 해석하지 않습니다.
- AWS deployment를 구현하기 전에 target account, region, network, IAM boundary, compute platform, database, Redis, object storage, DNS, TLS, logging, monitoring, backup, cost control을 설정하고 문서화합니다.
- 사용자가 target architecture를 승인한 뒤 새 AWS resource에는 version-controlled infrastructure-as-code를 우선합니다.
- production secret에는 managed secrets storage를 사용하며, production value를 Compose file, source code, repository documentation에 두지 않습니다.
- production traffic을 변경하기 전에 PostgreSQL 및 object storage의 data migration을 계획하고 검증합니다.

## 10. Database 규칙

- PostgreSQL은 application database입니다. versioned migration이 도입되기 전까지 JPA entity mapping은 현재 schema evidence입니다.
- 승인된 implementation scope에서 JPA entity 및 migration source를 작성·수정하고 격리된 local/test Database에서 migration을 검증할 수 있습니다. source 변경과 Database에서 변경을 실제 실행하는 행위를 구분합니다.
- staging/production Database의 schema, data, instance를 실제로 변경하거나 destructive migration, production data backfill, restore를 수행하려면 target environment, 작업 scope, backup 및 rollback plan을 확인한 뒤 별도의 명시적인 authorization을 받아야 합니다.
- 명시적인 user authorization 없이 `ddl-auto` behavior를 변경하지 않습니다.
- migration mechanism이 도입되면 승인된 모든 persistent-schema change에 versioned migration을 추가합니다.
- 장기 production schema migration strategy로 Hibernate `create` 또는 `update`에 의존하지 않습니다.
- 모든 schema change에 대해 constraint, index, ownership/authorization, data backfill, rollback, compatibility를 검토합니다.
- 기존 entity가 요구하는 UUID 및 `jsonb` mapping compatibility를 보존합니다.

## 11. Testing 규칙

- 이 repository에는 security, auth, service, storage behavior를 다루는 automated test source가 있습니다. 새 behavior에는 비례하는 automated test가 포함되어야 합니다.
- 해당 영역이 변경되면 domain/service logic에는 unit test를, persistence, security, API contract, storage behavior에는 integration 또는 controller test를 추가합니다.
- test가 실행되지 않았다면 변경을 verified로 표시하지 않습니다. 무엇을 실행했는지와 검증되지 않은 항목을 정확히 명시합니다.
- external system이 필요한 변경에는 안전한 local/test configuration을 사용하거나 external prerequisite를 명확히 설명합니다.
- AI-generation flow에는 provider client의 deterministic contract test와 retry, failure, idempotency, authorization, persisted state transition test가 필요합니다.

## 12. Git 규칙

- 편집 전에 `git status`를 검사하고 관련 없는 작업을 보존합니다.
- 사용자가 commit을 요청하면 집중된 conventional commit을 사용합니다. 권장 형식: `<type>(<scope>): <subject>`.
- `auth`, `user`, `book`, `storage`, `aigeneration`, `infra`, `docs` 등 repository 영역에 맞는 scope를 사용합니다.
- 명시적으로 요청되지 않는 한 `reset --hard`, force-push, history rewriting 같은 destructive Git command를 실행하지 않습니다. 명시적으로 요청된 경우에도 실행 직전에 target branch, impact scope, protected branch 여부, recovery 가능성을 확인합니다.
- generated file, local `.env` file, credential, token, build artifact, data volume을 commit하지 않습니다.
- 사용자가 다른 naming convention을 요청하지 않으면 branch 생성 시 `codex/` prefix를 사용합니다.

## 13. AI Book Development 규칙

- AI picture-book creation을 synchronous controller-only feature가 아닌 asynchronous, failure-aware workflow로 취급합니다.
- generation endpoint를 구현하기 전에 draft, generation in progress, success, selection, publication, cancellation, failure에 대한 명시적인 state transition을 정의합니다.
- product/privacy requirement가 승인한 범위에서만 user input, prompt version, generation request identity, result reference, selected output, failure metadata를 persist합니다.
- generation request를 idempotent하게 만들고 per-user 및 per-book authorization, quota, retry, cost/rate limit을 적용합니다.
- generated asset은 승인된 object-storage abstraction을 통해 저장합니다. provider credential이나 대용량 binary content를 database에 embed하지 않습니다.
- character consistency, style preset, layout template, font, page order를 generation의 명시적인 input으로 보존합니다.
- production release 전에 content-safety, copyright, retention, moderation, user-reporting decision을 추가하며, 승인되지 않은 policy area는 `[UNKNOWN]`으로 표시합니다.
- 명시적인 approval 및 documentation 없이 provider, model, vector database, queue, external AI contract를 도입하지 않습니다.

## 14. Final Report 형식

모든 implementation 또는 investigation task에 대해 아래 순서로 간결한 final report를 제공합니다.

1. **Outcome** — 완료하거나 발견한 내용입니다.
2. **Changed files** — 각 file의 absolute path와 한 줄 목적입니다. analysis-only task에는 `None`이라고 명시합니다.
3. **Validation** — 실행한 command/test와 결과입니다. 해당하는 경우 이유와 함께 `Not run`이라고 명시합니다.
4. **Security / data / infrastructure impact** — 영향이 없으면 `None`이라고 명시하며 secret은 절대 포함하지 않습니다.
5. **Risks and follow-up** — 구체적인 미해결 항목만 기록하며, 필요 시 `[INFERRED]` 또는 `[UNKNOWN]`으로 표시합니다.
