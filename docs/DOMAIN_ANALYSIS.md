# GatotAuth Domain Analysis & Ubiquitous Language

This document establishes the business language (Ubiquitous Language) and domain models for GatotAuth, defining the core concepts, their lifecycles, constraints, and relationships.

---

## 1. Core Domain Concepts

### Player Profile
- **Definition**: The unique representation of a Minecraft network player (identifiable by Mojang/Bedrock UUID and Username).
- **Domain Role**: Aggregate Root / Entity.
- **Owner Module**: `gatotauth-api`.
- **Responsibility**: Tracks player identity, original login source (Premium Java, Offline, Bedrock via Floodgate), and coordinates account connections.
- **Lifecycle**: Created on first network connection. Remains active across server migrations. Destroyed only during data purging.
- **Relationships**: Owns 1 Account (optional), owns 0..* Sessions, associated with 1..* Connections.
- **Invariants**: 
  - UUID must be standard RFC 4122.
  - Username must be alphanumeric, 3-16 characters.
- **Future Extension**: Linking multiple alternative usernames or alternative UUID formats (e.g. custom offline namespace mappings).

### Account
- **Definition**: The authentication credential set and status registry associated with a Player Profile.
- **Domain Role**: Entity.
- **Owner Module**: `gatotauth-core`.
- **Responsibility**: Manages registration details, hashed credentials, MFA settings, and lock status.
- **Lifecycle**: 
  - `UNREGISTERED` -> (Registers) -> `REGISTERED` -> (Suspicious Activity) -> `LOCKED`.
- **Relationships**: Owned by 1 Player Profile.
- **Invariants**:
  - Cannot register an already registered Player Profile.
  - Locked account cannot generate new Sessions.
- **Future Extension**: Multi-credential support (e.g., secondary passwords, token-based verification).

### Session
- **Definition**: A cryptographically verified period of authorized network access.
- **Domain Role**: Aggregate Root.
- **Owner Module**: `gatotauth-api`.
- **Responsibility**: Authorizes in-game packets, tracks active verification duration, and governs login bypass logic.
- **Lifecycle**:
  - Created upon successful authentication. Expires after TTL duration. Revoked manually or by IP change.
- **Relationships**: Bound to 1 Player Profile. Bound to 1 Device/IP.
- **Invariants**:
  - Token must be cryptographically secure random value.
  - Expiry date must be in the future.
- **Future Extension**: Sliding sessions (renewing TTL on player action).

### Connection
- **Definition**: The network handshake channel representing a connecting client client.
- **Domain Role**: Entity.
- **Owner Module**: `gatotauth-protocol`.
- **Responsibility**: Holds socket state, tracks authentication progress (e.g., Handshake -> Status -> Login), and buffers incoming packets before login.
- **Lifecycle**: Created on socket connect, terminated on disconnect.
- **Relationships**: References 1 Player Profile, maps to 1 active Session (if authenticated).
- **Invariants**:
  - Must remain in a restricted state (canceling movement/chat) until verification resolves.
- **Future Extension**: Custom packet delays or anti-cheat integrations.

### Identity Types (Premium, Offline, Floodgate)
- **Definition**: The authentication origin of a player.
  - *Premium*: Validated via Mojang session servers.
  - *Offline*: Cracked client connection requiring password.
  - *Floodgate*: Bedrock player connecting via Geyser proxy, validated via Xbox Live.
- **Domain Role**: Value Object (Sealed class hierarchy).
- **Owner Module**: `gatotauth-api`.
- **Responsibility**: Directs which login pipeline route is taken.
- **Lifecycle**: Immutable. Assigned when connection is verified.
- **Relationships**: Property of Player Profile.
- **Invariants**:
  - A player cannot be Premium and Offline simultaneously on the same connection.
- **Future Extension**: Third-party federated login (OAuth2 / Discord ID).

### Device
- **Definition**: Client platform configuration footprint.
- **Domain Role**: Value Object.
- **Owner Module**: `gatotauth-common`.
- **Responsibility**: Tracks OS type, platform hardware identifiers, and connection properties (Bedrock vs Java console details).
- **Lifecycle**: Immutable. Set during login handshake.
- **Relationships**: Property of Connection.
- **Invariants**: None.
- **Future Extension**: Fingerprinting devices to automatically flag suspicious login changes (e.g., changing from Windows to Android).

### Authentication Method
- **Definition**: The mechanism used to verify identity (Password, TOTP/MFA, Passwordless).
- **Domain Role**: Value Object.
- **Owner Module**: `gatotauth-api`.
- **Responsibility**: Validates credentials using specific provider instances.
- **Lifecycle**: Immutable.
- **Relationships**: Property of Login Attempt.
- **Invariants**: None.
- **Future Extension**: WebAuthn/FIDO2 keys support.

### Security Policy
- **Definition**: Rules dictating password strength, maximum connections, lockout thresholds, and IP geofences.
- **Domain Role**: Value Object.
- **Owner Module**: `gatotauth-core`.
- **Responsibility**: Validates registration/login constraints.
- **Lifecycle**: Loaded from config; immutable at runtime.
- **Relationships**: Applied to Connection and Account.
- **Invariants**: Must have secure defaults (e.g., min 8 chars for passwords).
- **Future Extension**: Dynamic group-based policies (different rules for Staff vs Players).

### Login Attempt
- **Definition**: A recorded security transaction track logging a login event.
- **Domain Role**: Entity.
- **Owner Module**: `gatotauth-core`.
- **Responsibility**: Feeds the lockout detection cache, logging timing, IP, and success status.
- **Lifecycle**: Created on input, garbage collected after a sliding time-window.
- **Relationships**: Linked to Player Profile and IP.
- **Invariants**: None.
- **Future Extension**: Security log exporting to Elastic/Splunk.

### Trust Relationship
- **Definition**: An association of an IP and Player Profile that requires no re-verification.
- **Domain Role**: Value Object.
- **Owner Module**: `gatotauth-core`.
- **Responsibility**: Evaluates if the IP is trusted (e.g. has active session history).
- **Lifecycle**: Discarded on session termination or timeout.
- **Relationships**: Maps User UUID to trusted IP.
- **Invariants**: Must fail if IP does not match the trust record.
- **Future Extension**: Geo-location matching rather than raw IP matching.
