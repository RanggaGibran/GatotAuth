package dev.gatotauth.api.domain.account;

import java.util.Objects;

/**
 * Represents a standard password hash credential.
 * <p>
 * API Status: Stable
 *
 * @param hashedPassword Encoded password hash.
 * @since 1.0.0
 */
public record PasswordCredential(String hashedPassword) implements Credential {
    /**
     * Validates PasswordCredential parameters.
     */
    public PasswordCredential {
        Objects.requireNonNull(hashedPassword, "hashedPassword cannot be null");
    }
}
