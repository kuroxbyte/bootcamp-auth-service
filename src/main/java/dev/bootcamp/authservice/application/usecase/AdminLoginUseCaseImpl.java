package dev.bootcamp.authservice.application.usecase;

import dev.bootcamp.authservice.application.port.PasswordHasher;
import dev.bootcamp.authservice.application.port.TokenIssuer;
import dev.bootcamp.authservice.domain.exception.InvalidCredentialsException;
import dev.bootcamp.authservice.domain.model.user.AccessToken;
import dev.bootcamp.authservice.domain.model.user.Role;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

/**
 * Implementación de {@link AdminLoginUseCase}: compara con el usuario y el cifrado BCrypt de la
 * contraseña del administrador, que vienen de la configuración.
 */
@RequiredArgsConstructor
public class AdminLoginUseCaseImpl implements AdminLoginUseCase {
    private final String adminUsername;
    private final String adminPasswordHash;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    @Override
    public Single<AccessToken> execute(String username, String password) {
        return Single.fromCallable(() -> {
            if (!adminUsername.equals(username)
                    || !passwordHasher.matches(password, adminPasswordHash)) {
                throw new InvalidCredentialsException();
            }
            return tokenIssuer.issue(username, username, Role.ADMIN);
        });
    }
}
