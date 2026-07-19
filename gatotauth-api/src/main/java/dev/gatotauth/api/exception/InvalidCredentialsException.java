package dev.gatotauth.api.exception;

/**
 * Thrown when credential verification checks fail.
 * <p>
 * API Status: Stable
 *
 * @since 1.0.0
 */
public class InvalidCredentialsException extends GatotAuthException {
    /**
     * Constructs an InvalidCredentialsException with a detail message.
     *
     * @param message The detail message explaining the credential failure.
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
