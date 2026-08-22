# Coding 규칙

## 기존 구조

- Application code는 `src/main/java/com/picturebook` 아래에 있습니다.
- Domain은 package 중심으로 구성되며, 필요한 경우 controller, service, repository, entity, DTO, enum package를 둡니다.
- API는 Spring MVC REST endpoint이며, 대부분 `/api` 아래에 있습니다.
- JPA entity와 repository가 현재 persistence 구현을 제공합니다.

## 규칙

1. 기존 package와 domain 경계를 따르며, 승인 없이 새 architectural style을 도입하지 않습니다.
2. controller는 HTTP concern에 집중시키고, 기존 code에 맞게 business behavior를 service/entity에 둡니다.
3. 승인된 API-contract 결정이 없는 한 새 API behavior에는 기존 response wrapper(`ApiResponse`, `PageResponse`)를 사용합니다.
4. 모든 endpoint에 authorization intent를 명시하고 route를 추가할 때 `SecurityConfig`를 검사합니다.
5. 사용자 승인 없이 dependency, framework, HTTP client, queue, external provider integration을 추가하지 않습니다.
6. 변경된 endpoint contract는 `docs/API.md`에 문서화합니다.
