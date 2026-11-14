package dev.bootcamp.authservice.application.usecase;

import dev.bootcamp.authservice.domain.exception.InvalidCredentialsException;
import dev.bootcamp.authservice.domain.model.user.AccessToken;
import io.reactivex.rxjava3.core.Single;

/**
 * Caso de uso para que un empleado del banco inicie sesión.
 *
 * <p>Si las credenciales coinciden con las del administrador configurado emite un token JWT con
 * el rol {@code ADMIN}, cuyo claim {@code sub} es el nombre de usuario. Es reactivo: no hace nada
 * hasta que se suscribe al {@link Single}.
 */
public interface AdminLoginUseCase {

    /**
     * Verifica las credenciales y emite el token.
     *
     * @return el token de acceso; error {@link InvalidCredentialsException} si el usuario o la
     *     contraseña no corresponden (sin decir cuál de los dos)
     */
    Single<AccessToken> execute(String username, String password);
}
