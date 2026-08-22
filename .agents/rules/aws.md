# AWS 규칙

## 확인된 상태

- 프로젝트에는 현재 AWS infrastructure가 없습니다.
- 이 저장소에는 AWS SDK, AWS IaC, AWS service configuration이 없습니다.
- 기존 deployment evidence는 self-hosted GitHub Actions runner를 사용하는 host-oriented Docker Compose deployment입니다.

## 규칙

1. 명시적인 사용자 authorization 없이 AWS resource에 접근, 생성, 수정, 삭제하지 않습니다.
2. AWS region, account, VPC, compute service, database service, DNS design, cost budget을 임의로 작성하지 않습니다.
3. 구현 전에 account, region, network, IAM, compute, PostgreSQL, Redis, object storage, TLS, DNS, monitoring, backup, cost control의 승인된 결정을 기록합니다.
4. 새 AWS resource에는 승인된 version-controlled IaC를 우선합니다.
5. production secret을 source, Compose file, documentation에서 분리하고 승인된 managed-secret solution을 사용합니다.
6. `docs/AWS.md`에는 검증된 design/implementation detail만 갱신합니다.
