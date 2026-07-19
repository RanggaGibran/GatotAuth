package dev.gatotauth.platforms.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import dev.gatotauth.api.domain.account.Device;
import dev.gatotauth.api.domain.account.PasswordCredential;
import dev.gatotauth.core.auth.AuthenticationContext;
import dev.gatotauth.core.auth.AuthenticationEngine;
import dev.gatotauth.core.auth.AuthenticationResult;
import dev.gatotauth.core.infra.bootstrap.GatotAuthEngine;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * LoginCommand executes proxy /login command verifying password credentials.
 */
public final class LoginCommand implements SimpleCommand {
    private final GatotAuthEngine engine;

    /**
     * Instantiates LoginCommand.
     */
    public LoginCommand(GatotAuthEngine engine) {
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
            player.sendMessage(Component.text("Usage: /login <password>", NamedTextColor.RED));
            return;
        }

        String password = args[0];
        String ip = player.getRemoteAddress().getAddress().getHostAddress();

        AuthenticationContext context = new AuthenticationContext(
                player.getUniqueId(),
                player.getUsername(),
                new PasswordCredential(password),
                ip,
                new Device("MinecraftProxy", "VelocityClient")
        );

        AuthenticationEngine authEngine = engine.getRegistry().get(AuthenticationEngine.class);
        if (authEngine == null) {
            player.sendMessage(Component.text("Authentication service is currently unavailable.", NamedTextColor.RED));
            return;
        }

        player.sendMessage(Component.text("Authenticating...", NamedTextColor.GRAY));
        authEngine.authenticate(context).thenAccept(result -> {
            if (result instanceof AuthenticationResult.Success) {
                player.sendMessage(Component.text("Authenticated successfully!", NamedTextColor.GREEN));
            } else if (result instanceof AuthenticationResult.Failure failure) {
                player.sendMessage(Component.text("Authentication failed: " + failure.reason(), NamedTextColor.RED));
            }
        });
    }
}
