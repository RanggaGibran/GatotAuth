package dev.gatotauth.api.event;

import dev.gatotauth.api.domain.session.Session;
import java.util.Objects;

/**
 * Dispatched when an active session reaches its TTL or gets explicitly invalidated.
 * <p>
 * API Status: Stable
 *
 * @param session The invalidated Session context.
 * @since 1.0.0
 */
public record SessionExpiredEvent(Session session) implements GatotAuthEvent {
    /**
     * Validates event parameters.
     */
    public SessionExpiredEvent {
        Objects.requireNonNull(session, "Session cannot be null");
    }
}
