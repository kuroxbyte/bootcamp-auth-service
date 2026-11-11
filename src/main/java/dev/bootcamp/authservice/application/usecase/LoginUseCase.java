package dev.bootcamp.authservice.application.usecase;

import dev.bootcamp.authservice.domain.exception.InvalidCredentialsException;
import dev.bootcamp.authservice.domain.model.user.AccessToken;
import io.reactivex.rxjava3.core.Single;

/**
 * Caso de uso para iniciar sesión con usuario y contraseña.
 *
 * <p>Si las credenciales son correctas emite un token JWT cuyo claim {@code sub} es el id del
 * cliente. Es reactivo: no hace nada hasta que se suscribe al {@link Single}.
 */
public interface LoginUseCase {

    /**
     * Verifica las credenciales y emite el token.
     *
     * @return el token de acceso; error {@link InvalidCredentialsException} si el usuario no
     *     existe o la contraseña no corresponde (sin decir cuál de los dos)
     */
    Single<AccessToken> execute(String username, String password);
}
