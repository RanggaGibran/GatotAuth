package dev.gatotauth.api.domain.account;

/**
 * Sealed interface representing credentials used to verify identities.
 * <p>
 * API Status: Stable
 *
 * @since 1.0.0
 */
public sealed interface Credential permits
        PasswordCredential, TotpCredential, WebAuthnCredential, ApiTokenCredential {
}
