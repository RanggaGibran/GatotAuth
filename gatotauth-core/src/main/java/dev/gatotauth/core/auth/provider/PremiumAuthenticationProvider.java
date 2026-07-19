package dev.gatotauth.core.auth.provider;

import dev.gatotauth.api.domain.account.Account;
import dev.gatotauth.api.domain.account.MojangIdentity;
import dev.gatotauth.core.auth.AuthenticationContext;
import dev.gatotauth.core.auth.AuthenticationDecision;
import dev.gatotauth.core.auth.AuthenticationProvider;
import java.util.concurrent.CompletableFuture;

/**
 * PremiumAuthenticationProvider validates online Mojang identities and bypasses password prompts.
 */
public final class PremiumAuthenticationProvider implements AuthenticationProvider {
    @Override
    public boolean supports(AuthenticationContext context) {
        return true;
    }

    @Override
    public CompletableFuture<Void> authenticate(
            AuthenticationContext context,
            Account account,
            AuthenticationDecision decision
    ) {
        boolean hasPremiumIdentity = account.identities().stream()
                .anyMatch(identity -> identity instanceof MojangIdentity);

        if (hasPremiumIdentity) {
            boolean matches = account.identities().stream()
                    .filter(identity -> identity instanceof MojangIdentity)
                    .map(identity -> (MojangIdentity) identity)
                    .anyMatch(mi -> mi.mojangUuid().equals(context.userUuid()));

            if (matches) {
                decision.allow();
            } else {
                decision.reject("Premium UUID mismatch");
            }
        }
        return CompletableFuture.completedFuture(null);
    }
}
