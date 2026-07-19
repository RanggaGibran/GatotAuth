package dev.gatotauth.core.auth;

import dev.gatotauth.api.domain.account.Account;
import java.util.concurrent.CompletableFuture;

/**
 * AuthenticationProvider defines the contract to evaluate specific credentials.
 */
public interface AuthenticationProvider {
    /**
     * Checks if this provider supports the given authentication context.
     *
     * @param context Active connection context.
     * @return true if supported.
     */
    boolean supports(AuthenticationContext context);

    /**
     * Evaluates the credential inputs against the loaded Account identity.
     *
     * @param context Authentication connection inputs.
     * @param account Target player account details.
     * @param decision State tracker to approve or reject.
     * @return Future completing when authentication evaluation finishes.
     */
    CompletableFuture<Void> authenticate(
            AuthenticationContext context,
            Account account,
            AuthenticationDecision decision
    );
}
