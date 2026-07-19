package dev.gatotauth.api.service;

import java.util.Objects;

/**
 * Holder managing the registration and lookup of the GatotAuthApi singleton instance.
 * <p>
 * API Status: Internal
 *
 * @since 1.0.0
 */
public final class GatotAuthApiProvider {
    private static GatotAuthApi instance;

    private GatotAuthApiProvider() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Gets the registered API instance.
     *
     * @return GatotAuthApi instance.
     * @throws IllegalStateException if API is not yet registered.
     */
    public static GatotAuthApi get() {
        if (instance == null) {
            throw new IllegalStateException("GatotAuth API is not registered yet");
        }
        return instance;
    }

    /**
     * Registers the singleton API instance.
     *
     * @param apiInstance The implementation instance.
     * @throws IllegalStateException if an instance is already registered.
     */
    public static void register(GatotAuthApi apiInstance) {
        Objects.requireNonNull(apiInstance, "API instance cannot be null");
        if (instance != null) {
            throw new IllegalStateException("GatotAuth API is already registered");
        }
        instance = apiInstance;
    }

    /**
     * Unregisters the current instance.
     */
    public static void unregister() {
        instance = null;
    }
}
