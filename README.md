# AI 그림책 제작 플랫폼 — 백엔드 개발 가이드.

> **Tech Stack**: Spring Boot 4.x · Java 21+ · PostgreSQL 16 · Redis  
> DDD (Domain-Driven Design)

---

## 1. 브랜치 전략 (Git Flow 기반)

### 1.1 브랜치 구조

```
main (production)
 └── develop (integration)
      └── feature/{도메인}/{작업내용}
```

| 브랜치 | 용도 | 생성 기준 | 병합 대상 |
|--------|------|-----------|-----------|
| `main` | 운영 배포 코드 | — | — |
| `develop` | 통합 개발 브랜치 | `main` 에서 최초 생성 | `main` (릴리즈 시) |
| `feature/*` | 기능 개발 | `develop` | `develop` (PR) |

### 1.2 브랜치 네이밍 컨벤션

DDD 도메인 기준으로 브랜치를 분류합니다.

```
feature/user/social-login-kakao
feature/book/create-book-api
feature/page/stt-text-input
feature/character/consistency-prompt
```

### 1.3 PR 규칙

- `develop` ← `feature/*` : 최소 1명 코드 리뷰 승인 필수
- `main` ← `release/*` : 팀 전원 승인 + QA 통과
- PR 제목: `[도메인] 작업 요약` (예: `[Book] 책 생성 API 구현`)
- Squash Merge 사용 → develop 히스토리를 깔끔하게 유지

---

## 2. 커밋 컨벤션 (Conventional Commits)

### 2.1 커밋 메시지 형식

```
<type>(<scope>): <subject>

<body>        ← 선택
<footer>      ← 선택
```

### 2.2 Type 정의

| Type | 의미 | 예시 |
|------|------|------|
| `feat` | 새 기능 | `feat(book): 책 생성 API 구현` |
| `fix` | 버그 수정 | `fix(page): 페이지 순서 변경 시 중복 번호 수정` |
| `refactor` | 리팩터링 (동작 변경 없음) | `refactor(user): UserService 도메인 이벤트 분리` |
| `docs` | 문서 수정 | `docs(readme): API 명세 업데이트` |
| `test` | 테스트 추가/수정 | `test(book): BookService 단위 테스트 추가` |
| `chore` | 빌드/설정 변경 | `chore(docker): compose 네트워크 설정 변경` |
| `style` | 코드 포맷팅 | `style(global): 들여쓰기 통일` |
| `perf` | 성능 개선 | `perf(page): 페이지 목록 쿼리 N+1 해결` |

### 2.3 Scope (DDD Bounded Context 기반)

```
user, book, page, character, ai-generation, auto-save, auth, infra
```

### 2.4 커밋 예시

```
feat(book): 책 생성 및 상태 전이 도메인 로직 구현

- Book 엔티티에 상태 전이 메서드 추가 (DRAFT → IN_PROGRESS → COMPLETED)
- BookCreatedEvent 도메인 이벤트 발행
- 잘못된 상태 전이 시 InvalidBookStatusException 발생

Refs: #42
```

---

## 3. 버전 관리 (Semantic Versioning)

### 3.1 버전 형식

```
v{MAJOR}.{MINOR}.{PATCH}[-{PRE_RELEASE}]
```

| 구분 | 올리는 시점 | 예시 |
|------|-------------|------|
| MAJOR | 호환 불가 API 변경 | `v1.0.0` → `v2.0.0` |
| MINOR | 하위 호환 기능 추가 | `v1.0.0` → `v1.1.0` |
| PATCH | 하위 호환 버그 수정 | `v1.1.0` → `v1.1.1` |
| PRE_RELEASE | 릴리즈 후보 | `v1.2.0-rc.1` |

### 3.2 릴리즈 프로세스

```
1. develop에서 release/v1.2.0 브랜치 생성
2. 버전 번호 업데이트 (build.gradle)
3. QA 및 버그 수정
4. main에 머지 + 태그 생성 (v1.2.0)
5. develop에 역머지
```

## 3. DDD 기반 설계 가이드

### 3.1 Bounded Context 분리

```
┌─────────────────────────────────────────────────────┐
│                 AI 그림책 플랫폼                      │
├─────────────┬──────────────┬────────────────────────┤
│  User       │  Book        │  AI Generation         │
│  Context    │  Context     │  Context               │
│             │              │                        │
│ · User      │ · Book       │ · TextRefinementLog    │
│             │ · Page       │ · ImageGenerationLog   │
│             │ · Character  │ · CoverGenerationLog   │
│             │ · PageChar   │                        │
│             │ · AutoSave   │                        │
├─────────────┼──────────────┼────────────────────────┤
│  Auth       │  Master      │                        │
│  Context    │  Context     │                        │
│             │              │                        │
│ · JWT 발급   │ · StylePreset│                        │
│ · 소셜 로그인 │ · Layout     │                        │
│             │ · Font       │                        │
└─────────────┴──────────────┴────────────────────────┘
```

### 3.2 패키지 구조 (Hexagonal Architecture)

```
com.picturebook
├── domain/                              ← 도메인 계층 (순수 비즈니스 로직, 외부 의존성 없음)
│   ├── user/
│   │   ├── entity/                      ← Aggregate Root, Entity, Value Object
│   │   │   └── User.java
│   │   ├── enums/
│   │   │   └── SocialProvider.java
│   │   └── repository/                  ← Repository 인터페이스 (Port)
│   │       └── UserRepository.java
```

### 3.3 DDD 핵심 규칙

**Aggregate Root (AR)**

| Aggregate | Root Entity | 하위 Entity / VO |
|-----------|-------------|-------------------|
| User | `User` | — |
| Book | `Book` | `Page`, `Character`, `PageCharacter` |
| AI Generation | 각 Log가 독립 | `TextRefinementLog`, `ImageGenerationLog`, `CoverGenerationLog` |
| Auto Save | `AutoSaveSnapshot` | — |

**규칙**

1. 외부에서 Aggregate 내부 엔티티에 직접 접근하지 않는다. 반드시 AR을 통해 접근.
2. Aggregate 간 참조는 ID로만 한다 (객체 참조 X).
3. 하나의 트랜잭션에서 하나의 Aggregate만 수정한다.
4. Aggregate 간 일관성은 도메인 이벤트로 처리한다.

**도메인 이벤트 예시**

```
BookCreatedEvent        → 자동 저장 스냅샷 초기 생성
PageTextRefinedEvent    → 정제 로그 기록
ImageGeneratedEvent     → 생성 로그 기록
BookPublishedEvent      → 공유 링크 토큰 생성
```

### 3.4 엔티티 코딩 컨벤션

```java
// 1. @Entity에 @Table(name = "...") 명시
// 2. PK 전략: UUID → uuid-ossp, SERIAL → IDENTITY
// 3. 생성/수정 시각 → @CreatedDate / @LastModifiedDate (JPA Auditing)
// 4. Enum → @Enumerated(EnumType.STRING)
// 5. soft delete → @Where(clause = "is_active = true") 또는 @SQLRestriction
// 6. 비즈니스 로직은 엔티티 안에 (Rich Domain Model)
// 7. setter 금지 → 의미 있는 메서드명 사용 (changeStatus, updateProfile 등)
// 8. 생성자 접근 제한 → @NoArgsConstructor(access = PROTECTED)
// 9. 연관관계 편의 메서드는 AR 쪽에 위치
```

---

## 4. 공통 기술 가이드


### 4.1 API 응답 형식
성공응답
```json
{
  "success": true,
  "status" : 200,
  "data": {
    "id": "550e8400-...",
    "title": "나의 그림책"
  }
}
```

실패응답
```json
{
  "success": false,
  "status" : 404,
  "error": {
    "code": "BOOK_NOT_FOUND",
    "message": "해당 책을 찾을 수 없습니다."
  }
}
```

### 4.2 테스트 전략

| 계층 | 테스트 종류 | 도구 |
|------|-------------|------|
| Domain | 단위 테스트 | JUnit 5, AssertJ |
| Application | 통합 테스트 | @SpringBootTest, Mockito |
| Infrastructure | 리포지토리 테스트 | @DataJpaTest, Testcontainers |
| Presentation | API 테스트 | MockMvc, RestAssured |

---
# spring-server  
