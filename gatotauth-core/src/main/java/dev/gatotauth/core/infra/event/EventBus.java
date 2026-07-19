package dev.gatotauth.core.infra.event;

import dev.gatotauth.api.event.GatotAuthEvent;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * EventBus handles non-blocking domain event dispatching and handler subscriptions.
 */
public final class EventBus {
    private final Executor executor;
    private final Map<Class<? extends GatotAuthEvent>, List<Consumer<?>>> listeners = new ConcurrentHashMap<>();

    /**
     * Constructs the EventBus.
     *
     * @param executor The executor (ideally virtual threads executor) used to dispatch events asynchronously.
     */
    public EventBus(Executor executor) {
        this.executor = Objects.requireNonNull(executor, "Executor cannot be null");
    }

    /**
     * Registers a listener for a specific event type.
     *
     * @param eventType The class type of the event.
     * @param listener The listener callback consumer.
     * @param <T> The event type subclassing GatotAuthEvent.
     */
    public <T extends GatotAuthEvent> void subscribe(Class<T> eventType, Consumer<T> listener) {
        Objects.requireNonNull(eventType, "Event type cannot be null");
        Objects.requireNonNull(listener, "Listener cannot be null");
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    /**
     * Publishes a domain event, running all registered listeners asynchronously.
     *
     * @param event The event payload instance.
     */
    @SuppressWarnings("unchecked")
    public void publish(GatotAuthEvent event) {
        Objects.requireNonNull(event, "Event payload cannot be null");
        List<Consumer<?>> targets = listeners.get(event.getClass());
        if (targets == null || targets.isEmpty()) {
            return;
        }
        for (Consumer<?> target : targets) {
            Consumer<GatotAuthEvent> casted = (Consumer<GatotAuthEvent>) target;
            executor.execute(() -> {
                try {
                    casted.accept(event);
                } catch (Exception ex) {
                    System.err.println("Error dispatching event "
                            + event.getClass().getSimpleName() + ": " + ex.getMessage());
                }
            });
        }
    }

    /**
     * Clears all subscribers.
     */
    public void clear() {
        listeners.clear();
    }
}
