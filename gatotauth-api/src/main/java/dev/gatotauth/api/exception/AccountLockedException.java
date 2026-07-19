package dev.gatotauth.api.exception;

/**
 * Thrown when an authenticated player's account is locked.
 * <p>
 * API Status: Stable
 *
 * @since 1.0.0
 */
public class AccountLockedException extends GatotAuthException {
    /**
     * Constructs an AccountLockedException with a detail message.
     *
     * @param message The detail message explaining why the account is locked.
     */
    public AccountLockedException(String message) {
        super(message);
    }
}
