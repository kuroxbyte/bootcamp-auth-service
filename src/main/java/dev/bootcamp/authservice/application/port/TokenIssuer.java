package dev.bootcamp.authservice.application.port;

import dev.bootcamp.authservice.domain.model.user.AccessToken;
import dev.bootcamp.authservice.domain.model.user.UserAccount;

/**
 * Emisión de tokens de acceso.
 */
public interface TokenIssuer {

    /**
     * Emite un token para el usuario; su claim {@code sub} es el id del cliente.
     */
    AccessToken issue(UserAccount userAccount);
}
