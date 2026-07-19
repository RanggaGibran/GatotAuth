package dev.gatotauth.core.auth.provider;

import dev.gatotauth.api.domain.account.Account;
import dev.gatotauth.api.domain.account.PasswordCredential;
import dev.gatotauth.core.auth.AuthenticationContext;
import dev.gatotauth.core.auth.AuthenticationDecision;
import dev.gatotauth.core.auth.AuthenticationProvider;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * PasswordAuthenticationProvider validates PasswordCredential against stored passwords.
 */
public final class PasswordAuthenticationProvider implements AuthenticationProvider {
    @Override
    public boolean supports(AuthenticationContext context) {
        return context.credential() instanceof PasswordCredential;
    }

    @Override
    public CompletableFuture<Void> authenticate(
            AuthenticationContext context,
            Account account,
            AuthenticationDecision decision
    ) {
        PasswordCredential input = (PasswordCredential) context.credential();
        Optional<PasswordCredential> storedOpt = account.findCredential(PasswordCredential.class);

        if (storedOpt.isEmpty()) {
            decision.reject("No password registered for this account");
            return CompletableFuture.completedFuture(null);
        }

        PasswordCredential stored = storedOpt.get();
        if (stored.hashedPassword().equals(input.hashedPassword())) {
            decision.allow();
        } else {
            decision.reject("Invalid password credentials");
        }

        return CompletableFuture.completedFuture(null);
    }
}
