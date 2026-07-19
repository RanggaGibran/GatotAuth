# GatotAuth API Contracts & Stability Guidelines

This document outlines the public contract specifications, error models, threading rules, and stability classifications for all developer-facing APIs in GatotAuth.

---

## 1. Stability Classifications & SemVer

To protect third-party integrations, we classify every class and interface using the following visibility annotations:

- **@ApiStatus.Stable**: Production-ready. Will remain backwards compatible across the current major version (e.g. `1.x.x`).
- **@ApiStatus.Experimental**: Preview features. API signature may change in minor versions without deprecation cycles.
- **@ApiStatus.Internal**: Implementation classes. Not accessible by third-party loaders.
- **@ApiStatus.ScheduledForRemoval**: Deprecated APIs that will be removed in the next major version.

### Semantic Versioning Rules
- **Major (X.0.0)**: Breaking changes in `@ApiStatus.Stable` interfaces.
- **Minor (1.Y.0)**: Backwards-compatible additions to `@ApiStatus.Stable` or changes to `@ApiStatus.Experimental`.
- **Patch (1.0.Z)**: Backwards-compatible bug fixes.

---

## 2. API Contract Specifications

### 1. Authentication API (`@ApiStatus.Stable`)
- **Purpose**: Processes credentials and moves players through the validation pipeline.
- **Responsibilities**: Validates user identity and emits login tokens.
- **Inputs**: `UUID clientUuid`, `String plainTextCredentials`, `IPAddress ip`.
- **Outputs**: `CompletableFuture<AuthResult>` (where `AuthResult` is a sealed interface containing `Success` or `Failure(Reason)`).
- **Lifecycle**: Active only during player pre-login/login connection stages.
- **Error Cases**: `AccountLockedException`, `InvalidCredentialsException`, `RateLimitExceededException`.
- **Thread Safety**: Fully thread-safe. Execution runs off-thread.
- **Performance**: Password verification must execute in < 250ms (for Argon2id).

### 2. Session API (`@ApiStatus.Stable`)
- **Purpose**: Governs active login sessions and validation bypasses.
- **Responsibilities**: Checks session authenticity and invalidates tokens.
- **Inputs**: `UUID clientUuid`, `IPAddress ip`.
- **Outputs**: `CompletableFuture<SessionValidationResult>` (Valid / Invalid / IP_Mismatch).
- **Lifecycle**: Maintained for the duration of the player session until TTL or disconnect.
- **Error Cases**: `SessionExpiredException`, `SessionNotFoundException`.
- **Thread Safety**: Safe for concurrent asynchronous reads.
- **Performance**: Sub-1ms response via in-memory cache/Redis.

### 3. Account API (`@ApiStatus.Stable`)
- **Purpose**: Controls registrations and account management.
- **Responsibilities**: Changes passwords, locks/unlocks users, registers new credentials.
- **Inputs**: `UUID clientUuid`, `PasswordHash newHash` OR `String plainPassword`.
- **Outputs**: `CompletableFuture<Void>`.
- **Lifecycle**: Persistent. Modifies long-term database storage.
- **Error Cases**: `AccountAlreadyExistsException`, `WeakPasswordException`.
- **Thread Safety**: Thread-safe (guaranteed via transaction lock boundaries in the core layer).
- **Performance**: Under 10ms for lookups, database writing bounded by driver speed.

### 4. Identity API (`@ApiStatus.Stable`)
- **Purpose**: Identifies player platform origins (Premium vs Offline vs Bedrock/Floodgate).
- **Responsibilities**: Resolves login checks.
- **Inputs**: `UUID clientUuid` or `String username`.
- **Outputs**: `CompletableFuture<IdentityType>` (Premium, Offline, Floodgate).
- **Lifecycle**: Read-only lookup.
- **Error Cases**: `UserNotFoundException`.
- **Thread Safety**: Thread-safe.
- **Performance**: Sub-1ms via local memory cache.

### 5. Security API (`@ApiStatus.Experimental`)
- **Purpose**: Dynamically adjusts lockout and rate limiting rules.
- **Responsibilities**: Declares suspect IPs or overrides active lockouts.
- **Inputs**: `IPAddress ip`, `Duration banDuration`.
- **Outputs**: `CompletableFuture<Void>`.
- **Lifecycle**: Dynamic memory updates.
- **Error Cases**: `InvalidIpException`.
- **Thread Safety**: Thread-safe (Atomic state modifications).
- **Performance**: O(1) in-memory lookup.

### 6. Provider API (`@ApiStatus.Stable`)
- **Purpose**: Service registry hook for hashing and notifications.
- **Responsibilities**: Registers custom algorithms or 2FA senders.
- **Inputs**: `PasswordHasher hasher` or `NotificationProvider provider`.
- **Outputs**: `RegistrationResult`.
- **Lifecycle**: Invoked during plugin enable cycle. Cannot replace providers while sessions are active.
- **Error Cases**: `DuplicateProviderException`, `IllegalLifecycleException`.
- **Thread Safety**: Single-threaded write (called from main thread during startup), multi-threaded concurrent read.
- **Performance**: Instant lookup in HashMaps.

### 7. Event API (`@ApiStatus.Stable`)
- **Purpose**: Publishes and subscribes to authentication actions.
- **Responsibilities**: Dispatches events to downstream plugins.
- **Inputs**: `Class<T> eventClass`, `EventListener<T> listener`.
- **Outputs**: `SubscriptionHandler`.
- **Lifecycle**: Active during plugin runtime.
- **Error Cases**: None.
- **Thread Safety**: Thread-safe event submission and subscription additions.
- **Performance**: Less than 1 microsecond dispatch overhead per subscriber.

### 8. Storage API (`@ApiStatus.Experimental`)
- **Purpose**: Registers custom repositories for identity storage.
- **Responsibilities**: Overrides native SQLite/MySQL/PostgreSQL lookups.
- **Inputs**: `UserRepository customRepo`.
- **Outputs**: `Void`.
- **Lifecycle**: Invoked during startup before database connection cycles start.
- **Error Cases**: `IllegalLifecycleException` (if database is already active).
- **Thread Safety**: Must be fully thread-safe.
- **Performance**: High performance expectations; must never block returning threads.

### 9. Extension API (`@ApiStatus.Stable`)
- **Purpose**: Controls loader hooks for extensions.
- **Responsibilities**: Mounts configs and resources to separate plugins.
- **Inputs**: `GatotAuthExtension extension`.
- **Outputs**: `ExtensionContext`.
- **Lifecycle**: Governed by platform plugin enable/disable sequences.
- **Error Cases**: `ExtensionLoadException`.
- **Thread Safety**: Single-threaded execution during load/unload sequences.
- **Performance**: Negligible overhead.
