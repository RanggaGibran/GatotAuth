package dev.gatotauth.api.domain.account;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a Bedrock player identity connected via Floodgate verification.
 * <p>
 * API Status: Stable
 *
 * @param bedrockUuid The player's Bedrock UUID.
 * @param username The player's username.
 * @since 1.0.0
 */
public record FloodgateIdentity(UUID bedrockUuid, String username) implements Identity {
    /**
     * Validates FloodgateIdentity parameters.
     */
    public FloodgateIdentity {
        Objects.requireNonNull(bedrockUuid, "bedrockUuid cannot be null");
        Objects.requireNonNull(username, "username cannot be null");
    }

    @Override
    public String identifier() {
        return bedrockUuid.toString();
    }
}
