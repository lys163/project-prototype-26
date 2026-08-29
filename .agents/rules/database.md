# Database 규칙

## 확인된 상태

- PostgreSQL은 Spring Data JPA와 Hibernate를 통해 구성되어 있습니다.
- 현재 entity mapping이 repository의 schema evidence입니다.
- repository는 Flyway와 `src/main/resources/db/migration/V1__initial_schema.sql`을 사용합니다. 적용 완료된 V1은 수정하지 않으며 이후 schema 변경은 새 versioned migration으로 추가합니다.
- 대부분 identifier에는 UUID를 사용하며, 일부 master-data entity는 identity ID를 사용합니다.
- 기존 mapping은 선택된 field에 PostgreSQL `jsonb`를 사용합니다.

## 규칙

1. 명시적인 authorization 없이 schema, production data, `ddl-auto`, database instance를 변경하지 않습니다.
2. persistence behavior를 변경하기 전에 entity, repository, constraint, 영향을 받는 API contract를 검사합니다.
3. migration, data backfill, index, rollback, compatibility를 명시적 design item으로 취급합니다.
4. database content, backup state, connection detail, production schema state를 가정하지 않으며, 보고 시 `[UNKNOWN]`으로 표시합니다.
5. 승인된 schema 변경에는 새 versioned migration을 만들고 `docs/DATABASE.md`에 문서화합니다.
