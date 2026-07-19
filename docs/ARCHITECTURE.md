# GatotAuth System Architecture

GatotAuth is structured around **Clean Architecture** (Ports and Adapters / Hexagonal Architecture). The goal is to keep the core domain completely decoupled from external frameworks, database drivers, and Minecraft server platforms.

```mermaid
graph TD
    subgraph Platform Adapters (Velocity, Paper, Folia)
        V[Velocity Adapter]
        P[Paper/Folia Adapter]
    end

    subgraph GatotAuth Core (Use Cases & Implementation)
        UC[Auth Use Cases]
        S[Session Manager]
        DB[Database Adapters]
    end

    subgraph GatotAuth API (Domain & Interfaces)
        D[Domain Models]
        E[Domain Events]
        PI[Port Interfaces]
    end

    V --> UC
    P --> UC
    UC --> D
    UC --> PI
    DB --> PI
```

## 1. Architectural Layers

### Domain Layer (`gatotauth-api`)
- Contains pure business entities, value objects, and domain events.
- Zero dependencies on any Minecraft or database libraries.
- Standard Java only (Java Records, sealed interfaces).

### Use Case / Core Layer (`gatotauth-core`)
- Contains orchestration logic (e.g., executing a login flow, validating passwords, managing sessions).
- Defines *Ports* (interfaces) for external infrastructure (e.g., `UserRepository`, `PasswordHasher`, `SessionStorage`).

### Infrastructure & Platform Adapters (`gatotauth-platform-*`)
- Implements the *Ports* (e.g., `SQLUserRepository`, `BCryptPasswordHasher`).
- Listens to Minecraft login/pre-login events, maps Minecraft players to domain entities, and handles player movements/network packets.

## 2. Alternatives & Trade-offs

### Alternative: Layered Architecture (Domain -> Services -> Repository -> DB)
- *Pros*: Standard Java pattern, easy to grasp.
- *Cons*: Database models leak into services, making domain testing hard and database migrations complex.

### Recommended: Hexagonal (Ports & Adapters)
- *Why*: Isolates domain logic completely. We can change storage from MySQL to MongoDB or change hashing from BCrypt to Argon2 without touching any authentication business logic.
