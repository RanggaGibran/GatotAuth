package dev.gatotauth.core.auth;

import java.util.Objects;
import java.util.Optional;

/**
 * Tracks the state of the active authentication flow run.
 */
public final class AuthenticationDecision {
    private String failureReason;
    private boolean completed = false;

    /**
     * Vetoes the authentication pipeline with a failure reason.
     *
     * @param reason The rejection reason.
     */
    public void reject(String reason) {
        this.failureReason = Objects.requireNonNull(reason, "Rejection reason cannot be null");
        this.completed = true;
    }

    /**
     * Approves and completes the authentication pipeline run.
     */
    public void allow() {
        this.completed = true;
    }

    /**
     * Checks if the decision has been finalized.
     *
     * @return true if finalized.
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Gets the failure reason if authentication was rejected.
     *
     * @return Optional containing the reason.
     */
    public Optional<String> getFailureReason() {
        return Optional.ofNullable(failureReason);
    }
}
