# Testing Skill

## 목적

backend 변경에 비례하는 verification을 추가하고 실행합니다.

## 확인된 기준선

- Gradle은 build tool이며 `test`는 JUnit Platform을 사용하도록 구성되어 있습니다.
- security, auth, service, storage behavior를 다루는 test source directory가 존재합니다.
- 변경을 verified로 보고할 때는 해당 checkout에서 실제 실행한 focused test와 broader build/test 결과를 구분합니다.

## 절차

1. 변경된 behavior를 unit, service, persistence, controller/API, security, external-contract test requirement에 연결합니다.
2. 승인된 각 behavior 변경과 함께 focused test를 추가합니다.
3. 안전하고 사용할 수 있는 가장 작은 관련 Gradle test/build command를 실행합니다.
4. test output, fixture, assertion에 configuration secret을 노출하지 않습니다.
5. 실행한 command, result, 검증되지 않은 prerequisite를 정확하게 보고합니다.
6. AI 또는 storage behavior에서는 timeout, failure, retry, authorization, idempotency, persisted state를 다룹니다.
