package dev.gatotauth.core.infra.storage;

import dev.gatotauth.api.domain.account.AccountId;
import dev.gatotauth.api.domain.session.Session;
import dev.gatotauth.api.domain.session.SessionId;
import dev.gatotauth.api.provider.SessionRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * InMemorySessionRepository implements SessionRepository using concurrent in-memory maps.
 */
public final class InMemorySessionRepository implements SessionRepository {
    private final Map<SessionId, Session> sessions = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Optional<Session>> findById(SessionId id) {
        return CompletableFuture.completedFuture(Optional.ofNullable(sessions.get(id)));
    }

    @Override
    public CompletableFuture<Void> save(Session session) {
        sessions.put(session.sessionId(), session);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> delete(SessionId id) {
        sessions.remove(id);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<List<Session>> findByAccountId(AccountId accountId) {
        return CompletableFuture.completedFuture(
                sessions.values().stream()
                        .filter(s -> s.userUuid().equals(accountId.value()))
                        .collect(Collectors.toList())
        );
    }
}
