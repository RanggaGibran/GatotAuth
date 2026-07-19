package dev.gatotauth.core.auth;

import dev.gatotauth.api.domain.session.Session;
import java.util.Objects;

/**
 * Sealed interface representing the outcome of authentication.
 */
public sealed interface AuthenticationResult permits
        AuthenticationResult.Success, AuthenticationResult.Failure {

    /**
     * Represents a successful authentication result.
     *
     * @param session The active Session created.
     */
    record Success(Session session) implements AuthenticationResult {
        public Success {
            Objects.requireNonNull(session, "Session cannot be null");
        }
    }

    /**
     * Represents a failed authentication result.
     *
     * @param reason Description explaining why verification failed.
     */
    record Failure(String reason) implements AuthenticationResult {
        public Failure {
            Objects.requireNonNull(reason, "Reason cannot be null");
        }
    }
}
