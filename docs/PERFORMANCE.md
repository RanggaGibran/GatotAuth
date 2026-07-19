# Performance Tuning & Scalability

GatotAuth is engineered to support thousands of concurrent players and handle heavy login load spikes (e.g., lobby restarts or network reboots) with minimal overhead.

## 1. Core Performance Vectors

### Connection Pools & I/O
- Database pooling via **HikariCP** is configured with optimized timeouts (`connectionTimeout = 5000ms`, `maximumPoolSize = 10` per proxy).
- Queries are tuned with indexed columns on `uuid` and `username`.

### Cache Strategies
- Caffeine is used for memory caching. All user settings are cached with a short TTL (e.g., 5 minutes) to avoid repeated database hits during connection events.
- Active session validations are completed entirely in memory (via local cache or Redis cache), removing standard database load entirely.

### CPU Efficiency
- Password hashing (Argon2id) runs out-of-band in `GatotAuthScheduler.compute()`. We cap concurrency to the number of physical cores minus one to ensure game ticks are unaffected.

## 2. Packet Performance
- Instead of using heavy platform metadata layers, we intercept packet streams using **PacketEvents**. This allows us to cancel client packets (movement, chat, blocks) at the protocol layer before they are processed by the game engine, saving CPU cycles.

## 3. Alternative Approaches & Trade-offs
- *Alternative: Platform API events (e.g. PlayerMoveEvent)*
  - *Cons*: Triggered hundreds of times per tick per player. Canceling movement at the Bukkit level wastes enormous CPU resources. Intercepting protocol packets via PacketEvents filters movement messages immediately at the network thread level.
