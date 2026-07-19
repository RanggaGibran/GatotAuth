package dev.gatotauth.core.infra.storage.sql;

import static org.assertj.core.api.Assertions.assertThat;

import dev.gatotauth.api.domain.account.Account;
import dev.gatotauth.api.domain.account.AccountId;
import dev.gatotauth.api.domain.account.OfflineIdentity;
import dev.gatotauth.api.domain.account.PasswordCredential;
import dev.gatotauth.api.domain.account.SecurityPolicy;
import dev.gatotauth.api.domain.session.Session;
import dev.gatotauth.api.domain.session.SessionId;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatabaseConnectorTest {
    @TempDir
    File tempDir;

    private DatabaseConnector connector;
    private SqlAccountRepository accountRepo;
    private SqlSessionRepository sessionRepo;
    private ExecutorService ioExecutor;

    @BeforeEach
    void setUp() {
        File dbFile = new File(tempDir, "test.db");
        connector = new DatabaseConnector(dbFile.getAbsolutePath());
        ioExecutor = Executors.newSingleThreadExecutor();
        accountRepo = new SqlAccountRepository(connector, ioExecutor);
        sessionRepo = new SqlSessionRepository(connector, ioExecutor);
    }

    @AfterEach
    void tearDown() {
        if (connector != null) {
            connector.close();
        }
        if (ioExecutor != null) {
            ioExecutor.shutdownNow();
        }
    }

    @Test
    void shouldPersistAndRetrieveAccountsAndSessions() throws ExecutionException, InterruptedException {
        UUID uuid = UUID.randomUUID();
        AccountId accountId = new AccountId(uuid);
        Account account = new Account(
                accountId,
                List.of(new OfflineIdentity(uuid, "Player")),
                List.of(new PasswordCredential("hash")),
                List.of(),
                List.of(),
                new SecurityPolicy(8, 5, Duration.ofMinutes(15)),
                Map.of(),
                Instant.now(),
                false
        );

        // 1. Save and fetch account
        accountRepo.save(account).get();
        Optional<Account> fetchedOpt = accountRepo.findById(accountId).get();
        assertThat(fetchedOpt).isPresent();
        Account fetched = fetchedOpt.get();
        assertThat(fetched.id()).isEqualTo(accountId);
        assertThat(fetched.isLocked()).isFalse();

        // 2. Fetch by identity
        Optional<Account> identityFetched = accountRepo.findByIdentity("OfflineIdentity", "player").get();
        assertThat(identityFetched).isPresent();
        assertThat(identityFetched.get().id()).isEqualTo(accountId);

        // 3. Save and fetch session
        SessionId sessionId = new SessionId("session-token");
        Session session = new Session(
                sessionId, uuid, "127.0.0.1", Instant.now(), Instant.now().plus(Duration.ofMinutes(30))
        );
        sessionRepo.save(session).get();

        Optional<Session> fetchedSessionOpt = sessionRepo.findById(sessionId).get();
        assertThat(fetchedSessionOpt).isPresent();
        assertThat(fetchedSessionOpt.get().sessionId()).isEqualTo(sessionId);

        // 4. Fetch session by AccountId
        List<Session> sessions = sessionRepo.findByAccountId(accountId).get();
        assertThat(sessions).hasSize(1);
        assertThat(sessions.get(0).sessionId()).isEqualTo(sessionId);

        // 5. Delete session
        sessionRepo.delete(sessionId).get();
        assertThat(sessionRepo.findById(sessionId).get()).isEmpty();

        // 6. Delete account
        accountRepo.delete(accountId).get();
        assertThat(accountRepo.findById(accountId).get()).isEmpty();
    }
}
