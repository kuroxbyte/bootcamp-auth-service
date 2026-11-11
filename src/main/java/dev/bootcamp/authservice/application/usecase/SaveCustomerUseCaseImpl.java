package dev.bootcamp.authservice.application.usecase;

import dev.bootcamp.authservice.application.port.CustomerRepository;
import dev.bootcamp.authservice.domain.model.customer.Customer;
import io.reactivex.rxjava3.core.Completable;

/**
 * Implementación de {@link SaveCustomerUseCase}.
 */
public class SaveCustomerUseCaseImpl implements SaveCustomerUseCase {
    private final CustomerRepository customerRepository;

    /**
     * Crea el caso de uso con el adaptador de su puerto.
     */
    public SaveCustomerUseCaseImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Completable execute(Customer customer) {
        return customerRepository.save(customer);
    }
}
