# Deploy Workflow

## 현재 경계

- 현재 repository evidence는 self-hosted GitHub Actions runner를 통한 host-oriented Docker Compose deployment입니다.
- AWS infrastructure는 존재하지 않습니다.

## 절차

1. 사용자가 명시적으로 허가하지 않는 한 cloud resource를 deploy하거나 변경하지 않습니다.
2. deployment 전에 target environment, artifact version, 승인된 configuration source, secret source, database migration state, backup, rollback, health check, monitoring을 확인합니다.
3. report 또는 log에 secret value를 절대로 복사하거나 표시하지 않습니다.
4. 향후 AWS deployment에는 실행 전에 승인된 architecture 및 version-controlled infrastructure definition이 필요합니다.
5. 승인된 non-destructive check로 application health, API availability, authentication redirect, storage access, log, monitoring을 검증합니다.
6. 실제 deployment action, 관찰 결과, rollback readiness, 남은 `[UNKNOWN]` 항목을 보고합니다.
