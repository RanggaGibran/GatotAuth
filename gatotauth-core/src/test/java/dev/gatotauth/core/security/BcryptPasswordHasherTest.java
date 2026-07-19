package dev.gatotauth.core.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class BcryptPasswordHasherTest {
    @Test
    void shouldHashAndVerifyPasswordsSuccessfully() {
        BcryptPasswordHasher hasher = new BcryptPasswordHasher(10);
        String plain = "secr3t_p@ss!";
        String hash = hasher.hash(plain);

        assertThat(hash).isNotEqualTo(plain);
        assertThat(hasher.verify(plain, hash)).isTrue();
        assertThat(hasher.verify("wrong", hash)).isFalse();
    }

    @Test
    void shouldThrowExceptionOnInvalidLogRounds() {
        assertThatThrownBy(() -> new BcryptPasswordHasher(3))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new BcryptPasswordHasher(32))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
