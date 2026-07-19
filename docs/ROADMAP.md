# Project Roadmap

The GatotAuth release cycles are divided into five phases to ensure complete structural validation before adding plugin compatibility.

## Phase 1: API & Core Architecture (Current)
- [x] Initial Gradle modular project config.
- [x] Checkstyle, Spotless, Jacoco, CI/CD pipelines.
- [x] Comprehensive architectural design documentation.
- [ ] Implement domain models, value objects, and repository ports.
- [ ] Implement basic unit tests for domain validations.

## Phase 2: Core Logics & Storage Engines
- [ ] Implement Argon2id password hash provider.
- [ ] Implement database adapters (H2, MySQL, PostgreSQL, SQLite).
- [ ] Setup migration runner for automatic schema upgrades.
- [ ] Integrate Testcontainers test suite for database checks.

## Phase 3: Session & Cache Cluster
- [ ] Implement Local Caffeine cache for sessions.
- [ ] Implement Redis-based distributed session engine.
- [ ] Setup cache invalidation pub/sub system.

## Phase 4: Platform Adapters (Velocity & Paper)
- [ ] Implement PacketEvents listener pipelines for protocol locks.
- [ ] Build Velocity connection adapters (Mojang premium check + Bedrock check).
- [ ] Build Paper/Folia player tracking adapters.
- [ ] Implement LuckPerms context providers (authenticated vs guest permissions).

## Phase 5: Extension SDK & Advanced Security
- [ ] Build extension loader (SPI classloaders).
- [ ] Setup Multi-factor (MFA/2FA) email & Discord adapters.
- [ ] Public API packaging & developer documentation.
