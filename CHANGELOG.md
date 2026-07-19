# Changelog

All notable changes to GatotAuth will be documented in this file. This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0-SNAPSHOT] - 2026-07-19

### Added
- **Polymorphic Domain Model**: Implemented the decoupled Option D aggregate structure where `Account` owns independent lists of identities, credentials, devices, sessions, and security policies.
- **Asynchronous Authentication Engine**: Created a fully non-blocking authentication engine executing Argon2 and BCrypt validation pipelines offloaded onto compute executors.
- **Session Manager Engine**: Developed a core session lifecycle service handling expiration, sliding renewals, and session events.
- **SQLite Storage Driver**: Implemented an async SQL storage provider using HikariCP connection pooling and WAL caching settings.
- **Database Migrations System**: Introduced automatic database versioned schema migrations execution.
- **Velocity Platform Adapter**: Delivered a Velocity proxy adapter handling connection event translations, restriction interceptors, and proxy auth commands (`/login`, `/register`).
