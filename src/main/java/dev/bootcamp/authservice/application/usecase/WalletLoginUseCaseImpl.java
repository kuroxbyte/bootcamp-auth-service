package dev.bootcamp.authservice.application.usecase;

import dev.bootcamp.authservice.application.port.PasswordHasher;
import dev.bootcamp.authservice.application.port.TokenIssuer;
import dev.bootcamp.authservice.application.port.WalletUserRepository;
import dev.bootcamp.authservice.domain.exception.InvalidCredentialsException;
import dev.bootcamp.authservice.domain.model.user.AccessToken;
import dev.bootcamp.authservice.domain.model.user.Role;
import io.reactivex.rxjava3.core.Single;

/**
 * Implementación de {@link WalletLoginUseCase}.
 */
public class WalletLoginUseCaseImpl implements WalletLoginUseCase {
    private final WalletUserRepository walletUserRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    /**
     * Crea el caso de uso con los adaptadores de sus puertos.
     */
    public WalletLoginUseCaseImpl(
            WalletUserRepository walletUserRepository,
            PasswordHasher passwordHasher,
            TokenIssuer tokenIssuer) {
        this.walletUserRepository = walletUserRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    public Single<AccessToken> execute(String phone, String password) {
        return walletUserRepository
                .findByPhone(phone)
                .filter(user -> passwordHasher.matches(password, user.passwordHash()))
                .switchIfEmpty(Single.error(InvalidCredentialsException::new))
                .map(user -> tokenIssuer.issue(user.walletId(), user.phone(), Role.WALLET));
    }
}
