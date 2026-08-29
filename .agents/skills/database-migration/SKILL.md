# Database Migration Skill

## 목적

승인된 PostgreSQL schema/data migration을 안전하게 계획하거나 구현합니다.

## 확인된 기준선

- PostgreSQL은 JPA/Hibernate를 통해 사용됩니다.
- 이 repository는 Flyway와 `src/main/resources/db/migration/V1__initial_schema.sql`을 사용합니다. 적용 완료된 migration은 수정하지 않으며 이후 변경은 새 versioned migration으로 추가합니다.
- 기존 entity mapping에는 UUID와 `jsonb` field가 포함됩니다.
- 실제 database content와 backup status는 repository evidence만으로는 `[UNKNOWN]`입니다.

## 절차

1. 영향을 받는 table, column, constraint, entity mapping, repository, API contract, data volume assumption을 식별합니다.
2. schema/data를 변경하거나 migration tooling을 추가하기 전에 명시적인 승인을 받습니다.
3. forward migration, compatibility window, index/constraint behavior, backfill, rollback, verification, backup prerequisite를 정의합니다.
4. 명시적인 authorization과 정확한 target 없이는 destructive schema/data operation을 절대 사용하지 않습니다.
5. 안전하고 대표성 있는 database를 사용할 수 있을 때 해당 database를 대상으로 migration을 test합니다.
6. 확인된 migration behavior와 알려진 limitation으로 `docs/DATABASE.md`를 갱신합니다.
