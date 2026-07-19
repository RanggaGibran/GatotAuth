package dev.gatotauth.api.domain.account;

import java.util.Objects;

/**
 * Represents a Time-based One-Time Password (TOTP) MFA secret key.
 * <p>
 * API Status: Stable
 *
 * @param secretKey Base32 encoded secret key.
 * @since 1.0.0
 */
public record TotpCredential(String secretKey) implements Credential {
    /**
     * Validates TotpCredential parameters.
     */
    public TotpCredential {
        Objects.requireNonNull(secretKey, "secretKey cannot be null");
    }
}
