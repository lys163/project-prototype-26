# Review Workflow

1. 변경을 요청된 범위 및 기존 repository convention과 비교합니다.
2. API compatibility, authorization, input validation, persistence constraint, transaction behavior, storage access, logging, error handling을 검토합니다.
3. 변경된 behavior에 대한 documentation과 test를 확인합니다.
4. secret, token logging, 안전하지 않은 public access, 우발적 dependency 변경, 관련 없는 파일 편집을 확인합니다.
5. file/line evidence로 confirmed finding을 식별하고, 불확실한 관찰은 `[INFERRED]` 또는 `[UNKNOWN]`으로 표시합니다.
6. confidence를 과장하지 않고 남은 risk와 verification gap을 보고합니다.
