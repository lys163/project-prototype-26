# Test Workflow

1. 변경된 behavior와 적용되는 unit, integration, controller, security, persistence, storage, provider-contract test를 식별합니다.
2. test에 필요한 external dependency를 확인하며, 실제 secret을 사용하거나 노출하지 않습니다.
3. 먼저 targeted test를 실행하고, 안전하고 관련 있을 때 더 넓은 check를 실행합니다.
4. command, outcome, 건너뛴 check, 이유를 기록합니다.
5. API, database, security, external behavior가 변경되었을 때 compilation만으로 성공을 주장하지 않습니다.
6. test output과 fixture에서 credential 및 token이 없도록 유지합니다.
