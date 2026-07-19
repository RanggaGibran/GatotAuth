package dev.gatotauth.api.provider;

import dev.gatotauth.api.domain.account.Account;
import dev.gatotauth.api.domain.account.AccountId;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous repository provider contract to persist and fetch Accounts.
 * <p>
 * API Status: Stable
 *
 * @since 1.0.0
 */
public interface AccountRepository {
    /**
     * Finds an Account by its unique ID.
     *
     * @param id AccountId instance.
     * @return Future containing the optional Account.
     */
    CompletableFuture<Optional<Account>> findById(AccountId id);

    /**
     * Finds an Account by a registered identity provider and its identifier.
     *
     * @param providerType The class name or identifier of the provider.
     * @param identifier Specific identity login username or uuid.
     * @return Future containing the optional Account match.
     */
    CompletableFuture<Optional<Account>> findByIdentity(String providerType, String identifier);

    /**
     * Saves or updates an Account aggregate.
     *
     * @param account The Account aggregate.
     * @return Future indicating completion of the save transaction.
     */
    CompletableFuture<Void> save(Account account);

    /**
     * Deletes an Account by ID.
     *
     * @param id AccountId to purge.
     * @return Future indicating completion of the delete transaction.
     */
    CompletableFuture<Void> delete(AccountId id);
}
