package dev.gatotauth.core.infra.storage;

import dev.gatotauth.api.domain.account.Account;
import dev.gatotauth.api.domain.account.AccountId;
import dev.gatotauth.api.provider.AccountRepository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * InMemoryAccountRepository implements AccountRepository using concurrent in-memory maps.
 */
public final class InMemoryAccountRepository implements AccountRepository {
    private final Map<AccountId, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Optional<Account>> findById(AccountId id) {
        return CompletableFuture.completedFuture(Optional.ofNullable(accounts.get(id)));
    }

    @Override
    public CompletableFuture<Optional<Account>> findByIdentity(String providerType, String identifier) {
        return CompletableFuture.completedFuture(
                accounts.values().stream()
                        .filter(a -> a.identities().stream()
                                .anyMatch(i -> i.identifier().equals(identifier)))
                        .findFirst()
        );
    }

    @Override
    public CompletableFuture<Void> save(Account account) {
        accounts.put(account.id(), account);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> delete(AccountId id) {
        accounts.remove(id);
        return CompletableFuture.completedFuture(null);
    }
}
