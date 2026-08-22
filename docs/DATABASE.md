# Database

## 확인된 database technology

[CONFIRMED] application은 PostgreSQL을 사용하도록 구성되어 있으며 Spring Data JPA와 Hibernate를 통해 접근합니다.

[CONFIRMED] default configuration은 local PostgreSQL JDBC URL을 사용합니다. Development Compose는 PostgreSQL container를 사용합니다. Production Compose는 Compose stack 외부의 PostgreSQL database를 전제로 합니다.

[CONFIRMED] Default 및 development setting은 `spring.jpa.hibernate.ddl-auto=create`를 사용하며, production Compose는 `SPRING_JPA_HIBERNATE_DDL_AUTO=update`를 설정합니다.

[CONFIRMED] Flyway, Liquibase, SQL migration directory, migration script는 발견되지 않았습니다.

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

## 확인되지 않은 database 상태

[UNKNOWN] 이 repository에는 database dump, migration history, connection information, schema snapshot, seed data, record count, backup, production database가 없습니다.

[CONFIRMED] 사용자는 현재 AWS infrastructure가 없다고 밝혔습니다. 따라서 AWS RDS database는 기존 project resource가 아닙니다.
