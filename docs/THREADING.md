# Threading Model & Async Boundaries

Minecraft server software runs on a single primary game loop thread (Tick Thread) while proxy servers run on event loops (Netty Event Loops). Blocking these threads causes lag or timeouts. GatotAuth guarantees a fully non-blocking execution model.

## 1. Threading Rules
- **NEVER** perform blocking I/O (SQL queries, Redis reads, web requests) on Netty event loops or Minecraft tick threads.
- **NEVER** run cryptographically expensive operations (Argon2/BCrypt hashing) on the primary server thread.

```
[Velocity / Paper Event] ──> Handled instantly
                               │ (returns immediately)
                               └──> CompletableFuture running on:
                                    ├── [Virtual Thread Pool] -> Hashing / DB
                                    └── [Netty Thread] -> packet write
```

## 2. Java 21 Virtual Threads (Project Loom)
GatotAuth utilizes a virtual thread executor for asynchronous execution, making asynchronous programming simple and fast without the thread context-switch penalty.

### Thread Pools:
- **`GatotAuthScheduler.virtual()`**: Virtual thread per-task executor for blocking I/O operations (SQL/Redis queries).
- **`GatotAuthScheduler.compute()`**: Fixed platform thread pool for CPU-bound computations (hashing, decryption).

## 3. Alternative Models & Trade-offs
- *Alternative 1: Thread-per-plugin (CachedThreadPool)*: Allocates standard OS threads dynamically.
- *Trade-off*: High context switching costs and potential memory exhaustion under login flood attacks. Virtual threads scale to millions of concurrent tasks with minimal memory overhead, perfectly shielding the server from thread leaks.
