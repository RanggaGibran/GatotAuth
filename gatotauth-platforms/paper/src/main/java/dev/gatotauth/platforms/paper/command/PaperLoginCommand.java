package dev.gatotauth.platforms.paper.command;

import dev.gatotauth.api.domain.account.Device;
import dev.gatotauth.api.domain.account.PasswordCredential;
import dev.gatotauth.core.auth.AuthenticationContext;
import dev.gatotauth.core.auth.AuthenticationEngine;
import dev.gatotauth.core.auth.AuthenticationResult;
import dev.gatotauth.core.infra.bootstrap.GatotAuthEngine;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * PaperLoginCommand executes Paper /login command verifying password credentials.
 */
@SuppressWarnings("UnstableApiUsage")
public final class PaperLoginCommand implements BasicCommand {
    private final GatotAuthEngine engine;

    /**
     * Instantiates PaperLoginCommand.
     */
    public PaperLoginCommand(GatotAuthEngine engine) {
        this.engine = Objects.requireNonNull(engine, "engine cannot be null");
    }

    @Override
    public void execute(@NotNull CommandSourceStack source, @NotNull String[] args) {
        CommandSender sender = source.getSender();
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
            return;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /login <password>");
            return;
        }

        String password = args[0];
        String ip = player.getAddress() != null && player.getAddress().getAddress() != null
                ? player.getAddress().getAddress().getHostAddress()
                : "127.0.0.1";

        AuthenticationContext context = new AuthenticationContext(
                player.getUniqueId(),
                player.getName(),
                new PasswordCredential(password),
                ip,
                new Device("MinecraftServer", "PaperClient")
        );

        AuthenticationEngine authEngine = engine.getRegistry().get(AuthenticationEngine.class);
        if (authEngine == null) {
            player.sendMessage(ChatColor.RED + "Authentication service is currently unavailable.");
            return;
        }

        player.sendMessage(ChatColor.GRAY + "Authenticating...");
        authEngine.authenticate(context).thenAccept(result -> {
            if (result instanceof AuthenticationResult.Success) {
                player.sendMessage(ChatColor.GREEN + "Authenticated successfully!");
            } else if (result instanceof AuthenticationResult.Failure failure) {
                player.sendMessage(ChatColor.RED + "Authentication failed: " + failure.reason());
            }
        });
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
