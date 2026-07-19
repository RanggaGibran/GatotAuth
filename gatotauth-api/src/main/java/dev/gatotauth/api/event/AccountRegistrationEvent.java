package dev.gatotauth.api.event;

import dev.gatotauth.api.domain.account.Account;
import java.util.Objects;

/**
 * Dispatched when a player completes registration.
 * <p>
 * API Status: Stable
 *
 * @param account The registered Account.
 * @since 1.0.0
 */
public record AccountRegistrationEvent(Account account) implements GatotAuthEvent {
    /**
     * Validates event parameters.
     */
    public AccountRegistrationEvent {
        Objects.requireNonNull(account, "account cannot be null");
    }
}
