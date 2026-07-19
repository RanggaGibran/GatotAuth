package dev.gatotauth.api.domain.session;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an active authenticated player session.
 * <p>
 * API Status: Stable
 *
 * @param sessionId The unique session token identifier.
 * @param userUuid The player UUID bound to this session.
 * @param ipAddress The IP address the session was generated under.
 * @param createdAt The creation timestamp.
 * @param expiresAt The expiry timestamp.
 * @since 1.0.0
 */
public record Session(
        SessionId sessionId,
        UUID userUuid,
        String ipAddress,
        Instant createdAt,
        Instant expiresAt
) {
    /**
     * Validates session creation parameters and time constraints.
     */
    public Session {
        Objects.requireNonNull(sessionId, "Session ID cannot be null");
        Objects.requireNonNull(userUuid, "User UUID cannot be null");
        Objects.requireNonNull(ipAddress, "IP address cannot be null");
        Objects.requireNonNull(createdAt, "Created timestamp cannot be null");
        Objects.requireNonNull(expiresAt, "Expires timestamp cannot be null");
        if (expiresAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("Session expiry cannot be before creation time");
        }
    }

    /**
     * Checks if the session has expired relative to the provided timestamp.
     *
     * @param now The current timestamp to check against.
     * @return true if the session is expired.
     */
    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }
}
