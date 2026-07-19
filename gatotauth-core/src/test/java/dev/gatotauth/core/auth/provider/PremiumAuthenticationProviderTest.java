package dev.gatotauth.core.auth.provider;

import static org.assertj.core.api.Assertions.assertThat;

import dev.gatotauth.api.domain.account.Account;
import dev.gatotauth.api.domain.account.AccountId;
import dev.gatotauth.api.domain.account.Device;
import dev.gatotauth.api.domain.account.MojangIdentity;
import dev.gatotauth.api.domain.account.OfflineIdentity;
import dev.gatotauth.api.domain.account.PasswordCredential;
import dev.gatotauth.api.domain.account.SecurityPolicy;
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

class PremiumAuthenticationProviderTest {
    private PremiumAuthenticationProvider provider;
    private UUID userUuid;

    @BeforeEach
    void setUp() {
        provider = new PremiumAuthenticationProvider();
        userUuid = UUID.randomUUID();
    }

    @Test
    void shouldAllowWhenUuidMatchesMojangIdentity() throws ExecutionException, InterruptedException {
        AuthenticationContext ctx = new AuthenticationContext(
                userUuid, "PremiumPlayer", new PasswordCredential(""), "127.0.0.1", new Device("OS", "HardwareId")
        );

        Account account = new Account(
                new AccountId(userUuid),
                List.of(new MojangIdentity(userUuid, "PremiumPlayer")),
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
        assertThat(decision.getFailureReason()).isEmpty();
    }

    @Test
    void shouldRejectWhenUuidDiffersFromMojangIdentity() throws ExecutionException, InterruptedException {
        UUID differentUuid = UUID.randomUUID();
        AuthenticationContext ctx = new AuthenticationContext(
                userUuid, "PremiumPlayer", new PasswordCredential(""), "127.0.0.1", new Device("OS", "HardwareId")
        );

        Account account = new Account(
                new AccountId(userUuid),
                List.of(new MojangIdentity(differentUuid, "PremiumPlayer")),
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
                .hasValueSatisfying(reason -> assertThat(reason).contains("Premium UUID mismatch"));
    }

    @Test
    void shouldNoOpWhenAccountHasNoPremiumIdentity() throws ExecutionException, InterruptedException {
        AuthenticationContext ctx = new AuthenticationContext(
                userUuid, "OfflinePlayer", new PasswordCredential("pass"), "127.0.0.1", new Device("OS", "HardwareId")
        );

        Account account = new Account(
                new AccountId(userUuid),
                List.of(new OfflineIdentity(userUuid, "OfflinePlayer")),
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

        assertThat(decision.isCompleted()).isFalse();
    }
}
