package dev.gatotauth.core.auth;

import dev.gatotauth.api.domain.account.Credential;
import dev.gatotauth.api.domain.account.Device;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable context containing connection and authentication input payloads.
 */
public record AuthenticationContext(
        UUID userUuid,
        String username,
        Credential credential,
        String ipAddress,
        Device device
) {
    /**
     * Validates AuthenticationContext parameters.
     */
    public AuthenticationContext {
        Objects.requireNonNull(userUuid, "userUuid cannot be null");
        Objects.requireNonNull(username, "username cannot be null");
        Objects.requireNonNull(credential, "credential cannot be null");
        Objects.requireNonNull(ipAddress, "ipAddress cannot be null");
        Objects.requireNonNull(device, "device cannot be null");
    }
}
