package dev.gatotauth.core.infra.lifecycle;

/**
 * Represents a service component with a defined startup and shutdown lifecycle.
 */
public interface Lifecycle {
    /**
     * Triggers component initialization routines.
     */
    void start();

    /**
     * Triggers component cleanup routines.
     */
    void stop();
}
