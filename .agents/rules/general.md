# 일반 규칙

## 프로젝트 사실

- 이 저장소는 AI 그림책 서비스의 Java/Spring Boot 백엔드입니다.
- Java 21, Spring Boot 4, Gradle, PostgreSQL, Redis, MinIO, Docker Compose를 사용합니다.
- 이 저장소에는 frontend source가 없습니다.
- 이 저장소에는 backend search 구현이 없습니다.
- AI-generation entity는 존재하지만, AI text/image generation 실행은 여기에서 구현되어 있지 않습니다.
- AWS infrastructure는 현재 존재하지 않습니다.

## 작업 규칙

1. 중요한 작업 전에 `AGENTS.md`와 관련 `docs/` 파일을 읽습니다.
2. 기능 또는 behavior를 제안하거나 변경하기 전에 영향을 받는 code와 configuration을 조사합니다.
3. 좁은 범위의 변경을 유지하고 관련 없는 사용자 변경을 보존합니다.
4. 저장소 사실은 confirmed로 명시하고, 가정은 `[INFERRED]`, 이용할 수 없는 정보는 `[UNKNOWN]`으로 표시합니다.
5. 없는 기능이나 external system을 구현된 것처럼 설명하지 않습니다.
6. 조사만 요청된 작업에서는 명시적으로 요청된 documentation 외에 파일을 수정하지 않습니다.
