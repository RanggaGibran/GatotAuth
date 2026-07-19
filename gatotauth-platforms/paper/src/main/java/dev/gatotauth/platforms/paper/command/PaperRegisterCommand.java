package dev.gatotauth.platforms.paper.command;

import dev.gatotauth.api.domain.account.AccountId;
import dev.gatotauth.api.service.AccountManager;
import dev.gatotauth.api.service.SessionManager;
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
 * PaperRegisterCommand executes Paper /register command creating a player account.
 */
@SuppressWarnings("UnstableApiUsage")
public final class PaperRegisterCommand implements BasicCommand {
    private final GatotAuthEngine engine;

    /**
     * Instantiates PaperRegisterCommand.
     */
    public PaperRegisterCommand(GatotAuthEngine engine) {
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
            player.sendMessage(ChatColor.RED + "Usage: /register <password>");
            return;
        }

        String password = args[0];

        AccountManager accountManager = engine.getRegistry().get(AccountManager.class);
        SessionManager sessionManager = engine.getRegistry().get(SessionManager.class);

        if (accountManager == null || sessionManager == null) {
            player.sendMessage(ChatColor.RED + "Registration service is currently unavailable.");
            return;
        }

        player.sendMessage(ChatColor.GRAY + "Registering...");
        AccountId accountId = new AccountId(player.getUniqueId());
        accountManager.registerAccount(accountId, player.getName(), password).thenAccept(registered -> {
            if (registered) {
                String ip = player.getAddress() != null && player.getAddress().getAddress() != null
                        ? player.getAddress().getAddress().getHostAddress()
                        : "127.0.0.1";
                sessionManager.createSession(player.getUniqueId(), ip).thenAccept(session -> {
                    player.sendMessage(ChatColor.GREEN + "Registered and authenticated successfully!");
                });
            } else {
                player.sendMessage(ChatColor.RED + "Registration failed. Account might already exist.");
            }
        });
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack source, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
