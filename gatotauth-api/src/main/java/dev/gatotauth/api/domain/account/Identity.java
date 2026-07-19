package dev.gatotauth.api.domain.account;

/**
 * Sealed interface representing a verified identity within the Minecraft/External ecosystem.
 * <p>
 * API Status: Stable
 *
 * @since 1.0.0
 */
public sealed interface Identity permits MojangIdentity, OfflineIdentity, FloodgateIdentity {
    /**
     * Gets the unique string representation of the identity identifier.
     *
     * @return String identity value.
     */
    String identifier();
}
