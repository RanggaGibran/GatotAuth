package dev.gatotauth.platforms.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import dev.gatotauth.api.domain.account.AccountId;
import dev.gatotauth.api.service.AccountManager;
import dev.gatotauth.api.service.SessionManager;
import dev.gatotauth.core.infra.bootstrap.GatotAuthEngine;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * RegisterCommand executes proxy /register command creating a player account.
 */
public final class RegisterCommand implements SimpleCommand {
    private final GatotAuthEngine engine;

    /**
     * Instantiates RegisterCommand.
     */
    public RegisterCommand(GatotAuthEngine engine) {
        this.engine = Objects.requireNonNull(engine, "engine cannot be null");
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(Component.text(
                    "Only players can execute this command.",
                    NamedTextColor.RED
            ));
            return;
        }

        String[] args = invocation.arguments();
        if (args.length < 1) {
            player.sendMessage(Component.text("Usage: /register <password>", NamedTextColor.RED));
            return;
        }

        String password = args[0];

        AccountManager accountManager = engine.getRegistry().get(AccountManager.class);
        SessionManager sessionManager = engine.getRegistry().get(SessionManager.class);

        if (accountManager == null || sessionManager == null) {
            player.sendMessage(Component.text("Registration service is currently unavailable.", NamedTextColor.RED));
            return;
        }

        player.sendMessage(Component.text("Registering...", NamedTextColor.GRAY));
        AccountId accountId = new AccountId(player.getUniqueId());
        accountManager.registerAccount(accountId, player.getUsername(), password).thenAccept(registered -> {
            if (registered) {
                String ip = player.getRemoteAddress().getAddress().getHostAddress();
                sessionManager.createSession(player.getUniqueId(), ip).thenAccept(session -> {
                    player.sendMessage(Component.text(
                            "Registered and authenticated successfully!",
                            NamedTextColor.GREEN
                    ));
                });
            } else {
                player.sendMessage(Component.text(
                        "Registration failed. Account might already exist.",
                        NamedTextColor.RED
                ));
            }
        });
    }
}
