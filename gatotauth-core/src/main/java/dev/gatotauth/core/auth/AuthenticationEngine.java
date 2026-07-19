package dev.gatotauth.core.auth;

import dev.gatotauth.api.domain.account.Account;
import dev.gatotauth.api.domain.account.AccountId;
import dev.gatotauth.api.event.AccountLoginEvent;
import dev.gatotauth.api.service.AccountManager;
import dev.gatotauth.api.service.SessionManager;
import dev.gatotauth.core.infra.event.EventBus;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * AuthenticationEngine coordinates the authentication execution flow.
 */
public final class AuthenticationEngine {
    private final AccountManager accountManager;
    private final SessionManager sessionManager;
    private final AuthenticationPipeline pipeline;
    private final EventBus eventBus;
    private final Executor computeExecutor;

    /**
     * Instantiates the AuthenticationEngine.
     */
    public AuthenticationEngine(
            AccountManager accountManager,
            SessionManager sessionManager,
            AuthenticationPipeline pipeline,
            EventBus eventBus,
            Executor computeExecutor
    ) {
        this.accountManager = Objects.requireNonNull(accountManager, "accountManager cannot be null");
        this.sessionManager = Objects.requireNonNull(sessionManager, "sessionManager cannot be null");
        this.pipeline = Objects.requireNonNull(pipeline, "pipeline cannot be null");
        this.eventBus = Objects.requireNonNull(eventBus, "eventBus cannot be null");
        this.computeExecutor = Objects.requireNonNull(computeExecutor, "computeExecutor cannot be null");
    }

    /**
     * Executes the authentication process asynchronously.
     *
     * @param context The authentication context.
     * @return CompletableFuture resolving to the outcome result.
     */
    public CompletableFuture<AuthenticationResult> authenticate(AuthenticationContext context) {
        return accountManager.findById(new AccountId(context.userUuid()))
                .thenComposeAsync(optAccount -> {
                    if (optAccount.isEmpty()) {
                        return CompletableFuture.completedFuture(
                                new AuthenticationResult.Failure("Account not registered")
                        );
                    }

                    Account account = optAccount.get();
                    if (account.isLocked()) {
                        return CompletableFuture.completedFuture(
                                new AuthenticationResult.Failure("Account locked")
                        );
                    }

                    return pipeline.execute(context, account)
                            .thenCompose(decision -> {
                                if (decision.getFailureReason().isPresent()) {
                                    return CompletableFuture.completedFuture(
                                            new AuthenticationResult.Failure(decision.getFailureReason().get())
                                    );
                                }

                                return sessionManager.createSession(context.userUuid(), context.ipAddress())
                                        .thenApply(session -> {
                                            eventBus.publish(new AccountLoginEvent(account, session));
                                            return new AuthenticationResult.Success(session);
                                        });
                            });
                }, computeExecutor);
    }
}
