package dev.gatotauth.core.account;

import dev.gatotauth.api.domain.account.Account;
import dev.gatotauth.api.domain.account.AccountId;
import dev.gatotauth.api.domain.account.OfflineIdentity;
import dev.gatotauth.api.domain.account.PasswordCredential;
import dev.gatotauth.api.domain.account.SecurityPolicy;
import dev.gatotauth.api.provider.AccountRepository;
import dev.gatotauth.api.service.AccountManager;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * DefaultAccountManager implements AccountManager using AccountRepository.
 */
public final class DefaultAccountManager implements AccountManager {
    private final AccountRepository accountRepository;

    /**
     * Instantiates DefaultAccountManager.
     */
    public DefaultAccountManager(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public CompletableFuture<Optional<Account>> findById(AccountId id) {
        return accountRepository.findById(id);
    }

    @Override
    public CompletableFuture<Optional<Account>> findByUsername(String username) {
        return accountRepository.findByIdentity("OfflineIdentity", username);
    }

    @Override
    public CompletableFuture<Boolean> registerAccount(AccountId id, String username, String plainTextPassword) {
        return findById(id).thenCompose(opt -> {
            if (opt.isPresent()) {
                return CompletableFuture.completedFuture(false);
            }
            Account account = new Account(
                    id,
                    List.of(new OfflineIdentity(id.value(), username)),
                    List.of(new PasswordCredential(plainTextPassword)),
                    List.of(),
                    List.of(),
                    new SecurityPolicy(8, 5, Duration.ofMinutes(15)),
                    Map.of(),
                    Instant.now(),
                    false
            );
            return accountRepository.save(account).thenApply(v -> true);
        });
    }

    @Override
    public CompletableFuture<Void> lockAccount(AccountId id, String reason) {
        return findById(id).thenCompose(opt -> {
            if (opt.isEmpty()) {
                return CompletableFuture.completedFuture(null);
            }
            Account existing = opt.get();
            Account locked = new Account(
                    existing.id(),
                    existing.identities(),
                    existing.credentials(),
                    existing.sessions(),
                    existing.devices(),
                    existing.securityPolicy(),
                    existing.metadata(),
                    existing.createdAt(),
                    true
            );
            return accountRepository.save(locked);
        });
    }

    @Override
    public CompletableFuture<Void> unlockAccount(AccountId id) {
        return findById(id).thenCompose(opt -> {
            if (opt.isEmpty()) {
                return CompletableFuture.completedFuture(null);
            }
            Account existing = opt.get();
            Account unlocked = new Account(
                    existing.id(),
                    existing.identities(),
                    existing.credentials(),
                    existing.sessions(),
                    existing.devices(),
                    existing.securityPolicy(),
                    existing.metadata(),
                    existing.createdAt(),
                    false
            );
            return accountRepository.save(unlocked);
        });
    }
}
