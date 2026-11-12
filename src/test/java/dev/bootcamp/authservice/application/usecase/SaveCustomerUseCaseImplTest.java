package dev.bootcamp.authservice.application.usecase;

import dev.bootcamp.authservice.application.port.CustomerRepository;
import dev.bootcamp.authservice.domain.model.customer.Customer;
import io.reactivex.rxjava3.core.Completable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaveCustomerUseCaseImplTest {

    @Mock private CustomerRepository customerRepository;

    @InjectMocks private SaveCustomerUseCaseImpl useCase;

    @Test
    void savesTheCustomer() {
        Customer customer = new Customer("c1", "BUSINESS", "PYME");
        when(customerRepository.save(customer)).thenReturn(Completable.complete());

        useCase.execute(customer).test().assertComplete();

        verify(customerRepository).save(customer);
    }
}
