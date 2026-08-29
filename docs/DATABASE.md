# Database

## 확인된 database technology

[CONFIRMED] application은 PostgreSQL을 사용하도록 구성되어 있으며 Spring Data JPA와 Hibernate를 통해 접근합니다.

[CONFIRMED] default configuration은 local PostgreSQL JDBC URL을 사용합니다. Development Compose는 PostgreSQL container를 사용합니다. Production Compose는 Compose stack 외부의 PostgreSQL database를 전제로 합니다.

[CONFIRMED] Default configuration, development Compose, host-oriented Compose는 `spring.jpa.hibernate.ddl-auto=validate`를 사용합니다. Hibernate는 Entity와 database schema의 호환성을 검증하며 schema를 생성·갱신하지 않습니다.

[CONFIRMED] Flyway를 schema migration 도구로 사용합니다. Migration은 `src/main/resources/db/migration/`에 두며, 현재 `V1__initial_schema.sql`은 빈 PostgreSQL database에 application schema를 생성합니다.

## Entity에서 도출한 schema

[CONFIRMED] 다음 table은 JPA entity로 mapping됩니다.

| 영역 | Table |
| --- | --- |
| User | `users` |
| Book | `books`, `pages`, `characters`, `page_characters` |
| AI log | `text_refinement_logs`, `image_generation_logs`, `cover_generation_logs` |
| Draft 이력 | `auto_save_snapshots` |
| Master data | `categories`, `style_presets`, `layout_templates`, `fonts` |
| Content/community | `banners`, `reviews`, `reports`, `book_likes`, `author_follows` |
| Reader/commerce | `reading_progresses`, `user_reading_goals`, `purchases` |

## 주요 relationship 및 constraint

[CONFIRMED]

- `books`는 JPA relationship을 통해 `pages`와 `characters`를 소유하며, `pages`와 `characters`는 `page_characters`를 통해 연결됩니다.
- `users`에는 `email`과 `provider` 및 `provider_id` 조합에 대한 unique constraint가 있습니다.
- `books.share_link_token`은 unique합니다.
- `pages`에는 book 및 page number에 대한 unique constraint가 있습니다.
- `purchases`, `book_likes`, `reviews`, `author_follows`, `reading_progresses`, `user_reading_goals`는 entity mapping에 domain-specific uniqueness constraint를 정의합니다.
- `Book.userId`, `Purchase.bookId`, AI-log ID 같은 다수의 reference는 JPA entity association이 아니라 scalar UUID/ID field입니다.
- `auto_save_snapshots.snapshot_data`, `pages.layout_override`, `layout_templates.layout_config`는 PostgreSQL `jsonb`로 mapping됩니다.
- 대부분의 primary key는 generated UUID이며, category/style/layout/font ID는 identity generation을 사용합니다.

## Audit timestamp

[CONFIRMED] `BaseCreatedEntity`는 `created_at`을 제공합니다. `BaseTimeEntity`는 추가로 `updated_at`을 제공합니다. JPA auditing은 `JpaConfig`로 활성화됩니다.

## Migration 운영 원칙

[CONFIRMED]

- Migration file은 `V<번호>__<설명>.sql` 형식을 사용합니다. 예: `V2__add_book_slug.sql`.
- `V1__initial_schema.sql`은 UUID application/Hibernate generation, identity primary key, `jsonb`, 현재 FK 범위, enum CHECK 및 index를 명시적으로 보존합니다.
- 현재 repository policy는 Flyway `baseline` 및 `baselineOnMigrate`를 사용하지 않습니다. V1은 빈 database에 적용해야 합니다.
- 적용 완료된 migration file은 수정하지 않습니다. 이후 schema 변경은 새 `V2` 이상 migration으로 roll-forward 합니다.
- schema migration과 reference-data migration은 분리합니다. V1에는 reference-data seed, INSERT 또는 backfill이 포함되지 않습니다.
- 운영 또는 미래 외부 database를 변경하기 전에는 backup, representative validation, target environment와 작업 범위에 대한 명시적 승인이 필요합니다.

[UNKNOWN] Host-oriented Compose가 연결하는 외부 PostgreSQL의 현재 schema, data, migration history, backup 상태는 이 repository에서 확인할 수 없습니다. Flyway 도입 시 해당 Compose를 실행하지 않았습니다.

## 확인되지 않은 database 상태

[UNKNOWN] 이 repository에는 database dump, migration history, connection information, schema snapshot, seed data, record count, backup, production database가 없습니다.

[CONFIRMED] 사용자 확인에 따라 현재 deployed infrastructure는 없습니다. 이 repository에도 AWS infrastructure definition이 없습니다.

[UNKNOWN] 실제 AWS RDS database 또는 다른 외부 database resource의 존재·상태는 repository만으로 확인할 수 없습니다.
