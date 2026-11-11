package dev.bootcamp.authservice.application.port;

/**
 * Cifrado de contraseñas.
 */
public interface PasswordHasher {

    /**
     * Cifra la contraseña.
     */
    String hash(String rawPassword);

    /**
     * Indica si la contraseña corresponde al cifrado guardado.
     */
    boolean matches(String rawPassword, String passwordHash);
}
