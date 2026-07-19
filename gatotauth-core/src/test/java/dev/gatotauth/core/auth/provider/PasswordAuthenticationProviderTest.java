package dev.gatotauth.core.auth.provider;

import static org.assertj.core.api.Assertions.assertThat;

import dev.gatotauth.api.domain.account.Account;
import dev.gatotauth.api.domain.account.AccountId;
import dev.gatotauth.api.domain.account.Device;
import dev.gatotauth.api.domain.account.PasswordCredential;
import dev.gatotauth.api.domain.account.SecurityPolicy;
import dev.gatotauth.api.domain.account.TotpCredential;
import dev.gatotauth.core.auth.AuthenticationContext;
import dev.gatotauth.core.auth.AuthenticationDecision;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PasswordAuthenticationProviderTest {
    private PasswordAuthenticationProvider provider;
    private UUID userUuid;

    @BeforeEach
    void setUp() {
        provider = new PasswordAuthenticationProvider();
        userUuid = UUID.randomUUID();
    }

    @Test
    void shouldSupportPasswordCredentialOnly() {
        AuthenticationContext passwordCtx = new AuthenticationContext(
                userUuid, "Player", new PasswordCredential("pass"), "127.0.0.1", new Device("OS", "HardwareId")
        );
        AuthenticationContext totpCtx = new AuthenticationContext(
                userUuid, "Player", new TotpCredential("secret"), "127.0.0.1", new Device("OS", "HardwareId")
        );

        assertThat(provider.supports(passwordCtx)).isTrue();
        assertThat(provider.supports(totpCtx)).isFalse();
    }

    @Test
    void shouldAllowWhenPasswordsMatch() throws ExecutionException, InterruptedException {
        PasswordCredential input = new PasswordCredential("match");
        AuthenticationContext ctx = new AuthenticationContext(
                userUuid, "Player", input, "127.0.0.1", new Device("OS", "HardwareId")
        );

        Account account = new Account(
                new AccountId(userUuid),
                List.of(),
                List.of(input),
                List.of(),
                List.of(),
                new SecurityPolicy(8, 5, Duration.ofMinutes(15)),
                Map.of(),
                Instant.now(),
                false
        );

        AuthenticationDecision decision = new AuthenticationDecision();
        provider.authenticate(ctx, account, decision).get();

        assertThat(decision.isCompleted()).isTrue();
        assertThat(decision.getFailureReason()).isEmpty();
    }

    @Test
    void shouldRejectWhenPasswordsDoNotMatch() throws ExecutionException, InterruptedException {
        AuthenticationContext ctx = new AuthenticationContext(
                userUuid, "Player", new PasswordCredential("wrong"), "127.0.0.1", new Device("OS", "HardwareId")
        );

        Account account = new Account(
                new AccountId(userUuid),
                List.of(),
                List.of(new PasswordCredential("correct")),
                List.of(),
                List.of(),
                new SecurityPolicy(8, 5, Duration.ofMinutes(15)),
                Map.of(),
                Instant.now(),
                false
        );

        AuthenticationDecision decision = new AuthenticationDecision();
        provider.authenticate(ctx, account, decision).get();

        assertThat(decision.isCompleted()).isTrue();
        assertThat(decision.getFailureReason()).isPresent()
                .hasValueSatisfying(reason -> assertThat(reason).contains("Invalid password credentials"));
    }

    @Test
    void shouldRejectWhenNoPasswordRegistered() throws ExecutionException, InterruptedException {
        AuthenticationContext ctx = new AuthenticationContext(
                userUuid, "Player", new PasswordCredential("match"), "127.0.0.1", new Device("OS", "HardwareId")
        );

        Account account = new Account(
                new AccountId(userUuid),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                new SecurityPolicy(8, 5, Duration.ofMinutes(15)),
                Map.of(),
                Instant.now(),
                false
        );

        AuthenticationDecision decision = new AuthenticationDecision();
        provider.authenticate(ctx, account, decision).get();

        assertThat(decision.isCompleted()).isTrue();
        assertThat(decision.getFailureReason()).isPresent()
                .hasValueSatisfying(reason -> assertThat(reason).contains("No password registered"));
    }
}
