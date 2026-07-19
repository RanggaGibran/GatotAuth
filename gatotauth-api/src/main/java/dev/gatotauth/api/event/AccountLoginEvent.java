package dev.gatotauth.api.event;

import dev.gatotauth.api.domain.account.Account;
import dev.gatotauth.api.domain.session.Session;
import java.util.Objects;

/**
 * Dispatched when a player logs in successfully and yields a Session.
 * <p>
 * API Status: Stable
 *
 * @param account The authenticated Account.
 * @param session The active Session.
 * @since 1.0.0
 */
public record AccountLoginEvent(Account account, Session session) implements GatotAuthEvent {
    /**
     * Validates event parameters.
     */
    public AccountLoginEvent {
        Objects.requireNonNull(account, "account cannot be null");
        Objects.requireNonNull(session, "session cannot be null");
    }
}
