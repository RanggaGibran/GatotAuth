# Testing Strategy

GatotAuth mandates a rigorous testing pipeline to guarantee long-term stability and prevent regression bugs.

## 1. Test Levels

### Unit Tests
- Placed in `src/test/java`.
- Used to test pure domain aggregates, value objects, and business logic interceptors.
- Targets: 100% test coverage on `gatotauth-api` and core domain models.
- Mocking: Mockito is used to simulate repository outputs.

### Integration Tests
- Uses **Testcontainers** to spin up real containers for MySQL, PostgreSQL, and Redis.
- Verifies database migrations, repository implementations, cache failovers, and index lookups.

### CI/CD Quality Gates
- **Spotless**: Enforces code style formatting.
- **Checkstyle**: Restricts naming conventions, imports, and block structure rules.
- **Jacoco**: Fails the build if code coverage drops below 80% on core components.

## 2. Alternatives & Trade-offs
- *Alternative: Mock database layers using memory databases (H2/HSQLDB)*
  - *Pros*: Faster test runs, does not require Docker.
  - *Cons*: SQL dialect features (e.g. JSON types, Upserts) differ from real MySQL or PostgreSQL, causing false positives.
  - *Recommended Option*: Testcontainers. Ensures database code is validated against identical systems used in production.
