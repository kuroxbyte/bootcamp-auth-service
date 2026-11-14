package dev.bootcamp.authservice.application.port;

import dev.bootcamp.authservice.domain.model.customer.Customer;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;

/**
 * Copia local de los clientes del banco (datos maestros), con caché.
 */
public interface CustomerRepository {

    /**
     * Guarda o reemplaza el cliente; guardar dos veces el mismo evento no duplica nada.
     */
    Completable save(Customer customer);

    /**
     * Busca el cliente por su id.
     *
     * @return el cliente, o vacío si auth-service todavía no lo conoce
     */
    Maybe<Customer> findById(String customerId);

    /**
     * Quita el cliente de la copia local (y de la caché); no falla si no existe.
     */
    Completable deleteById(String customerId);
}
