package dev.bootcamp.authservice.application.usecase;

import dev.bootcamp.authservice.application.port.CustomerRepository;
import dev.bootcamp.authservice.application.port.PasswordHasher;
import dev.bootcamp.authservice.application.port.UserAccountRepository;
import dev.bootcamp.authservice.domain.exception.BusinessRuleException;
import dev.bootcamp.authservice.domain.model.customer.Customer;
import dev.bootcamp.authservice.domain.model.user.UserAccount;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseImplTest {

    private static final Customer CUSTOMER = new Customer("c1", "PERSONAL", "STANDARD");

    @Mock private CustomerRepository customerRepository;

    @Mock private UserAccountRepository userAccountRepository;

    @Mock private PasswordHasher passwordHasher;

    @InjectMocks private RegisterUserUseCaseImpl useCase;

    @Test
    void registersUserWithHashedPassword() {
        when(customerRepository.findById("c1")).thenReturn(Maybe.just(CUSTOMER));
        when(userAccountRepository.existsByCustomerId("c1")).thenReturn(Single.just(false));
        when(userAccountRepository.existsByUsername("ana")).thenReturn(Single.just(false));
        when(passwordHasher.hash("Secreta123")).thenReturn("hash");
        when(userAccountRepository.save(any())).thenAnswer(inv -> Single.just(inv.getArgument(0)));

        useCase.execute("c1", "ana", "Secreta123").test()
                .assertValue(new UserAccount(null, "c1", "ana", "hash"));
    }

    @Test
    void rejectsCustomerNotYetKnown() {
        when(customerRepository.findById("c9")).thenReturn(Maybe.empty());

        useCase.execute("c9", "ana", "Secreta123").test()
                .assertError(error -> error instanceof BusinessRuleException
                        && error.getMessage()
                                .equals("El cliente no existe o todavía no está disponible"));
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void rejectsSecondUserForTheSameCustomer() {
        when(customerRepository.findById("c1")).thenReturn(Maybe.just(CUSTOMER));
        when(userAccountRepository.existsByCustomerId("c1")).thenReturn(Single.just(true));

        useCase.execute("c1", "otro", "Secreta123").test()
                .assertError(BusinessRuleException.class);
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void rejectsUsernameInUse() {
        when(customerRepository.findById("c1")).thenReturn(Maybe.just(CUSTOMER));
        when(userAccountRepository.existsByCustomerId("c1")).thenReturn(Single.just(false));
        when(userAccountRepository.existsByUsername("ana")).thenReturn(Single.just(true));

        useCase.execute("c1", "ana", "Secreta123").test()
                .assertError(BusinessRuleException.class);
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void rejectsShortPasswordBeforeQueryingAnything() {
        useCase.execute("c1", "ana", "corta").test().assertError(BusinessRuleException.class);

        verifyNoInteractions(customerRepository, userAccountRepository, passwordHasher);
    }
}
