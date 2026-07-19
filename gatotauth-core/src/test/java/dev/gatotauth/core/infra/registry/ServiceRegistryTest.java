package dev.gatotauth.core.infra.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServiceRegistryTest {
    private ServiceRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ServiceRegistry();
    }

    @Test
    void shouldRegisterAndRetrieveService() {
        String testService = "MyTestService";
        registry.register(String.class, testService);

        String retrieved = registry.get(String.class);
        assertThat(retrieved).isEqualTo(testService);
    }

    @Test
    void shouldThrowExceptionOnDuplicateRegistration() {
        registry.register(String.class, "First");

        assertThatThrownBy(() -> registry.register(String.class, "Second"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void shouldThrowExceptionOnMissingService() {
        assertThatThrownBy(() -> registry.get(Integer.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No service registered for type");
    }

    @Test
    void shouldClearServices() {
        registry.register(String.class, "ClearMe");
        registry.clear();

        assertThatThrownBy(() -> registry.get(String.class))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
