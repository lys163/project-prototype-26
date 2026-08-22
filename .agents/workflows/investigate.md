# 조사 Workflow

1. `AGENTS.md`, 관련 `.agents/rules/`, 적용되는 `docs/` baseline document를 읽습니다.
2. 요청과 관련된 정확한 domain package와 operational file을 식별합니다.
3. feature description만 의존하지 말고 실제 source/configuration을 조사합니다.
4. finding을 `[CONFIRMED]`, `[INFERRED]`, `[UNKNOWN]`으로 보고합니다.
5. analysis-only 작업에서는 documentation 생성/갱신이 명시적으로 요청된 경우를 제외하고 편집하지 않습니다.
6. 최종 결과에는 evidence, risk, 누락된 정보를 포함하되, secret value는 포함하지 않습니다.
