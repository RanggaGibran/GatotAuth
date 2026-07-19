# Login Pipeline Design

The GatotAuth login pipeline manages a player's lifecycle from connection handshake to successful authentication.

## 1. Pipeline Stages

```mermaid
sequenceDiagram
    actor Player
    participant Proxy as Velocity Adapter
    participant Core as GatotAuth Core
    participant DB as Storage Adapter

    Player->>Proxy: Connection Handshake
    Proxy->>Core: Pre-Login Check (IP/UUID Rate Limits)
    Core->>DB: Query Session / Registration Status
    DB-->>Core: User details
    
    alt Bedrock / Linked Account
        Core->>Core: Validate Floodgate Session
        Core-->>Proxy: Bypass Passwords (Auto-Login)
    else Premium Java Player
        Core->>Core: Verify Mojang Session
        Core-->>Proxy: Secure login (Bypass local auth)
    else Cracked / Offline Player
        Core-->>Proxy: Request Password Auth
        Player->>Proxy: Submit /login <password>
        Proxy->>Core: Process Credentials
        Core->>Core: Hash & Compare Password
    end
    
    Core-->>Proxy: Success (Spawn Player & Remove Restrictions)
```

## 2. Pipeline Design Pattern
We recommend a **Pipeline Pattern** (Chain of Responsibility) using non-blocking asynchronous interceptors:

```java
public interface LoginInterceptor {
    CompletableFuture<InterceptorResult> intercept(LoginContext context);
}
```

### Pipeline Steps:
1. **RateLimiterInterceptor**: Assesses IP connection count. Kicks if limits are exceeded.
2. **GeoIP/WhitelistingInterceptor**: Validates country or range limits.
3. **SessionAutoLoginInterceptor**: Verifies if IP and UUID match an existing active session.
4. **LinkAuthenticationInterceptor**: Intercepts Geyser/Floodgate Bedrock UUIDs to skip authentication.
5. **CredentialRequestInterceptor**: Serves the command instruction context if offline authentication is required.

## 3. Alternatives & Trade-offs
- *Alternative: Direct Event Handler*: Handle everything in a monolithic event listener class.
- *Trade-off*: Monolithic listeners lead to spaghetti code with deep nesting (`if/else`). The interceptor chain is highly modular: writing a new integration (e.g., discord linking) only requires adding a single clean interceptor.
