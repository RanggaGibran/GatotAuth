package dev.gatotauth.api.service;

import dev.gatotauth.api.domain.account.Account;
import dev.gatotauth.api.domain.account.AccountId;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface managing Accounts and registrations.
 * <p>
 * API Status: Stable
 *
 * @since 1.0.0
 */
public interface AccountManager {
    /**
     * Finds an Account by its unique ID.
     *
     * @param id AccountId value.
     * @return Future containing the optional Account match.
     */
    CompletableFuture<Optional<Account>> findById(AccountId id);

    /**
     * Finds an Account by a registered username.
     *
     * @param username Minecraft username.
     * @return Future containing the optional Account match.
     */
    CompletableFuture<Optional<Account>> findByUsername(String username);

    /**
     * Registers a new player account with a password.
     *
     * @param id Account UUID.
     * @param username Minecraft username.
     * @param plainTextPassword Hashing source password.
     * @return Future indicating success status of the registration.
     */
    CompletableFuture<Boolean> registerAccount(AccountId id, String username, String plainTextPassword);

    /**
     * Lock an account dynamically, preventing session generation.
     *
     * @param id Target AccountId.
     * @param reason Description reason for the lockout.
     * @return Future resolving when state modification finishes.
     */
    CompletableFuture<Void> lockAccount(AccountId id, String reason);

    /**
     * Unlocks a locked account.
     *
     * @param id Target AccountId.
     * @return Future resolving when state modification finishes.
     */
    CompletableFuture<Void> unlockAccount(AccountId id);
}
