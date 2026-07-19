package dev.gatotauth.platforms.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.gatotauth.core.infra.bootstrap.GatotAuthEngine;
import dev.gatotauth.core.infra.logging.GatotAuthLogger;
import dev.gatotauth.platforms.velocity.command.LoginCommand;
import dev.gatotauth.platforms.velocity.command.RegisterCommand;
import dev.gatotauth.platforms.velocity.listener.RestrictionListener;
import java.nio.file.Path;
import org.slf4j.Logger;

/**
 * Main Velocity plugin entry point loading the GatotAuth engine.
 */
@Plugin(
        id = "gatotauth",
        name = "GatotAuth",
        version = "1.0.0-SNAPSHOT",
        authors = {"dev.gatotauth"}
)
public final class VelocityGatotAuthPlugin {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private GatotAuthEngine engine;

    /**
     * Instantiates the Velocity plugin bootstrap.
     */
    @Inject
    public VelocityGatotAuthPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    /**
     * Listens to Velocity ProxyInitializeEvent.
     */
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        GatotAuthLogger coreLogger = new GatotAuthLogger() {
            @Override
            public void info(String message) {
                logger.info(message);
            }

            @Override
            public void warn(String message) {
                logger.warn(message);
            }

            @Override
            public void error(String message) {
                logger.error(message);
            }

            @Override
            public void error(String message, Throwable cause) {
                logger.error(message, cause);
            }
        };

        this.engine = new GatotAuthEngine(coreLogger);
        this.engine.bootstrap();

        server.getEventManager().register(this, new VelocityPlayerListener(engine));
        server.getEventManager().register(this, new RestrictionListener(engine));

        CommandManager commandManager = server.getCommandManager();

        CommandMeta loginMeta = commandManager.metaBuilder("login").build();
        commandManager.register(loginMeta, new LoginCommand(engine));

        CommandMeta registerMeta = commandManager.metaBuilder("register").build();
        commandManager.register(registerMeta, new RegisterCommand(engine));

        logger.info("GatotAuth Velocity adapter initialized successfully.");
    }

    /**
     * Listens to Velocity ProxyShutdownEvent.
     */
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (engine != null) {
            engine.shutdown();
        }
        logger.info("GatotAuth Velocity adapter stopped.");
    }
}
