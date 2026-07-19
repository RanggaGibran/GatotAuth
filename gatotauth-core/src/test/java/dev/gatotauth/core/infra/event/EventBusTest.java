package dev.gatotauth.core.infra.event;

import static org.assertj.core.api.Assertions.assertThat;

import dev.gatotauth.api.event.GatotAuthEvent;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class EventBusTest {

    static class MockEvent implements GatotAuthEvent {}
    static class AnotherMockEvent implements GatotAuthEvent {}

    @Test
    void shouldDispatchEventToSubscribers() {
        EventBus bus = new EventBus(Runnable::run);
        AtomicBoolean called = new AtomicBoolean(false);

        bus.subscribe(MockEvent.class, event -> called.set(true));
        bus.publish(new MockEvent());

        assertThat(called.get()).isTrue();
    }

    @Test
    void shouldOnlyDispatchToCorrectEventType() {
        EventBus bus = new EventBus(Runnable::run);
        AtomicInteger mockCalled = new AtomicInteger(0);
        AtomicInteger anotherCalled = new AtomicInteger(0);

        bus.subscribe(MockEvent.class, event -> mockCalled.incrementAndGet());
        bus.subscribe(AnotherMockEvent.class, event -> anotherCalled.incrementAndGet());

        bus.publish(new MockEvent());

        assertThat(mockCalled.get()).isEqualTo(1);
        assertThat(anotherCalled.get()).isZero();
    }

    @Test
    void shouldHandleListenerExceptionGracefully() {
        EventBus bus = new EventBus(Runnable::run);
        AtomicBoolean nextCalled = new AtomicBoolean(false);

        bus.subscribe(MockEvent.class, event -> {
            throw new RuntimeException("Test Exception");
        });
        bus.subscribe(MockEvent.class, event -> nextCalled.set(true));

        bus.publish(new MockEvent());

        assertThat(nextCalled.get()).isTrue();
    }

    @Test
    void shouldDispatchAsynchronously() throws InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        EventBus bus = new EventBus(executor);
        CountDownLatch latch = new CountDownLatch(1);

        bus.subscribe(MockEvent.class, event -> latch.countDown());
        bus.publish(new MockEvent());

        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        executor.shutdown();
    }
}
