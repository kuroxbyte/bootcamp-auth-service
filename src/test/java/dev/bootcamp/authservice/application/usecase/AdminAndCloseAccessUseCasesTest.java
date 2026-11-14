package dev.bootcamp.authservice.application.usecase;

import dev.bootcamp.authservice.application.port.CustomerRepository;
import dev.bootcamp.authservice.application.port.PasswordHasher;
import dev.bootcamp.authservice.application.port.TokenIssuer;
import dev.bootcamp.authservice.application.port.UserAccountRepository;
import dev.bootcamp.authservice.application.port.WalletRepository;
import dev.bootcamp.authservice.application.port.WalletUserRepository;
import dev.bootcamp.authservice.domain.exception.InvalidCredentialsException;
import dev.bootcamp.authservice.domain.model.user.AccessToken;
import dev.bootcamp.authservice.domain.model.user.Role;
import dev.bootcamp.authservice.domain.model.wallet.Wallet;
import io.reactivex.rxjava3.core.Completable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAndCloseAccessUseCasesTest {

    private static final String HASH = "$2a$10$hash";

    @Mock private PasswordHasher passwordHasher;

    @Mock private TokenIssuer tokenIssuer;

    @Mock private CustomerRepository customerRepository;

    @Mock private UserAccountRepository userAccountRepository;

    @Mock private WalletRepository walletRepository;

    @Mock private WalletUserRepository walletUserRepository;

    private AdminLoginUseCaseImpl adminLogin() {
        return new AdminLoginUseCaseImpl("admin", HASH, passwordHasher, tokenIssuer);
    }

    @Test
    void issuesAdminTokenForTheConfiguredCredentials() {
        AccessToken token = new AccessToken("jwt", 3600);
        when(passwordHasher.matches("Admin2026!", HASH)).thenReturn(true);
        when(tokenIssuer.issue("admin", "admin", Role.ADMIN)).thenReturn(token);

        adminLogin().execute("admin", "Admin2026!").test().assertValue(token);
    }

    @Test
    void rejectsWrongPasswordOrUnknownUser() {
        when(passwordHasher.matches("otra", HASH)).thenReturn(false);

        adminLogin().execute("admin", "otra").test()
                .assertError(InvalidCredentialsException.class);
        adminLogin().execute("root", "Admin2026!").test()
                .assertError(InvalidCredentialsException.class);
        verify(tokenIssuer, never()).issue(anyString(), anyString(), any());
    }

    @Test
    void closingCustomerRemovesItsUserAndTheLocalCopy() {
        when(userAccountRepository.deleteByCustomerId("c1")).thenReturn(Completable.complete());
        when(customerRepository.deleteById("c1")).thenReturn(Completable.complete());

        closeAccess().closeCustomer("c1").test().assertComplete();

        InOrder order = inOrder(userAccountRepository, customerRepository);
        order.verify(userAccountRepository).deleteByCustomerId("c1");
        order.verify(customerRepository).deleteById("c1");
    }

    @Test
    void closingWalletRemovesItsAccessAndTheLocalCopy() {
        Wallet wallet = new Wallet("w1", "987654321");
        when(walletUserRepository.deleteByWalletId("w1")).thenReturn(Completable.complete());
        when(walletRepository.delete(wallet)).thenReturn(Completable.complete());

        closeAccess().closeWallet(wallet).test().assertComplete();

        verify(walletRepository).delete(wallet);
    }

    private CloseAccessUseCaseImpl closeAccess() {
        return new CloseAccessUseCaseImpl(
                customerRepository,
                userAccountRepository,
                walletRepository,
                walletUserRepository);
    }
}
