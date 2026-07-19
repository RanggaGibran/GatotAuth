package dev.gatotauth.platforms.paper.listener;

import dev.gatotauth.api.service.SessionManager;
import dev.gatotauth.core.infra.bootstrap.GatotAuthEngine;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * PaperPlayerListener bridges Paper events into core engine calls.
 */
public final class PaperPlayerListener implements Listener {
    private final GatotAuthEngine engine;

    /**
     * Instantiates PaperPlayerListener.
     */
    public PaperPlayerListener(GatotAuthEngine engine) {
        this.engine = Objects.requireNonNull(engine, "engine cannot be null");
    }

    /**
     * Handles player disconnect cleanup.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        SessionManager sessionManager = engine.getRegistry().get(SessionManager.class);
        if (sessionManager != null) {
            sessionManager.invalidateSession(player.getUniqueId());
        }
    }
}
