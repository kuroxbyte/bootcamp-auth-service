package dev.bootcamp.authservice.application.port;

import dev.bootcamp.authservice.domain.model.user.AccessToken;
import dev.bootcamp.authservice.domain.model.user.Role;

/**
 * Emisión de tokens de acceso.
 */
public interface TokenIssuer {

    /**
     * Emite un token.
     *
     * @param subject  claim {@code sub}: id del cliente o del monedero, o usuario del empleado
     * @param username claim {@code preferred_username}: nombre de usuario o celular
     * @param role     rol del claim {@code roles}
     */
    AccessToken issue(String subject, String username, Role role);
}
