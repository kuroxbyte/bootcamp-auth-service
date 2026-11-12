package dev.bootcamp.authservice.domain.exception;

/**
 * Usuario o contraseña incorrectos. Se traduce a una respuesta 401; el mensaje no dice cuál de los
 * dos falló.
 */
public class InvalidCredentialsException extends RuntimeException {
    /**
     * Crea la excepción con un mensaje genérico.
     */
    public InvalidCredentialsException() {
        super("Usuario o contraseña incorrectos");
    }
}
