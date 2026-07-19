package dev.gatotauth.core.infra.scheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * GatotAuthScheduler coordinates thread execution strategies using modern Java 21 features.
 */
public final class GatotAuthScheduler implements AutoCloseable {
    private final ExecutorService ioExecutor;
    private final ExecutorService computeExecutor;

    /**
     * Constructs the scheduler.
     */
    public GatotAuthScheduler() {
        this.ioExecutor = Executors.newVirtualThreadPerTaskExecutor();

        int cores = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        this.computeExecutor = Executors.newFixedThreadPool(cores);
    }

    /**
     * Returns the I/O-bound virtual thread executor.
     *
     * @return ExecutorService.
     */
    public Executor io() {
        return ioExecutor;
    }

    /**
     * Returns the CPU-bound platform thread executor.
     *
     * @return Executor.
     */
    public Executor compute() {
        return computeExecutor;
    }

    @Override
    public void close() {
        ioExecutor.shutdown();
        computeExecutor.shutdown();
        try {
            if (!ioExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                ioExecutor.shutdownNow();
            }
            if (!computeExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                computeExecutor.shutdownNow();
            }
        } catch (InterruptedException ex) {
            ioExecutor.shutdownNow();
            computeExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
