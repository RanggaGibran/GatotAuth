package dev.gatotauth.api.exception;

/**
 * Base exception for all GatotAuth runtime errors.
 * <p>
 * API Status: Stable
 *
 * @since 1.0.0
 */
public class GatotAuthException extends RuntimeException {
    /**
     * Constructs a GatotAuthException with a detail message.
     *
     * @param message The detail message.
     */
    public GatotAuthException(String message) {
        super(message);
    }

    /**
     * Constructs a GatotAuthException with a detail message and cause.
     *
     * @param message The detail message.
     * @param cause The underlying cause.
     */
    public GatotAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
