package dev.gatotauth.core.infra.bootstrap;

import dev.gatotauth.api.service.AccountManager;
import dev.gatotauth.api.service.SessionManager;
import dev.gatotauth.core.account.DefaultAccountManager;
import dev.gatotauth.core.auth.AuthenticationEngine;
import dev.gatotauth.core.auth.AuthenticationPipeline;
import dev.gatotauth.core.infra.config.ConfigurationService;
import dev.gatotauth.core.infra.event.EventBus;
import dev.gatotauth.core.infra.lifecycle.LifecycleManager;
import dev.gatotauth.core.infra.logging.GatotAuthLogger;
import dev.gatotauth.core.infra.registry.ServiceRegistry;
import dev.gatotauth.core.infra.scheduler.GatotAuthScheduler;
import dev.gatotauth.core.infra.storage.StorageService;
import dev.gatotauth.core.infra.storage.sql.DatabaseConnector;
import dev.gatotauth.core.infra.storage.sql.SqlAccountRepository;
import dev.gatotauth.core.infra.storage.sql.SqlSessionRepository;
import dev.gatotauth.core.session.DefaultSessionManager;
import java.time.Duration;
import java.util.Objects;

/**
 * GatotAuthEngine orchestrates core engine bootstraps, registering services and directing lifecycles.
 */
public final class GatotAuthEngine {
    private final ServiceRegistry registry;
    private final LifecycleManager lifecycleManager;
    private final GatotAuthLogger logger;
    private GatotAuthScheduler scheduler;
    private DatabaseConnector dbConnector;

    /**
     * Initializes the core engine configuration.
     *
     * @param logger Platform logger adapter.
     */
    public GatotAuthEngine(GatotAuthLogger logger) {
        this.logger = Objects.requireNonNull(logger, "Logger cannot be null");
        this.registry = new ServiceRegistry();
        this.lifecycleManager = new LifecycleManager();
    }

    /**
     * Executes the bootstrap startup sequence.
     */
    public synchronized void bootstrap() {
        logger.info("Initializing GatotAuth core engine...");

        this.scheduler = new GatotAuthScheduler();
        registry.register(GatotAuthScheduler.class, scheduler);

        EventBus eventBus = new EventBus(scheduler.io());
        registry.register(EventBus.class, eventBus);

        ConfigurationService configService = new ConfigurationService();
        registry.register(ConfigurationService.class, configService);

        this.dbConnector = new DatabaseConnector("gatotauth.db");
        SqlAccountRepository accountRepo = new SqlAccountRepository(dbConnector, scheduler.io());
        SqlSessionRepository sessionRepo = new SqlSessionRepository(dbConnector, scheduler.io());

        StorageService storageService = new StorageService();
        storageService.registerRepositories(accountRepo, sessionRepo);
        storageService.start();
        registry.register(StorageService.class, storageService);

        AccountManager accountManager = new DefaultAccountManager(accountRepo);
        registry.register(AccountManager.class, accountManager);

        SessionManager sessionManager = new DefaultSessionManager(sessionRepo, eventBus, Duration.ofMinutes(30));
        registry.register(SessionManager.class, sessionManager);

        AuthenticationPipeline pipeline = new AuthenticationPipeline();
        pipeline.registerProvider(new dev.gatotauth.core.auth.provider.PremiumAuthenticationProvider());
        pipeline.registerProvider(new dev.gatotauth.core.auth.provider.PasswordAuthenticationProvider());
        AuthenticationEngine authEngine = new AuthenticationEngine(
                accountManager,
                sessionManager,
                pipeline,
                eventBus,
                scheduler.compute()
        );
        registry.register(AuthenticationEngine.class, authEngine);

        registry.register(LifecycleManager.class, lifecycleManager);
        registry.register(ServiceRegistry.class, registry);

        lifecycleManager.start();
        logger.info("GatotAuth core engine bootstrapped successfully.");
    }

    /**
     * Executes the teardown shutdown sequence.
     */
    public synchronized void shutdown() {
        logger.info("Stopping GatotAuth core engine...");
        lifecycleManager.stop();
        StorageService storageService = registry.get(StorageService.class);
        if (storageService != null) {
            storageService.stop();
        }
        if (dbConnector != null) {
            dbConnector.close();
        }
        if (scheduler != null) {
            scheduler.close();
        }
        registry.clear();
        logger.info("GatotAuth core engine stopped.");
    }

    /**
     * Retrieves the service registry.
     *
     * @return ServiceRegistry instance.
     */
    public ServiceRegistry getRegistry() {
        return registry;
    }

    /**
     * Retrieves the lifecycle manager.
     *
     * @return LifecycleManager instance.
     */
    public LifecycleManager getLifecycleManager() {
        return lifecycleManager;
    }
}
