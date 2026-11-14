package dev.bootcamp.authservice.application.usecase;

import dev.bootcamp.authservice.domain.model.wallet.Wallet;
import io.reactivex.rxjava3.core.Completable;

/**
 * Caso de uso para guardar en la copia local un monedero creado en yanki-service (evento
 * {@code WalletCreated}).
 *
 * <p>Si el celular era de un monedero dado de baja cuyo {@code WalletClosed} todavía no llegó (los
 * eventos de monederos distintos pueden llegar en otro orden), primero quita la copia y el acceso
 * de ese monedero. Es idempotente: procesar dos veces el mismo evento deja un solo monedero. Es
 * reactivo: no hace nada hasta que se suscribe al {@link Completable}.
 */
public interface SaveWalletUseCase {

    /**
     * Guarda o reemplaza el monedero.
     */
    Completable execute(Wallet wallet);
}
