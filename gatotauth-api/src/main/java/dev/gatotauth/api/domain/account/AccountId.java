package dev.gatotauth.api.domain.account;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object record wrapping a unique identifier for an account.
 * <p>
 * API Status: Stable
 *
 * @param value Unique account UUID.
 * @since 1.0.0
 */
public record AccountId(UUID value) {
    /**
     * Validates AccountId parameter.
     */
    public AccountId {
        Objects.requireNonNull(value, "AccountId value cannot be null");
    }
}
