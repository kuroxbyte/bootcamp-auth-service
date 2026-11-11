package dev.bootcamp.authservice.application.usecase;

import dev.bootcamp.authservice.application.port.PasswordHasher;
import dev.bootcamp.authservice.application.port.TokenIssuer;
import dev.bootcamp.authservice.application.port.UserAccountRepository;
import dev.bootcamp.authservice.domain.exception.InvalidCredentialsException;
import dev.bootcamp.authservice.domain.model.user.AccessToken;
import dev.bootcamp.authservice.domain.model.user.UserAccount;
import io.reactivex.rxjava3.core.Maybe;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseImplTest {

    private static final UserAccount USER = new UserAccount("u1", "c1", "ana", "hash");

    @Mock private UserAccountRepository userAccountRepository;

    @Mock private PasswordHasher passwordHasher;

    @Mock private TokenIssuer tokenIssuer;

    @InjectMocks private LoginUseCaseImpl useCase;

    @Test
    void issuesTokenForValidCredentials() {
        AccessToken token = new AccessToken("jwt", 3600);
        when(userAccountRepository.findByUsername("ana")).thenReturn(Maybe.just(USER));
        when(passwordHasher.matches("Secreta123", "hash")).thenReturn(true);
        when(tokenIssuer.issue(USER)).thenReturn(token);

        useCase.execute("ana", "Secreta123").test().assertValue(token);
    }

    @Test
    void rejectsWrongPassword() {
        when(userAccountRepository.findByUsername("ana")).thenReturn(Maybe.just(USER));
        when(passwordHasher.matches("mala", "hash")).thenReturn(false);

        useCase.execute("ana", "mala").test().assertError(InvalidCredentialsException.class);
        verify(tokenIssuer, never()).issue(any());
    }

    @Test
    void rejectsUnknownUserWithTheSameError() {
        when(userAccountRepository.findByUsername("nadie")).thenReturn(Maybe.empty());

        useCase.execute("nadie", "Secreta123").test()
                .assertError(error -> error instanceof InvalidCredentialsException
                        && error.getMessage().equals("Usuario o contraseña incorrectos"));
    }
}
