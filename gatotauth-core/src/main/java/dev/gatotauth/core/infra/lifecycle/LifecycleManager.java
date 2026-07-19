package dev.gatotauth.core.infra.lifecycle;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages registration, startup, and shutdown orders for core components.
 */
public final class LifecycleManager {
    private final List<Lifecycle> components = new CopyOnWriteArrayList<>();
    private volatile boolean running = false;

    /**
     * Registers a lifecycle component.
     *
     * @param component Lifecycle object instance.
     */
    public void register(Lifecycle component) {
        components.add(component);
        if (running) {
            component.start();
        }
    }

    /**
     * Starts all registered lifecycle components.
     */
    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        for (Lifecycle component : components) {
            try {
                component.start();
            } catch (Exception ex) {
                System.err.println("Failed to start lifecycle component: " + ex.getMessage());
            }
        }
    }

    /**
     * Stops all registered lifecycle components in reverse order.
     */
    public synchronized void stop() {
        if (!running) {
            return;
        }
        running = false;
        for (int i = components.size() - 1; i >= 0; i--) {
            try {
                components.get(i).stop();
            } catch (Exception ex) {
                System.err.println("Failed to stop lifecycle component: " + ex.getMessage());
            }
        }
        components.clear();
    }

    /**
     * Checks if the manager is in running state.
     *
     * @return true if running.
     */
    public boolean isRunning() {
        return running;
    }
}
