# Storage Subsystem & Data Access

The GatotAuth storage subsystem manages long-term database connections, schema updates (migrations), and data access patterns.

## 1. Storage Interface (Port)
The core uses `UserRepository` and `SessionRepository` ports. All operations are fully asynchronous and return `CompletableFuture`.

```java
public interface UserRepository {
    CompletableFuture<Optional<User>> findByUuid(UUID uuid);
    CompletableFuture<Optional<User>> findByName(String name);
    CompletableFuture<Void> save(User user);
}
```

## 2. Alternatives & Trade-offs

### Migration Engines:
- *Alternative 1: Flyway / Liquibase*
  - *Pros*: Complete feature-rich migrations, handles multi-database differences easily.
  - *Cons*: Giant jar dependencies, increases plugin startup overhead significantly.
- *Alternative 2: Native Lightweight SQL Script Executor*
  - *Why*: GatotAuth uses a custom, lightweight SQL runner that parses files in `/db/migration/*.sql` and tracks schema versions in a local metadata table. This saves ~5-10MB in package size and initiates sub-10ms startups.

### Supported Databases:
- **H2 / SQLite**: Embedded databases for single-server setups (zero configuration).
- **PostgreSQL / MySQL / MariaDB**: Shared databases using connection pools (HikariCP) for large network scaling.
- **MongoDB**: Document-store adapter for schemaless architectures.
