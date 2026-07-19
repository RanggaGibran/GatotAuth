package dev.gatotauth.api.provider;

import dev.gatotauth.api.domain.account.AccountId;
import dev.gatotauth.api.domain.session.Session;
import dev.gatotauth.api.domain.session.SessionId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous repository provider contract to persist and fetch Sessions.
 * <p>
 * API Status: Stable
 *
 * @since 1.0.0
 */
public interface SessionRepository {
    /**
     * Finds a Session by ID.
     *
     * @param id SessionId instance.
     * @return Future containing the optional Session.
     */
    CompletableFuture<Optional<Session>> findById(SessionId id);

    /**
     * Saves a Session.
     *
     * @param session The Session object.
     * @return Future indicating completion of the save transaction.
     */
    CompletableFuture<Void> save(Session session);

    /**
     * Deletes a Session.
     *
     * @param id SessionId to purge.
     * @return Future indicating completion of the delete transaction.
     */
    CompletableFuture<Void> delete(SessionId id);

    /**
     * Finds active Sessions associated with a specific AccountId.
     *
     * @param accountId Target account.
     * @return Future containing the list of sessions.
     */
    CompletableFuture<List<Session>> findByAccountId(AccountId accountId);
}
