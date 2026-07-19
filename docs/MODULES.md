# Module Structure & Boundaries

GatotAuth is built as a **Modular Monolith** to separate compile-time dependencies. This ensures that platform-specific libraries (like Velocity or Paper API) never bleed into core logic.

## 1. Subproject Tree

```
gatotauth (root)
├── gatotauth-api       (Domain & Ports API)
├── gatotauth-core      (Implementation, Domain Services)
└── gatotauth-platform/ (Platform Adapters)
    ├── velocity        (Velocity Proxy Adapter)
    └── paper           (Spigot/Paper/Folia Adapter)
```

## 2. Module Responsibilities

### `gatotauth-api`
- **Output**: Java Library JAR containing interfaces, record types, and events.
- **Allowed Dependencies**: Zero external dependencies (only standard Java libs, logger API, annotations).
- **Core Interfaces**: `UserRepository`, `SessionManager`, `EventPublisher`, `PasswordHasher`.

### `gatotauth-core`
- **Output**: Library JAR containing implementation models.
- **Allowed Dependencies**: `gatotauth-api`, dependency injection engines, database driver APIs (JDBC/R2DBC), cryptographic libraries.
- **Responsibilities**: Authentication Use Cases, password hashing algorithms, database queries, cache synchronization.

### `gatotauth-platform-velocity`
- **Output**: Velocity Plugin JAR.
- **Allowed Dependencies**: `gatotauth-core`, Velocity API, Floodgate/Geyser API.
- **Responsibilities**: Listens to proxy pre-login/post-login connection stages, kicks unauthorized players, schedules network protocol actions.

### `gatotauth-platform-paper`
- **Output**: Paper/Folia Plugin JAR.
- **Allowed Dependencies**: `gatotauth-core`, Paper API, Folia scheduling API.
- **Responsibilities**: Listens to single-server events, blocks player actions (movement, chat, blocks) until authenticated.

## 3. Alternative Designs & Trade-offs
- *Alternative*: All-in-one single-project repository with packages separating platforms.
- *Trade-off*: Developers will accidentally import Velocity classes in Paper adapters or vice versa. Splitting into strict subprojects makes classpath leakage impossible.
