package dev.gatotauth.platforms.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import dev.gatotauth.api.domain.account.Device;
import dev.gatotauth.api.domain.account.PasswordCredential;
import dev.gatotauth.api.service.SessionManager;
import dev.gatotauth.core.auth.AuthenticationContext;
import dev.gatotauth.core.auth.AuthenticationEngine;
import dev.gatotauth.core.infra.bootstrap.GatotAuthEngine;
import java.util.UUID;

/**
 * VelocityPlayerListener translates platform proxy login/disconnect events to core auth triggers.
 */
public final class VelocityPlayerListener {
    private final GatotAuthEngine engine;

    /**
     * Instantiates VelocityPlayerListener.
     */
    public VelocityPlayerListener(GatotAuthEngine engine) {
        this.engine = engine;
    }

    /**
     * Handles Velocity login events.
     */
    @Subscribe
    public void onPlayerLogin(LoginEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String username = player.getUsername();
        String ipAddress = player.getRemoteAddress().getAddress().getHostAddress();

        Device device = new Device("MinecraftProxy", "VelocityClient");
        AuthenticationContext context = new AuthenticationContext(
                uuid,
                username,
                new PasswordCredential(""),
                ipAddress,
                device
        );

        AuthenticationEngine authEngine = engine.getRegistry().get(AuthenticationEngine.class);
        if (authEngine != null) {
            authEngine.authenticate(context);
        }
    }

    /**
     * Handles Velocity disconnect events.
     */
    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        SessionManager sessionManager = engine.getRegistry().get(SessionManager.class);
        if (sessionManager != null) {
            sessionManager.invalidateSession(uuid);
        }
    }
}
