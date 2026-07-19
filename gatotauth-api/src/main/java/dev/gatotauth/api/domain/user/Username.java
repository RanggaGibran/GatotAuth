package dev.gatotauth.api.domain.user;

import java.util.Objects;

/**
 * Represents a validated Minecraft username.
 * <p>
 * API Status: Stable
 *
 * @param value The raw username string.
 * @since 1.0.0
 */
public record Username(String value) {
    /**
     * Constructs and validates a Username instance.
     *
     * @param value The raw username value.
     * @throws IllegalArgumentException if validation checks fail.
     */
    public Username {
        Objects.requireNonNull(value, "Username value cannot be null");
        if (value.length() < 3 || value.length() > 16) {
            throw new IllegalArgumentException("Username length must be between 3 and 16 characters");
        }
        if (!value.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Username contains invalid characters");
        }
    }
}
