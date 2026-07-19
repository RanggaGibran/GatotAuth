package dev.gatotauth.api.exception;

/**
 * Thrown when connections or requests exceed defined rate limits.
 * <p>
 * API Status: Stable
 *
 * @since 1.0.0
 */
public class RateLimitExceededException extends GatotAuthException {
    /**
     * Constructs a RateLimitExceededException with a detail message.
     *
     * @param message The detail message explaining the rate limit breach.
     */
    public RateLimitExceededException(String message) {
        super(message);
    }
}
