package dev.gatotauth.api.provider;

import dev.gatotauth.api.domain.account.Account;
import java.util.concurrent.CompletableFuture;

/**
 * Service provider interface for delivering 2FA or verification notifications.
 * <p>
 * API Status: Stable
 *
 * @since 1.0.0
 */
public interface NotificationProvider {
    /**
     * Sends an authentication/verification code to a player account.
     *
     * @param account The target Account.
     * @param code The verification code payload.
     * @return CompletableFuture completing when the notification is sent.
     */
    CompletableFuture<Void> sendVerificationCode(Account account, String code);
}
