package dev.bootcamp.authservice.application.usecase;

import dev.bootcamp.authservice.application.port.PasswordHasher;
import dev.bootcamp.authservice.application.port.TokenIssuer;
import dev.bootcamp.authservice.application.port.WalletRepository;
import dev.bootcamp.authservice.application.port.WalletUserRepository;
import dev.bootcamp.authservice.domain.exception.BusinessRuleException;
import dev.bootcamp.authservice.domain.exception.InvalidCredentialsException;
import dev.bootcamp.authservice.domain.model.user.AccessToken;
import dev.bootcamp.authservice.domain.model.user.Role;
import dev.bootcamp.authservice.domain.model.user.WalletUser;
import dev.bootcamp.authservice.domain.model.wallet.Wallet;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletUserUseCasesTest {

    private static final String PHONE = "987654321";
    private static final WalletUser USER = new WalletUser("u1", "w1", PHONE, "hash");

    @Mock private WalletRepository walletRepository;

    @Mock private WalletUserRepository walletUserRepository;

    @Mock private PasswordHasher passwordHasher;

    @Mock private TokenIssuer tokenIssuer;

    private RegisterWalletUserUseCaseImpl register;

    private WalletLoginUseCaseImpl login;

    @BeforeEach
    void createUseCases() {
        register = new RegisterWalletUserUseCaseImpl(
                walletRepository,
                walletUserRepository,
                passwordHasher);
        login = new WalletLoginUseCaseImpl(walletUserRepository, passwordHasher, tokenIssuer);
    }

    @Test
    void savesWalletAfterRemovingTheClosedWalletWithTheSamePhone() {
        Wallet wallet = new Wallet("w2", PHONE);
        when(walletUserRepository.deleteByPhoneAndOtherWallet(PHONE, "w2"))
                .thenReturn(Completable.complete());
        when(walletRepository.deleteOtherWithPhone(wallet)).thenReturn(Completable.complete());
        when(walletRepository.save(wallet)).thenReturn(Completable.complete());

        new SaveWalletUseCaseImpl(walletRepository, walletUserRepository).execute(wallet).test()
                .assertComplete();

        InOrder order = inOrder(walletUserRepository, walletRepository);
        order.verify(walletUserRepository).deleteByPhoneAndOtherWallet(PHONE, "w2");
        order.verify(walletRepository).deleteOtherWithPhone(wallet);
        order.verify(walletRepository).save(wallet);
    }

    @Test
    void registersAccessOfKnownWalletWithHashedPassword() {
        when(walletRepository.findByPhone(PHONE)).thenReturn(Maybe.just(new Wallet("w1", PHONE)));
        when(walletUserRepository.existsByPhone(PHONE)).thenReturn(Single.just(false));
        when(passwordHasher.hash("Secreta123")).thenReturn("hash");
        when(walletUserRepository.save(new WalletUser(null, "w1", PHONE, "hash")))
                .thenReturn(Single.just(USER));

        register.execute(PHONE, "Secreta123").test().assertValue(USER);
    }

    @Test
    void rejectsUnknownWalletRegisteredWalletAndShortPassword() {
        when(walletRepository.findByPhone(PHONE))
                .thenReturn(Maybe.empty())
                .thenReturn(Maybe.just(new Wallet("w1", PHONE)));
        when(walletUserRepository.existsByPhone(PHONE)).thenReturn(Single.just(true));

        register.execute(PHONE, "Secreta123").test().assertError(BusinessRuleException.class);
        register.execute(PHONE, "Secreta123").test().assertError(BusinessRuleException.class);
        register.execute(PHONE, "corta").test().assertError(BusinessRuleException.class);
        verify(walletUserRepository, never()).save(any());
    }

    @Test
    void issuesWalletTokenForValidCredentials() {
        AccessToken token = new AccessToken("jwt", 3600);
        when(walletUserRepository.findByPhone(PHONE)).thenReturn(Maybe.just(USER));
        when(passwordHasher.matches("Secreta123", "hash")).thenReturn(true);
        when(tokenIssuer.issue("w1", PHONE, Role.WALLET)).thenReturn(token);

        login.execute(PHONE, "Secreta123").test().assertValue(token);
    }

    @Test
    void rejectsWrongPasswordOrUnknownPhone() {
        when(walletUserRepository.findByPhone(PHONE)).thenReturn(Maybe.just(USER));
        when(walletUserRepository.findByPhone("900000000")).thenReturn(Maybe.empty());
        when(passwordHasher.matches("mala", "hash")).thenReturn(false);

        login.execute(PHONE, "mala").test().assertError(InvalidCredentialsException.class);
        login.execute("900000000", "x").test().assertError(InvalidCredentialsException.class);
        verifyNoInteractions(tokenIssuer);
    }
}
