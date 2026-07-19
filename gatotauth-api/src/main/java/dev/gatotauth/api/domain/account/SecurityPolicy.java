package dev.gatotauth.api.domain.account;

import java.time.Duration;
import java.util.Objects;

/**
 * Record representing security policies applied to accounts.
 * <p>
 * API Status: Stable
 *
 * @param minPasswordLength Minimum password complexity threshold.
 * @param lockoutThreshold Maximum failure attempts before lockout triggers.
 * @param lockoutDuration Lockout lock release time duration.
 * @since 1.0.0
 */
public record SecurityPolicy(
        int minPasswordLength,
        int lockoutThreshold,
        Duration lockoutDuration
) {
    /**
     * Validates SecurityPolicy constraints.
     */
    public SecurityPolicy {
        if (minPasswordLength < 0) {
            throw new IllegalArgumentException("minPasswordLength cannot be negative");
        }
        if (lockoutThreshold < 1) {
            throw new IllegalArgumentException("lockoutThreshold must be at least 1");
        }
        Objects.requireNonNull(lockoutDuration, "lockoutDuration cannot be null");
    }
}
