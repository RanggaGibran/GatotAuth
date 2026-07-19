# Architecture Decision Records (ADR)

This document tracks major architectural decisions, the alternatives evaluated, and their trade-offs.

## ADR-001: Bootstrap Class Loading & Classloader Isolation

### Context
GatotAuth depends on multiple heavy external libraries (HikariCP, Redisson, Caffeine, Argon2, etc.). When deploying GatotAuth on Paper or Velocity servers, these dependencies can clash with other server plugins containing different versions of the same classes, leading to runtime exceptions (e.g. `LinkageError`, `NoSuchMethodError`).

### Decision
Implement a custom Bootstrap Loader system. The main JAR files loaded by the platforms (`velocity-loader.jar` and `paper-loader.jar`) will contain only a tiny bootstrap class that sets up a private isolated classloader and loads the actual platform and core implementations at runtime, relocating dependency namespaces to `dev.gatotauth.lib.*`.

### Alternatives
- **Alternative 1: Direct Gradle Shadow Jar (No relocation)**
  - *Pros*: Simple compilation step.
  - *Cons*: Class conflicts persist since class names are unchanged.
- **Alternative 2: Relocated Shadowing**
  - *Pros*: Solves class clash.
  - *Cons*: Giant artifact sizes and IDE indexing issues.
- **Alternative 3 (Chosen): Dynamic runtime downloader and loader isolation**
  - *Pros*: Absolute isolation, keeps plugin Jars small, completely prevents classpath leakage.

---

## ADR-002: Modular Monolith Repository Redesign

### Context
To support multi-proxy environments, the Paper platform needs to operate as a lightweight agent (Proxy-Agent mode) that trusts the proxy's authentication state, rather than spinning up full SQL database connections and hashing libraries on every backend server. Additionally, PacketEvents interception must be shared rather than duplicated.

### Decision
Redesign the repository into the following module groups:
1. `gatotauth-api`: Core interfaces (zero dependencies).
2. `gatotauth-core`: Domain implementation & storage.
3. `gatotauth-common`: Shared utility and configurations.
4. `gatotauth-protocol`: Centralized packet interception.
5. `gatotauth-platforms`: Adapters for target servers (Velocity proxy, Paper standalone, Paper proxy-agent).
6. `gatotauth-loaders`: Bootstrap classloaders for deployment.

### Alternatives
- **Alternative 1: Keep flat 4-module layout**
  - *Pros*: Simpler Gradle script.
  - *Cons*: Paper platform is locked into running a heavy database node, causing scaling and sync issues.
