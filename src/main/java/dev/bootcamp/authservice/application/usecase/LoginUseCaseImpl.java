package dev.bootcamp.authservice.application.usecase;

import dev.bootcamp.authservice.application.port.PasswordHasher;
import dev.bootcamp.authservice.application.port.TokenIssuer;
import dev.bootcamp.authservice.application.port.UserAccountRepository;
import dev.bootcamp.authservice.domain.exception.InvalidCredentialsException;
import dev.bootcamp.authservice.domain.model.user.AccessToken;
import dev.bootcamp.authservice.domain.model.user.Role;
import io.reactivex.rxjava3.core.Single;

/**
 * Implementación de {@link LoginUseCase}.
 */
public class LoginUseCaseImpl implements LoginUseCase {
    private final UserAccountRepository userAccountRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    /**
     * Crea el caso de uso con los adaptadores de sus puertos.
     */
    public LoginUseCaseImpl(
            UserAccountRepository userAccountRepository,
            PasswordHasher passwordHasher,
            TokenIssuer tokenIssuer) {
        this.userAccountRepository = userAccountRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    public Single<AccessToken> execute(String username, String password) {
        return userAccountRepository
                .findByUsername(username)
                .filter(user -> passwordHasher.matches(password, user.passwordHash()))
                .switchIfEmpty(Single.error(InvalidCredentialsException::new))
                .map(user -> tokenIssuer.issue(user.customerId(), user.username(), Role.CUSTOMER));
    }
}
