# Provider System

To ensure GatotAuth is fully extensible and does not lock developers into specific hashing algorithms, database backends, or notification platforms, we utilize a **Service Provider Architecture** (similar to Java's ServiceLoader pattern but managed via Dependency Injection).

## 1. Provider Interfaces

### Cryptographic Hashing Provider
Allows registering modern password hash algorithms (e.g., Argon2, bcrypt, scrypt, PBKDF2).

```java
public interface PasswordHasher {
    String name();
    String hash(String password);
    boolean verify(String password, String hashed);
}
```

### Notification / 2FA Provider
For sending verification codes (Discord, Telegram, SMTP).

```java
public interface NotificationProvider {
    CompletableFuture<Void> sendCode(User user, String code);
}
```

## 2. Alternatives & Design Choices
- *Alternative 1: Hardcoded Implementations*: Only support BCrypt and MySQL.
- *Trade-off*: Reduces codebase size but locks users out of security audits requiring Argon2, and prevents integration with proprietary custom systems.

- *Alternative 2: Service Registry / Plugin Hook*: Implement a runtime registry where third-party plugins can inject custom providers.
- *Recommended Choice*: **SPI Registry Pattern**. GatotAuth-core maintains a registry map:
  ```java
  public class HasherRegistry {
      private final Map<String, PasswordHasher> hashers = new ConcurrentHashMap<>();
      public void register(PasswordHasher hasher) { ... }
      public PasswordHasher get(String name) { ... }
  }
  ```
  This enables simple extensions without adding framework complexity.
