# 구현 Workflow

1. 작업이 authorized인지 확인하고 편집 전에 `git status`를 검사합니다.
2. 변경 전에 관련 domain code, `SecurityConfig`, entity/repository mapping, configuration을 읽습니다.
3. 필요하고 집중된 편집만 수행합니다.
4. 변경된 behavior와 함께 test를 추가하거나 갱신합니다.
5. confirmed implementation evidence를 사용해 영향을 받는 `docs/` 파일을 갱신합니다.
6. 명시적 authorization 없이 `.env`, secret, data, AWS resource, 관련 없는 파일을 수정하지 않습니다.
7. 가장 작은 관련 build/test command로 validation하고 정확한 결과를 보고합니다.
