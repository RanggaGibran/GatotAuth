package dev.gatotauth.core.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.gatotauth.api.domain.account.AccountId;
import dev.gatotauth.api.domain.session.Session;
import dev.gatotauth.api.domain.session.SessionId;
import dev.gatotauth.api.event.SessionExpiredEvent;
import dev.gatotauth.api.provider.SessionRepository;
import dev.gatotauth.core.infra.event.EventBus;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultSessionManagerTest {
    private SessionRepository mockRepo;
    private EventBus realEventBus;
    private DefaultSessionManager sessionManager;
    private UUID userUuid;
    private Session activeSession;

    @BeforeEach
    void setUp() {
        mockRepo = mock(SessionRepository.class);
        realEventBus = new EventBus(Runnable::run);
        sessionManager = new DefaultSessionManager(mockRepo, realEventBus, Duration.ofMinutes(30));

        userUuid = UUID.randomUUID();
        activeSession = new Session(
                new SessionId("test-session"),
                userUuid,
                "127.0.0.1",
                Instant.now(),
                Instant.now().plus(Duration.ofMinutes(30))
        );
    }

    @Test
    void shouldFindActiveSession() throws ExecutionException, InterruptedException {
        when(mockRepo.findByAccountId(new AccountId(userUuid)))
                .thenReturn(CompletableFuture.completedFuture(List.of(activeSession)));

        Optional<Session> sessionOpt = sessionManager.findActiveSession(userUuid).get();

        assertThat(sessionOpt).contains(activeSession);
    }

    @Test
    void shouldNotFindExpiredSession() throws ExecutionException, InterruptedException {
        Session expired = new Session(
                new SessionId("expired"),
                userUuid,
                "127.0.0.1",
                Instant.now().minus(Duration.ofMinutes(40)),
                Instant.now().minus(Duration.ofMinutes(10))
        );

        when(mockRepo.findByAccountId(new AccountId(userUuid)))
                .thenReturn(CompletableFuture.completedFuture(List.of(expired)));

        Optional<Session> sessionOpt = sessionManager.findActiveSession(userUuid).get();

        assertThat(sessionOpt).isEmpty();
    }

    @Test
    void shouldRenewSessionOnSuccessfulActiveCheck() throws ExecutionException, InterruptedException {
        when(mockRepo.findByAccountId(new AccountId(userUuid)))
                .thenReturn(CompletableFuture.completedFuture(List.of(activeSession)));
        when(mockRepo.save(any(Session.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        boolean hasActive = sessionManager.hasActiveSession(userUuid, "127.0.0.1").get();

        assertThat(hasActive).isTrue();
        verify(mockRepo).save(any(Session.class));
    }

    @Test
    void shouldInvalidateSessionsAndPublishEvents() throws ExecutionException, InterruptedException {
        List<Session> sessions = new ArrayList<>();
        sessions.add(activeSession);

        when(mockRepo.findByAccountId(new AccountId(userUuid)))
                .thenReturn(CompletableFuture.completedFuture(sessions));
        when(mockRepo.delete(activeSession.sessionId()))
                .thenReturn(CompletableFuture.completedFuture(null));

        List<SessionExpiredEvent> receivedEvents = new ArrayList<>();
        realEventBus.subscribe(SessionExpiredEvent.class, receivedEvents::add);

        sessionManager.invalidateSession(userUuid).get();

        verify(mockRepo).delete(activeSession.sessionId());
        assertThat(receivedEvents).hasSize(1);
        assertThat(receivedEvents.get(0).session()).isEqualTo(activeSession);
    }
}
