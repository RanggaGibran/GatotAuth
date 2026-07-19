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

        PaperLoginCommand loginCmd = new PaperLoginCommand(engine);
        PaperRegisterCommand regCmd = new PaperRegisterCommand(engine);

        if (getCommand("login") != null) {
            getCommand("login").setExecutor(loginCmd);
        }
        if (getCommand("register") != null) {
            getCommand("register").setExecutor(regCmd);
        }

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
