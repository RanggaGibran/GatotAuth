# GatotAuth Project Roadmap

This roadmap outlines the planned development and milestones for the GatotAuth ecosystem.

## Milestone 1: Core Foundation & Velocity Proxy Integration (Completed)
- [x] Polymorphic Identity & Credential Domain Model
- [x] Asynchronous Authentication & Session Engines
- [x] SQLite & HikariCP Asynchronous Storage Layer
- [x] Database Versioned Schema Migrations
- [x] Velocity Proxy Adapter (restrictions, commands, bridging)

## Milestone 2: Spigot & Folia/Paper Platform Integration (In Progress)
- [ ] Implement Paper platform adapter for standalone game servers
- [ ] Implement proxy-agent bridge protocol for secure inter-server communication
- [ ] Implement classloader isolation in platform loaders

## Milestone 3: Advanced Credentials & Hashing Support
- [ ] TOTP MFA Credential Provider implementation
- [ ] WebAuthn (FIDO2) authentication provider integration for Web Panels
- [ ] Multi-hash algorithms conversion module (converting legacy md5/sha256 to BCrypt/Argon2)

## Milestone 4: External Database Connectors
- [ ] PostgreSQL / MySQL production storage repository support
- [ ] Redis caching backend for proxy session synchronization
