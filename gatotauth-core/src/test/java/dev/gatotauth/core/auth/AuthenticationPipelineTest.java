package dev.gatotauth.core.auth;

import static org.assertj.core.api.Assertions.assertThat;

import dev.gatotauth.api.domain.account.Account;
import dev.gatotauth.api.domain.account.AccountId;
import dev.gatotauth.api.domain.account.Device;
import dev.gatotauth.api.domain.account.PasswordCredential;
import dev.gatotauth.api.domain.account.SecurityPolicy;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthenticationPipelineTest {
    private AuthenticationPipeline pipeline;
    private AuthenticationContext context;
    private Account account;

    @BeforeEach
    void setUp() {
        pipeline = new AuthenticationPipeline();
        context = new AuthenticationContext(
                UUID.randomUUID(),
                "PlayerName",
                new PasswordCredential("hash"),
                "127.0.0.1",
                new Device("OS", "HardwareId")
        );
        account = new Account(
                new AccountId(context.userUuid()),
                List.of(),
                List.of(context.credential()),
                List.of(),
                List.of(context.device()),
                new SecurityPolicy(8, 5, Duration.ofMinutes(15)),
                Map.of(),
                Instant.now(),
                false
        );
    }

    @Test
    void shouldFailClosedWhenNoProvidersRegistered() throws ExecutionException, InterruptedException {
        AuthenticationDecision decision = pipeline.execute(context, account).get();

        assertThat(decision.isCompleted()).isTrue();
        assertThat(decision.getFailureReason()).isPresent()
                .hasValueSatisfying(reason -> assertThat(reason).contains("No compatible authentication provider"));
    }

    @Test
    void shouldSkipUnsupportedProviders() throws ExecutionException, InterruptedException {
        AuthenticationProvider unsupported = new AuthenticationProvider() {
            @Override
            public boolean supports(AuthenticationContext context) {
                return false;
            }

            @Override
            public CompletableFuture<Void> authenticate(
                    AuthenticationContext context, Account account, AuthenticationDecision decision) {
                decision.allow();
                return CompletableFuture.completedFuture(null);
            }
        };

        pipeline.registerProvider(unsupported);
        AuthenticationDecision decision = pipeline.execute(context, account).get();

        assertThat(decision.isCompleted()).isTrue();
        assertThat(decision.getFailureReason()).isPresent();
    }

    @Test
    void shouldExecuteSupportedProviderAndStopOnCompletion() throws ExecutionException, InterruptedException {
        AuthenticationProvider provider = new AuthenticationProvider() {
            @Override
            public boolean supports(AuthenticationContext context) {
                return true;
            }

            @Override
            public CompletableFuture<Void> authenticate(
                    AuthenticationContext context, Account account, AuthenticationDecision decision) {
                decision.allow();
                return CompletableFuture.completedFuture(null);
            }
        };

        pipeline.registerProvider(provider);
        AuthenticationDecision decision = pipeline.execute(context, account).get();

        assertThat(decision.isCompleted()).isTrue();
        assertThat(decision.getFailureReason()).isEmpty();
    }
}
