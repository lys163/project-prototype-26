# 조사 Workflow

1. 요청 범위와 변경 권한을 확인하고 `git status`로 기존 변경을 보존합니다.
2. `AGENTS.md`, 관련 `.agents/rules/`, 적용되는 `docs/` baseline document를 읽습니다.
3. 요청과 관련된 정확한 Backend domain package, operational file, source/configuration/test를 조사합니다.
4. 외부 contract 영향 여부를 판단합니다.
   - 영향이 없으면 Backend 조사만 계속합니다.
   - 영향이 있으면 `C:\workspc\codex\front`에서 해당 contract의 실제 consumer만 선택적으로 조사합니다. Frontend 전체 audit은 수행하지 않습니다.
5. finding을 `[CONFIRMED]`, `[INFERRED]`, `[UNKNOWN]`으로 구분하고, 예상 변경 file 및 Frontend 영향을 정리합니다.
6. 사람이 결정할 사항과 구현 가능 여부를 정리합니다. Frontend 변경이 별도 범위이면 필요한 후속 작업으로 분리합니다.
7. analysis-only 작업에서는 documentation 생성/갱신이 명시적으로 요청된 경우를 제외하고 편집하지 않습니다.
8. 최종 결과에는 evidence, risk, 누락된 정보를 포함하되, secret value는 포함하지 않습니다.
