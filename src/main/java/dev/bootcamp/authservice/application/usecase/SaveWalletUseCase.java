package dev.bootcamp.authservice.application.usecase;

import dev.bootcamp.authservice.domain.model.wallet.Wallet;
import io.reactivex.rxjava3.core.Completable;

/**
 * Caso de uso para guardar en la copia local un monedero creado en yanki-service (evento
 * {@code WalletCreated}).
 *
 * <p>Es idempotente: procesar dos veces el mismo evento deja un solo monedero. Es reactivo: no
 * hace nada hasta que se suscribe al {@link Completable}.
 */
public interface SaveWalletUseCase {

    /**
     * Guarda o reemplaza el monedero.
     */
    Completable execute(Wallet wallet);
}
