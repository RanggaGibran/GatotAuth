package dev.gatotauth.api.service;

import dev.gatotauth.api.domain.session.Session;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface governing active player sessions.
 * <p>
 * API Status: Stable
 *
 * @since 1.0.0
 */
public interface SessionManager {
    /**
     * Finds the active session of a player.
     *
     * @param userUuid User UUID.
     * @return Future containing the optional active Session.
     */
    CompletableFuture<Optional<Session>> findActiveSession(UUID userUuid);

    /**
     * Checks if a player has an active session from a specific IP address.
     *
     * @param userUuid User UUID.
     * @param ipAddress Client IP Address.
     * @return Future resolving to true if session is valid.
     */
    CompletableFuture<Boolean> hasActiveSession(UUID userUuid, String ipAddress);

    /**
     * Creates a new active session for a player.
     *
     * @param userUuid User UUID.
     * @param ipAddress Client IP Address.
     * @return Future containing the newly created Session instance.
     */
    CompletableFuture<Session> createSession(UUID userUuid, String ipAddress);

    /**
     * Invalidates an active session, logging the player out.
     *
     * @param userUuid User UUID.
     * @return Future resolving when session is revoked.
     */
    CompletableFuture<Void> invalidateSession(UUID userUuid);
}
