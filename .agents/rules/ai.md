# AI 그림책 규칙

## 확인된 상태

- `Book`, `Page`, `BookCharacter`, master-data entity, AI-generation log entity는 picture-book generation의 기반을 포함합니다.
- 이 저장소에는 generation controller, service, provider client, queue worker, AI-server integration이 구현되어 있지 않습니다.
- Development Compose는 external AI server와 ChromaDB를 참조하지만, source와 API contract는 이 저장소에 없습니다.

## 규칙

1. repository evidence 없이 AI generation, ChromaDB search, external AI server가 operational하다고 주장하지 않습니다.
2. AI provider, model, queue, vector database, provider contract, cost policy를 선택하기 전에 명시적인 승인을 받습니다.
3. generation을 authorized, asynchronous, idempotent, failure-aware workflow로 설계합니다.
4. 구현 전에 request identity, state transition, retry, quota, cancellation, failure handling, asset persistence를 정의합니다.
5. 승인된 book/page/character/style/layout/font input을 명시적인 generation input으로 보존합니다.
6. provider secret을 code, log, documentation, database record에 저장하지 않습니다.
7. 미해결 safety, copyright, retention, moderation, privacy requirement는 `[UNKNOWN]`으로 기록합니다.
