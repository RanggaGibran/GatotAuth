package dev.gatotauth.core.infra.storage;

import dev.gatotauth.api.provider.AccountRepository;
import dev.gatotauth.api.provider.SessionRepository;
import dev.gatotauth.core.infra.lifecycle.Lifecycle;
import java.util.Objects;
import java.util.Optional;

/**
 * StorageService manages the active database repositories and lifecycle.
 */
public final class StorageService implements Lifecycle {
    private AccountRepository accountRepository;
    private SessionRepository sessionRepository;
    private boolean running = false;

    /**
     * Registers the active storage repository implementations.
     *
     * @param accountRepository Active AccountRepository instance.
     * @param sessionRepository Active SessionRepository instance.
     */
    public void registerRepositories(AccountRepository accountRepository, SessionRepository sessionRepository) {
        this.accountRepository = Objects.requireNonNull(accountRepository, "accountRepository cannot be null");
        this.sessionRepository = Objects.requireNonNull(sessionRepository, "sessionRepository cannot be null");
    }

    /**
     * Gets the active Account repository.
     *
     * @return Optional containing the active repository.
     */
    public Optional<AccountRepository> getAccountRepository() {
        return Optional.ofNullable(accountRepository);
    }

    /**
     * Gets the active Session repository.
     *
     * @return Optional containing the active repository.
     */
    public Optional<SessionRepository> getSessionRepository() {
        return Optional.ofNullable(sessionRepository);
    }

    @Override
    public void start() {
        if (accountRepository == null || sessionRepository == null) {
            throw new IllegalStateException("Repositories must be registered before starting StorageService");
        }
        running = true;
    }

    @Override
    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }
}
