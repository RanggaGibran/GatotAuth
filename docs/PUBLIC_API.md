# Public API Design

To allow external plugins to interact with GatotAuth, a stable, sealed, and version-controlled public API is exposed.

## 1. API Entry Point
Access to GatotAuth features is provided by a singleton provider instance, ensuring a decoupled access point.

```java
public interface GatotAuthApi {
    static GatotAuthApi get() {
        return GatotAuthApiProvider.get();
    }

    UserManager getUserManager();
    SessionManager getSessionManager();
    EventPublisher getEventPublisher();
}
```

## 2. API Components

### UserManager
Allows checking registration, password status, locking accounts, and creating users programmatically.

```java
public interface UserManager {
    CompletableFuture<Boolean> isRegistered(UUID uuid);
    CompletableFuture<Boolean> registerUser(UUID uuid, String name, String plainTextPassword);
    CompletableFuture<Boolean> lockAccount(UUID uuid, String reason);
}
```

### SessionManager
Enables forcing session validation, checking session validity, and invalidating sessions manually.

```java
public interface SessionManager {
    CompletableFuture<Boolean> hasActiveSession(UUID uuid, String ipAddress);
    CompletableFuture<Void> invalidateSession(UUID uuid);
}
```

## 3. Alternative Designs & Trade-offs
- *Alternative 1: Direct Core Class Access (Bukkit-style)*: Letting other plugins access the implementation instance directly.
- *Trade-off*: Means internal class changes break compatibility. The public API acts as an interface barrier. We can rewrite the core implementation entirely without breaking external integrations.
