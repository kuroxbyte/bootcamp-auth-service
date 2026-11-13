package dev.bootcamp.authservice.application.usecase;

import dev.bootcamp.authservice.application.port.CustomerRepository;
import dev.bootcamp.authservice.domain.model.customer.Customer;
import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;

/**
 * Implementación de {@link SaveCustomerUseCase}.
 */
@RequiredArgsConstructor
public class SaveCustomerUseCaseImpl implements SaveCustomerUseCase {
    private final CustomerRepository customerRepository;

    @Override
    public Completable execute(Customer customer) {
        return customerRepository.save(customer);
    }
}
