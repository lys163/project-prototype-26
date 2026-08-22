# AI Story Generation Skill

## 목적

AI picture-book workflow의 text/story-generation 부분을 계획하거나 구현합니다.

## 확인된 기준선

- AI log entity에는 text-refinement record가 포함됩니다.
- `Book`과 `Page`에는 story/page content를 저장할 수 있는 field가 포함됩니다.
- 이 repository에는 AI story-generation API, service, provider client, asynchronous worker가 없습니다.

## 절차

1. 승인된 user input, output contract, provider, model, cost limit, safety policy, retention policy를 식별하며, 누락된 결정은 `[UNKNOWN]`입니다.
2. authorized ownership과 idempotent request identity를 정의합니다.
3. persisted lifecycle state, retry, cancellation, failure behavior, audit/logging requirement를 정의합니다.
4. 승인된 abstraction을 통해 integration하며, provider secret을 source 또는 log에 두지 않습니다.
5. success, provider failure, retry/idempotency, authorization, persistence에 대한 test를 추가합니다.
6. 실제로 변경된 API, database, architecture, security, AI documentation을 갱신합니다.
