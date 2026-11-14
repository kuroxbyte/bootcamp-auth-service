package dev.bootcamp.authservice.application.port;

import dev.bootcamp.authservice.domain.model.wallet.Wallet;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;

/**
 * Copia local de los monederos Yanki (datos maestros), con caché.
 */
public interface WalletRepository {

    /**
     * Guarda o reemplaza el monedero; guardar dos veces el mismo evento no duplica nada.
     */
    Completable save(Wallet wallet);

    /**
     * Busca el monedero por su celular.
     *
     * @return el monedero, o vacío si auth-service todavía no lo conoce
     */
    Maybe<Wallet> findByPhone(String phone);

    /**
     * Quita el monedero de la copia local (y de la caché); no falla si no existe.
     */
    Completable delete(Wallet wallet);
}
