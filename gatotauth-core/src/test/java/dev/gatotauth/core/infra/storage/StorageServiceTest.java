package dev.gatotauth.core.infra.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import dev.gatotauth.api.provider.AccountRepository;
import dev.gatotauth.api.provider.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StorageServiceTest {
    private StorageService storageService;
    private AccountRepository mockAccountRepo;
    private SessionRepository mockSessionRepo;

    @BeforeEach
    void setUp() {
        storageService = new StorageService();
        mockAccountRepo = mock(AccountRepository.class);
        mockSessionRepo = mock(SessionRepository.class);
    }

    @Test
    void shouldThrowExceptionIfStartedWithoutRepositories() {
        assertThatThrownBy(() -> storageService.start())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Repositories must be registered");
    }

    @Test
    void shouldRegisterAndRetrieveRepositories() {
        storageService.registerRepositories(mockAccountRepo, mockSessionRepo);
        storageService.start();

        assertThat(storageService.isRunning()).isTrue();
        assertThat(storageService.getAccountRepository()).contains(mockAccountRepo);
        assertThat(storageService.getSessionRepository()).contains(mockSessionRepo);

        storageService.stop();
        assertThat(storageService.isRunning()).isFalse();
    }
}
