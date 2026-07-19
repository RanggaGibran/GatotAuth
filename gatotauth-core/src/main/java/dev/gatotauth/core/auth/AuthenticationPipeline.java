package dev.gatotauth.core.auth;

import dev.gatotauth.api.domain.account.Account;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * AuthenticationPipeline executes registered validation providers sequentially.
 */
public final class AuthenticationPipeline {
    private final List<AuthenticationProvider> providers = new CopyOnWriteArrayList<>();

    /**
     * Registers an authentication provider in the pipeline.
     *
     * @param provider Custom validator.
     */
    public void registerProvider(AuthenticationProvider provider) {
        providers.add(provider);
    }

    /**
     * Evaluates a connection context against registered providers asynchronously.
     *
     * @param context Active connection details.
     * @param account Target account.
     * @return Future containing the finalized decision.
     */
    public CompletableFuture<AuthenticationDecision> execute(AuthenticationContext context, Account account) {
        AuthenticationDecision decision = new AuthenticationDecision();
        CompletableFuture<Void> pipelineChain = CompletableFuture.completedFuture(null);

        for (AuthenticationProvider provider : providers) {
            if (provider.supports(context)) {
                pipelineChain = pipelineChain.thenCompose(v -> {
                    if (decision.isCompleted()) {
                        return CompletableFuture.completedFuture(null);
                    }
                    return provider.authenticate(context, account, decision);
                });
            }
        }

        return pipelineChain.thenApply(v -> {
            if (!decision.isCompleted()) {
                decision.reject("No compatible authentication provider resolved the connection");
            }
            return decision;
        });
    }
}
