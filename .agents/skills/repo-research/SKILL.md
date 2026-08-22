# Repository Research Skill

## 목적

feature, migration, operational change를 계획하기 전에 이 repository를 조사합니다.

## 절차

1. `AGENTS.md`와 `docs/`의 관련 document를 읽습니다.
2. 직접 관련된 controller, service, repository, entity, DTO, security, configuration, Compose, workflow file을 검사합니다.
3. source evidence를 사용하여 `[CONFIRMED]`, `[INFERRED]`, `[UNKNOWN]`을 구분합니다.
4. 조사만 요청된 경우 file을 수정하지 않습니다.
5. configuration에서 발견한 secret value를 출력하지 않습니다.

## 프로젝트별 확인 사항

- 주장된 capability가 `src/main/java`에 존재하는지 검증합니다. 현재 search는 없습니다.
- AI execution이 실제로 구현되어 있는지 검증합니다. 현재 AI entity만으로는 구현되었다고 볼 수 없습니다.
- local execution을 재현 가능하다고 보기 전에 external Compose path를 검증합니다.
- AWS 관련 주장을 repository evidence와 대조하여 검증합니다. 현재 여기에는 AWS infrastructure가 정의되어 있지 않습니다.
