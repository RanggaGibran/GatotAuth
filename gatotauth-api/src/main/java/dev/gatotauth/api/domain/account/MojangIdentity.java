package dev.gatotauth.api.domain.account;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents an official Mojang online player identity.
 * <p>
 * API Status: Stable
 *
 * @param mojangUuid The player's unique Mojang UUID.
 * @param username The player's username.
 * @since 1.0.0
 */
public record MojangIdentity(UUID mojangUuid, String username) implements Identity {
    /**
     * Validates MojangIdentity parameters.
     */
    public MojangIdentity {
        Objects.requireNonNull(mojangUuid, "mojangUuid cannot be null");
        Objects.requireNonNull(username, "username cannot be null");
    }

    @Override
    public String identifier() {
        return mojangUuid.toString();
    }
}
