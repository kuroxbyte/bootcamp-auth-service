package dev.bootcamp.authservice.application.usecase;

import dev.bootcamp.authservice.application.port.PasswordHasher;
import dev.bootcamp.authservice.application.port.WalletRepository;
import dev.bootcamp.authservice.application.port.WalletUserRepository;
import dev.bootcamp.authservice.domain.exception.BusinessRuleException;
import dev.bootcamp.authservice.domain.model.user.UserAccount;
import dev.bootcamp.authservice.domain.model.user.WalletUser;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * Implementación de {@link RegisterWalletUserUseCase}.
 */
public class RegisterWalletUserUseCaseImpl implements RegisterWalletUserUseCase {
    private final WalletRepository walletRepository;
    private final WalletUserRepository walletUserRepository;
    private final PasswordHasher passwordHasher;

    /**
     * Crea el caso de uso con los adaptadores de sus puertos.
     */
    public RegisterWalletUserUseCaseImpl(
            WalletRepository walletRepository,
            WalletUserRepository walletUserRepository,
            PasswordHasher passwordHasher) {
        this.walletRepository = walletRepository;
        this.walletUserRepository = walletUserRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public Single<WalletUser> execute(String phone, String password) {
        return Completable.fromAction(() -> UserAccount.validatePassword(password))
                .andThen(Maybe.defer(() -> walletRepository.findByPhone(phone)))
                .switchIfEmpty(Single.error(() -> new BusinessRuleException(
                        "El celular no tiene monedero Yanki o todavía no está disponible")))
                .flatMap(wallet -> walletUserRepository.existsByPhone(phone)
                        .flatMap(exists -> exists
                                ? Single.error(new BusinessRuleException(
                                        "El monedero ya tiene un acceso registrado"))
                                : walletUserRepository.save(new WalletUser(
                                        null,
                                        wallet.id(),
                                        phone,
                                        passwordHasher.hash(password)))));
    }
}
