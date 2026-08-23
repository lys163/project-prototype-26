# 계획 Workflow

1. confirmed repository behavior에서 시작하고, 누락된 모든 결정은 `[UNKNOWN]`으로 표시합니다.
2. 영향을 받는 API, security, database, storage, documentation, test, deployment, AWS 영역을 나열합니다.
3. application 변경을 infrastructure 또는 external-provider 결정과 분리합니다.
4. AWS, database, AI provider 결정은 target design을 가정하기 전에 승인을 위해 중단합니다.
5. 구현 전에 validation 및 rollback 기대사항을 정의합니다.
6. 승인된 breaking change가 없는 한 계획을 작고 순서가 있으며 기존 user/data와 compatible하게 유지합니다.
7. Backend 외부 contract 변경이면 관련 Frontend consumer 영향과 검증 항목을 계획에 기록합니다. Frontend 변경이 별도 범위이면 후속 작업으로 분리하고, 명시적 동시 변경이면 양 repository의 변경·검증 계획을 분리합니다.
