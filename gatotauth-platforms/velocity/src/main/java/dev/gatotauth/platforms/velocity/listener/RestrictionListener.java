package dev.gatotauth.platforms.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import dev.gatotauth.api.service.SessionManager;
import dev.gatotauth.core.infra.bootstrap.GatotAuthEngine;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * RestrictionListener cancels chat, non-auth commands, and server jumps for unauthenticated players.
 */
public final class RestrictionListener {
    private final GatotAuthEngine engine;

    /**
     * Instantiates RestrictionListener.
     */
    public RestrictionListener(GatotAuthEngine engine) {
        this.engine = Objects.requireNonNull(engine, "engine cannot be null");
    }

    private CompletableFuture<Boolean> isAuth(Player player) {
        SessionManager sessionManager = engine.getRegistry().get(SessionManager.class);
        if (sessionManager == null) {
            return CompletableFuture.completedFuture(false);
        }
        String ip = player.getRemoteAddress().getAddress().getHostAddress();
        return sessionManager.hasActiveSession(player.getUniqueId(), ip);
    }

    /**
     * Enforces chat restrictions.
     */
    @Subscribe
    public CompletableFuture<PlayerChatEvent.ChatResult> onPlayerChat(PlayerChatEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return CompletableFuture.completedFuture(PlayerChatEvent.ChatResult.allowed());
        }

        return isAuth(player).thenApply(authenticated -> {
            if (authenticated) {
                return PlayerChatEvent.ChatResult.allowed();
            }
            player.sendMessage(Component.text(
                    "Please authenticate using /login <password> or /register <password>",
                    NamedTextColor.RED
            ));
            return PlayerChatEvent.ChatResult.denied();
        });
    }

    /**
     * Enforces command restrictions.
     */
    @Subscribe
    public CompletableFuture<CommandExecuteEvent.CommandResult> onCommandExecute(CommandExecuteEvent event) {
        if (!(event.getCommandSource() instanceof Player player)) {
            return CompletableFuture.completedFuture(CommandExecuteEvent.CommandResult.allowed());
        }

        String command = event.getCommand().trim().toLowerCase();
        // Allow login and register commands
        if (command.startsWith("login") || command.startsWith("register")
                || command.startsWith("/login") || command.startsWith("/register")) {
            return CompletableFuture.completedFuture(CommandExecuteEvent.CommandResult.allowed());
        }

        return isAuth(player).thenApply(authenticated -> {
            if (authenticated) {
                return CommandExecuteEvent.CommandResult.allowed();
            }
            player.sendMessage(Component.text(
                    "Please authenticate using /login <password> or /register <password>",
                    NamedTextColor.RED
            ));
            return CommandExecuteEvent.CommandResult.denied();
        });
    }

    /**
     * Enforces server connection switch restrictions.
     */
    @Subscribe
    public CompletableFuture<ServerPreConnectEvent.ServerResult> onServerPreConnect(ServerPreConnectEvent event) {
        Player player = event.getPlayer();

        return isAuth(player).thenApply(authenticated -> {
            if (authenticated) {
                return ServerPreConnectEvent.ServerResult.allowed(
                        event.getOriginalServer()
                );
            }
            // Cancel transition connection to backend servers if not authenticated
            return ServerPreConnectEvent.ServerResult.denied();
        });
    }
}
