package dev.gatotauth.core.session;

import dev.gatotauth.api.domain.account.AccountId;
import dev.gatotauth.api.domain.session.Session;
import dev.gatotauth.api.domain.session.SessionId;
import dev.gatotauth.api.event.SessionExpiredEvent;
import dev.gatotauth.api.provider.SessionRepository;
import dev.gatotauth.api.service.SessionManager;
import dev.gatotauth.core.infra.event.EventBus;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * DefaultSessionManager implements SessionManager interface using the SessionRepository SPI.
 */
public final class DefaultSessionManager implements SessionManager {
    private final SessionRepository sessionRepository;
    private final EventBus eventBus;
    private final Duration sessionDuration;

    /**
     * Instantiates DefaultSessionManager.
     */
    public DefaultSessionManager(SessionRepository sessionRepository, EventBus eventBus, Duration sessionDuration) {
        this.sessionRepository = Objects.requireNonNull(sessionRepository, "sessionRepository cannot be null");
        this.eventBus = Objects.requireNonNull(eventBus, "eventBus cannot be null");
        this.sessionDuration = Objects.requireNonNull(sessionDuration, "sessionDuration cannot be null");
    }

    @Override
    public CompletableFuture<Optional<Session>> findActiveSession(UUID userUuid) {
        return sessionRepository.findByAccountId(new AccountId(userUuid))
                .thenApply(sessions -> sessions.stream()
                        .filter(session -> !session.isExpired(Instant.now()))
                        .findFirst());
    }

    @Override
    public CompletableFuture<Boolean> hasActiveSession(UUID userUuid, String ipAddress) {
        return findActiveSession(userUuid)
                .thenCompose(optSession -> {
                    if (optSession.isEmpty()) {
                        return CompletableFuture.completedFuture(false);
                    }
                    Session session = optSession.get();
                    if (!session.ipAddress().equals(ipAddress)) {
                        return CompletableFuture.completedFuture(false);
                    }
                    // Renew session expiration window
                    Instant newExpiry = Instant.now().plus(sessionDuration);
                    Session renewed = new Session(
                            session.sessionId(),
                            session.userUuid(),
                            session.ipAddress(),
                            session.createdAt(),
                            newExpiry
                    );
                    return sessionRepository.save(renewed)
                            .thenApply(v -> true);
                });
    }

    @Override
    public CompletableFuture<Session> createSession(UUID userUuid, String ipAddress) {
        return invalidateSession(userUuid)
                .thenCompose(v -> {
                    SessionId sessionId = new SessionId(UUID.randomUUID().toString());
                    Instant now = Instant.now();
                    Instant expiry = now.plus(sessionDuration);
                    Session session = new Session(sessionId, userUuid, ipAddress, now, expiry);
                    return sessionRepository.save(session)
                            .thenApply(v2 -> session);
                });
    }

    @Override
    public CompletableFuture<Void> invalidateSession(UUID userUuid) {
        return sessionRepository.findByAccountId(new AccountId(userUuid))
                .thenCompose(sessions -> {
                    CompletableFuture<Void> allDeletes = CompletableFuture.completedFuture(null);
                    for (Session session : sessions) {
                        allDeletes = allDeletes.thenCompose(v -> sessionRepository.delete(session.sessionId())
                                .thenRun(() -> eventBus.publish(new SessionExpiredEvent(session))));
                    }
                    return allDeletes;
                });
    }
}
