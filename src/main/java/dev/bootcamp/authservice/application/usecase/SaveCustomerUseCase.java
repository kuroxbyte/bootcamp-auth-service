package dev.bootcamp.authservice.application.usecase;

import dev.bootcamp.authservice.domain.model.customer.Customer;
import io.reactivex.rxjava3.core.Completable;

/**
 * Caso de uso para guardar en la copia local un cliente creado en customer-service (evento
 * {@code CustomerCreated}).
 *
 * <p>Es idempotente: procesar dos veces el mismo evento deja un solo cliente. Es reactivo: no
 * hace nada hasta que se suscribe al {@link Completable}.
 */
public interface SaveCustomerUseCase {

    /**
     * Guarda o reemplaza el cliente.
     */
    Completable execute(Customer customer);
}
