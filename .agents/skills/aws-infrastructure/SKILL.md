# AWS Infrastructure Skill

## 목적

이 프로젝트를 위해 승인된 AWS infrastructure 계획 또는 구현을 준비합니다.

## 사전 조건

- AWS infrastructure는 현재 존재하지 않습니다.
- 명시적인 사용자 승인 없이 AWS credential을 사용하거나 AWS API call을 수행하지 않습니다.
- 승인된 결정 없이 AWS service, region, account boundary, cost limit을 선택하지 않습니다.

## 절차

1. `docs/AWS.md`, `docs/SECURITY.md`, `docs/DATABASE.md`, Compose file, deployment workflow를 읽습니다.
2. 기존 host-oriented dependency인 application, PostgreSQL, Redis, MinIO, monitoring, OAuth redirect/CORS requirement, GitHub Actions deployment의 목록을 작성합니다.
3. 필요한 결정인 account, region, VPC, IAM, compute, managed service, DNS, TLS, secret, backup, observability, budget, migration cutover를 `[UNKNOWN]`으로 식별합니다.
4. IaC 또는 resource를 생성하기 전에 승인을 받을 architecture 및 migration plan을 작성합니다.
5. 승인된 구현 후에는 검증된 resource와 validation result만 `docs/AWS.md`에 문서화합니다.
