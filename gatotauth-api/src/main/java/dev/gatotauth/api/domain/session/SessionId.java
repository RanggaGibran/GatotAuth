package dev.gatotauth.api.domain.session;

import java.util.Objects;

/**
 * Value object representing a secure unique session token.
 * <p>
 * API Status: Stable
 *
 * @param token The cryptographically random token string.
 * @since 1.0.0
 */
public record SessionId(String token) {
    /**
     * Validates session token integrity.
     */
    public SessionId {
        Objects.requireNonNull(token, "Session token cannot be null");
        if (token.isBlank()) {
            throw new IllegalArgumentException("Session token cannot be blank");
        }
    }
}
