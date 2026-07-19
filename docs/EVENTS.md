# Event-Driven Architecture

GatotAuth relies on a decoupled, asynchronous event-driven design. This allows external plugins to listen to authentication actions without injecting dependencies directly into core authentication classes.

## 1. Event Propagation Flow

```
[Core UseCase Action] ──> publish(DomainEvent)
                            │
                            ├──> Internal Asynchronous Listeners
                            │
                            └──> Platform Adapters (Maps to Velocity/Paper Plugin Events)
```

## 2. Event Types

### Domain Events (`gatotauth-api` / internal)
Fired in the core business logic layer. Completely decoupled from Minecraft.
- Examples: `UserRegistrationEvent`, `UserLoginEvent`, `UserLogoutEvent`, `AuthFailedEvent`.

### Platform Events (external)
Adapters catch domain events and re-trigger platform-native events (e.g. `VelocityGatotAuthLoginEvent` implementing Velocity's API) so downstream plugins can hook into the authentication flow easily.

## 3. Alternative Dispatchers
- *Alternative 1: Guava EventBus*
  - *Pros*: Built-in, easy to use.
  - *Cons*: Relies heavily on reflection, hard to manage thread dispatching, deprecated/legacy APIs.
- *Alternative 2: Custom Executor-based Dispatcher*
  - *Why*: A lightweight custom registry that holds listeners by event class and runs them within a dedicated virtual thread pool (Java 21). This avoids blocking server game tick loops.
