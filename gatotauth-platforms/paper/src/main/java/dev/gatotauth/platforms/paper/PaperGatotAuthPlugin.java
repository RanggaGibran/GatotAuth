package dev.gatotauth.platforms.paper;

import dev.gatotauth.core.infra.bootstrap.GatotAuthEngine;
import dev.gatotauth.core.infra.logging.GatotAuthLogger;
import dev.gatotauth.platforms.paper.command.PaperLoginCommand;
import dev.gatotauth.platforms.paper.command.PaperRegisterCommand;
import dev.gatotauth.platforms.paper.listener.PaperPlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin entry point for Paper platform adapter.
 */
@SuppressWarnings("UnstableApiUsage")
public class PaperGatotAuthPlugin extends JavaPlugin {
    private GatotAuthEngine engine;

    @Override
    public void onEnable() {
        GatotAuthLogger coreLogger = new GatotAuthLogger() {
            @Override
            public void info(String message) {
                getLogger().info(message);
            }

            @Override
            public void warn(String message) {
                getLogger().warning(message);
            }

            @Override
            public void error(String message) {
                getLogger().severe(message);
            }

            @Override
            public void error(String message, Throwable cause) {
                getLogger().log(java.util.logging.Level.SEVERE, message, cause);
            }
        };

        this.engine = new GatotAuthEngine(coreLogger);
        this.engine.bootstrap();
        getServer().getPluginManager().registerEvents(new PaperPlayerListener(engine), this);

        this.getLifecycleManager().registerEventHandler(
                io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents.COMMANDS,
                event -> {
                    final io.papermc.paper.command.brigadier.Commands commands = event.registrar();
                    commands.register("login", new PaperLoginCommand(engine));
                    commands.register("register", new PaperRegisterCommand(engine));
                }
        );

        getLogger().info("GatotAuth Paper Platform Adapter initialized successfully.");
    }

    @Override
    public void onDisable() {
        if (engine != null) {
            engine.shutdown();
        }
        getLogger().info("GatotAuth Paper Platform Adapter disabled.");
    }
}
