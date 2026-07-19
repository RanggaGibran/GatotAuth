# GatotAuth Vision & Core Philosophy

GatotAuth is a next-generation, high-performance authentication platform designed specifically for Minecraft server networks (Velocity, Paper, Folia, Geyser, Floodgate, LuckPerms, etc.).

## 1. Vision Statement
Minecraft authentication solutions have traditionally been built as tightly-coupled server plugins. This creates immense technical debt when upgrading server software, migrating databases, or extending authentication flows. GatotAuth is designed to decouple Minecraft-specific APIs from core business domain rules, ensuring it remains maintainable, secure, and extensible for at least the next 10 years.

## 2. Target Platforms
- **Velocity**: Core proxy integration for network-wide authentication.
- **Paper & Folia**: Single-server deployment, supporting modern multi-threaded environments (Folia's region-based scheduling).
- **Geyser & Floodgate**: Seamless integration with Bedrock Edition players (e.g., auto-login mechanisms for linked Xbox Live accounts).

## 3. Architecture Vision
- **Platform-Agnostic Core**: The core authentication rules, session management, and storage engine are written in pure Java, using only the Java Standard Library.
- **Adapters for Minecraft**: Paper, Velocity, and Folia are treated strictly as ingress/egress adapters (Clean Architecture / Hexagonal Architecture).
- **Modular Monolith**: Clean physical separation of API, Core, and Platform subprojects.

## 4. Key Alternatives & Architectural Trade-offs
### Alternative 1: Traditional Shared Plugin (Monolith with Platform Abstracts)
- *Pros*: Simple packaging, easy to write.
- *Cons*: High risk of API leakage, fragile when platform classes change, hard to test without mock server environments.

### Alternative 2: Microservices Authentication
- *Pros*: Decoupled completely, independent scalability.
- *Cons*: High latency overhead (bad for Minecraft join/pre-login times), complex deployment for server administrators.

### Recommended Architecture: Modular Monolith with Hexagonal Adapters (Clean Architecture)
- *Why*: Provides the logical separation of microservices without the networking/deployment overhead. Business logic is fully isolated and unit-testable in milliseconds.
