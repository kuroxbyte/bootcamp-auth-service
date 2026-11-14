package dev.bootcamp.authservice.application.usecase;

import dev.bootcamp.authservice.domain.model.wallet.Wallet;
import io.reactivex.rxjava3.core.Completable;

/**
 * Caso de uso para quitar el acceso de un cliente o de un monedero dados de baja
 * ({@code CustomerClosed} de customer-service y {@code WalletClosed} de yanki-service).
 *
 * <p>Elimina el usuario y la copia local: ya no se puede iniciar sesión ni registrar un usuario
 * nuevo. Los tokens ya emitidos siguen válidos hasta que vencen. Es idempotente y reactivo: no hace
 * nada hasta que se suscribe al {@link Completable}.
 */
public interface CloseAccessUseCase {

    /**
     * Quita el acceso del cliente dado de baja.
     */
    Completable closeCustomer(String customerId);

    /**
     * Quita el acceso del monedero dado de baja.
     */
    Completable closeWallet(Wallet wallet);
}
