package dev.gatotauth.core.infra.registry;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ServiceRegistry implements a clean, reflection-free dependency injection container.
 * It manages type-safe singletons and resolves dependency lookups at runtime.
 */
public final class ServiceRegistry {
    private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

    /**
     * Registers a service implementation instance.
     *
     * @param serviceType The service type interface class.
     * @param instance The implementation instance.
     * @param <T> The service type.
     * @throws IllegalStateException if an instance of the type is already registered.
     */
    public <T> void register(Class<T> serviceType, T instance) {
        Objects.requireNonNull(serviceType, "Service type cannot be null");
        Objects.requireNonNull(instance, "Service instance cannot be null");
        Object existing = services.putIfAbsent(serviceType, instance);
        if (existing != null) {
            throw new IllegalStateException("Service of type " + serviceType.getName() + " already registered");
        }
    }

    /**
     * Retrieves a service implementation.
     *
     * @param serviceType The service interface type class.
     * @param <T> The service type.
     * @return The registered instance.
     * @throws IllegalArgumentException if no instance of the service is registered.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> serviceType) {
        Objects.requireNonNull(serviceType, "Service type cannot be null");
        Object instance = services.get(serviceType);
        if (instance == null) {
            throw new IllegalArgumentException("No service registered for type: " + serviceType.getName());
        }
        return (T) instance;
    }

    /**
     * Clears all registered services. Useful for reloading configurations or tests.
     */
    public void clear() {
        services.clear();
    }
}
