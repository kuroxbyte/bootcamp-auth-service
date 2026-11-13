package dev.bootcamp.authservice.application.usecase;

import dev.bootcamp.authservice.application.port.PasswordHasher;
import dev.bootcamp.authservice.application.port.TokenIssuer;
import dev.bootcamp.authservice.application.port.UserAccountRepository;
import dev.bootcamp.authservice.domain.exception.InvalidCredentialsException;
import dev.bootcamp.authservice.domain.model.user.AccessToken;
import dev.bootcamp.authservice.domain.model.user.Role;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

/**
 * Implementación de {@link LoginUseCase}.
 */
@RequiredArgsConstructor
public class LoginUseCaseImpl implements LoginUseCase {
    private final UserAccountRepository userAccountRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    @Override
    public Single<AccessToken> execute(String username, String password) {
        return userAccountRepository
                .findByUsername(username)
                .filter(user -> passwordHasher.matches(password, user.passwordHash()))
                .switchIfEmpty(Single.error(InvalidCredentialsException::new))
                .map(user -> tokenIssuer.issue(user.customerId(), user.username(), Role.CUSTOMER));
    }
}
