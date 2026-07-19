# Security Architecture

Authentication plugins are a primary target for exploit vectors. GatotAuth is designed with defense-in-depth security principles.

## 1. Core Security Features

### Password Hashing Standards
- **Argon2id** is the default hashing algorithm (parameters: memory = 65536 KB, iterations = 3, parallelism = 4).
- Automatic migration path: If a user logs in using a legacy algorithm (e.g., MD5, SHA-256, BCrypt), GatotAuth automatically re-hashes their password using Argon2id during verification.

### Brute Force & Rate Limiting
- **IP connection limits**: Automatically limits connections per IP in pre-login to mitigate DDoS and connection floods.
- **Login attempts rate limit**: Limits login commands to 3 per minute.
- **Account Lockout**: After 5 failed password attempts, the UUID is locked for 15 minutes. Fails are tracked in memory via sliding-window cache.

### Session Hijack & Replay Prevention
- **IP-bound sessions**: Sessions are bound to a specific IP address by default. If the player IP changes, the session is invalidated immediately, and authentication is requested.
- **Session Token Rotation**: Each session token is cryptographically random (secure random bytes encoded in Base64).

## 2. Alternatives & Trade-offs
- *Alternative: BCrypt Hashing*
  - *Pros*: Built-in Java library support via Spring Security or lightweight wrappers, low CPU resource requirement.
  - *Cons*: Vulnerable to GPU-based crack farms. Argon2id is memory-hard, making GPU cracking orders of magnitude more expensive.
