# Session Management System

GatotAuth manages user sessions to provide seamless auto-logins across multi-proxy environments (BungeeCord/Velocity clusters).

## 1. Session Lifecycle

```
[Connect] -> Check Active Session
               ├── Valid Session found? -> Bypasses login (Update LastSeen)
               └── No Session -> Login Required -> Validated -> Create Session (Persisted)
```

## 2. Session Properties & Replication
A `Session` requires:
- Cryptographically secure random ID (Token).
- Client IP Address (for IP-bind checking).
- Creation Time & Expiry Time (TTL).

### Replication Alternatives:
#### Alternative 1: SQL Database polling
- *Pros*: Simple, no extra software needed.
- *Cons*: Slow, creates heavy database IO when players switch servers.

#### Alternative 2: Redis Session Cache
- *Pros*: Extremely fast sub-millisecond lookups, built-in key expiry (TTL).
- *Cons*: Requires setting up a Redis server.

#### Alternative 3: Distributed In-Memory (Hazelcast)
- *Pros*: Fully Java-native, embeds directly in proxy processes.
- *Cons*: Large memory foot-print, complicates proxy cluster setups.

### Recommended Design: Unified Cache Port
GatotAuth defines `SessionStorage` interface (Port).
We implement two adapters:
1. **LocalCacheSessionStorage** (using Caffeine for single-server setups).
2. **RedisSessionStorage** (using Redisson/Jedis for multi-proxy setups, publishing pub/sub events on session invalidation).
This allows networks to scale seamlessly from 1 server to 100+ proxy servers.
