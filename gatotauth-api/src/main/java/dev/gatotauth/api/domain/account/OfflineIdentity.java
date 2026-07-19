package dev.gatotauth.api.domain.account;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a local cracked offline player identity.
 * <p>
 * API Status: Stable
 *
 * @param offlineUuid The player's offline UUID.
 * @param username The player's username.
 * @since 1.0.0
 */
public record OfflineIdentity(UUID offlineUuid, String username) implements Identity {
    /**
     * Validates OfflineIdentity parameters.
     */
    public OfflineIdentity {
        Objects.requireNonNull(offlineUuid, "offlineUuid cannot be null");
        Objects.requireNonNull(username, "username cannot be null");
    }

    @Override
    public String identifier() {
        return username.toLowerCase();
    }
}
