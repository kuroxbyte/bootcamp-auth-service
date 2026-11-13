package dev.bootcamp.authservice.application.port;

import dev.bootcamp.authservice.domain.model.user.WalletUser;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * Puerto de persistencia de los accesos de los monederos.
 */
public interface WalletUserRepository {

    /**
     * Busca el acceso por el celular.
     *
     * @return el acceso, o vacío si no existe
     */
    Maybe<WalletUser> findByPhone(String phone);

    /**
     * Indica si el celular ya tiene un acceso.
     */
    Single<Boolean> existsByPhone(String phone);

    /**
     * Guarda el acceso y lo emite con su id asignado.
     */
    Single<WalletUser> save(WalletUser walletUser);
}
