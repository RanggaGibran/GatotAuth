package dev.gatotauth.api.domain.account;

import java.util.Objects;

/**
 * Represents WebAuthn credential context.
 * <p>
 * API Status: Stable
 *
 * @param credentialId Unique FIDO2 credential identifier.
 * @param publicKey Encoded public key.
 * @since 1.0.0
 */
public record WebAuthnCredential(String credentialId, String publicKey) implements Credential {
    /**
     * Validates WebAuthnCredential parameters.
     */
    public WebAuthnCredential {
        Objects.requireNonNull(credentialId, "credentialId cannot be null");
        Objects.requireNonNull(publicKey, "publicKey cannot be null");
    }
}
