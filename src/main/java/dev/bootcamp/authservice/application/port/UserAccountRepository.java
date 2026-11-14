package dev.bootcamp.authservice.application.port;

import dev.bootcamp.authservice.domain.model.user.UserAccount;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * Puerto de persistencia de los usuarios.
 */
public interface UserAccountRepository {

    /**
     * Busca el usuario por su nombre.
     *
     * @return el usuario, o vacío si no existe
     */
    Maybe<UserAccount> findByUsername(String username);

    /**
     * Indica si ya existe un usuario con ese nombre.
     */
    Single<Boolean> existsByUsername(String username);

    /**
     * Indica si el cliente ya tiene un usuario.
     */
    Single<Boolean> existsByCustomerId(String customerId);

    /**
     * Guarda el usuario y lo emite con su id asignado.
     */
    Single<UserAccount> save(UserAccount userAccount);

    /**
     * Elimina el usuario del cliente; no falla si no tiene.
     */
    Completable deleteByCustomerId(String customerId);
}
