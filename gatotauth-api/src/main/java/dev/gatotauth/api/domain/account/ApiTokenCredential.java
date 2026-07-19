package dev.gatotauth.api.domain.account;

import java.util.Objects;

/**
 * Represents API token credentials.
 * <p>
 * API Status: Stable
 *
 * @param tokenHash Hashed representation of the API token.
 * @since 1.0.0
 */
public record ApiTokenCredential(String tokenHash) implements Credential {
    /**
     * Validates ApiTokenCredential parameters.
     */
    public ApiTokenCredential {
        Objects.requireNonNull(tokenHash, "tokenHash cannot be null");
    }
}
