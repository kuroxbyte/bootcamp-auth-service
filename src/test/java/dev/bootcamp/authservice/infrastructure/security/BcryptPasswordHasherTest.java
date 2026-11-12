package dev.bootcamp.authservice.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BcryptPasswordHasherTest {

    private final BcryptPasswordHasher hasher = new BcryptPasswordHasher();

    @Test
    void hashesWithBcryptAndMatchesOnlyTheSamePassword() {
        String hash = hasher.hash("Secreta123");

        assertThat(hash).startsWith("$2a$").doesNotContain("Secreta123");
        assertThat(hasher.matches("Secreta123", hash)).isTrue();
        assertThat(hasher.matches("Otra12345", hash)).isFalse();
    }
}
