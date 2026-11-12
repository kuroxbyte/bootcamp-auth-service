package dev.bootcamp.authservice.domain.model.user;

import dev.bootcamp.authservice.domain.exception.BusinessRuleException;

/**
 * Usuario con el que un cliente inicia sesión. Guarda la contraseña solo cifrada.
 *
 * @param id           id del usuario; null antes de guardarlo
 * @param customerId   id del cliente al que pertenece; un cliente tiene un solo usuario
 * @param username     nombre de usuario, único
 * @param passwordHash contraseña cifrada
 */
public record UserAccount(String id, String customerId, String username, String passwordHash) {

    /**
     * Longitud mínima de la contraseña.
     */
    public static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * Longitud máxima de la contraseña (BCrypt solo usa los primeros 72 bytes).
     */
    public static final int MAX_PASSWORD_LENGTH = 72;

    /**
     * Valida la contraseña antes de cifrarla.
     *
     * @throws BusinessRuleException si falta o no tiene entre 8 y 72 caracteres
     */
    public static void validatePassword(String rawPassword) {
        if (rawPassword == null
                || rawPassword.length() < MIN_PASSWORD_LENGTH
                || rawPassword.length() > MAX_PASSWORD_LENGTH) {
            throw new BusinessRuleException(
                    "La contraseña debe tener entre " + MIN_PASSWORD_LENGTH + " y "
                            + MAX_PASSWORD_LENGTH + " caracteres");
        }
    }
}
