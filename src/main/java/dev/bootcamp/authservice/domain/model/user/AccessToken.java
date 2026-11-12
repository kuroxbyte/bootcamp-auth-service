package dev.bootcamp.authservice.domain.model.user;

/**
 * Token de acceso emitido al iniciar sesión.
 *
 * @param value     JWT firmado
 * @param expiresIn segundos de validez
 */
public record AccessToken(String value, long expiresIn) {}
