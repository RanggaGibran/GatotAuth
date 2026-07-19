package dev.gatotauth.api.exception;

/**
 * Thrown when looking up a user that does not exist.
 * <p>
 * API Status: Stable
 *
 * @since 1.0.0
 */
public class UserNotFoundException extends GatotAuthException {
    /**
     * Constructs a UserNotFoundException with a detail message.
     *
     * @param message The detail message explaining the user lookup error.
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}
