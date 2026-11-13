package dev.bootcamp.authservice.application.usecase;

import dev.bootcamp.authservice.application.port.CustomerRepository;
import dev.bootcamp.authservice.application.port.PasswordHasher;
import dev.bootcamp.authservice.application.port.UserAccountRepository;
import dev.bootcamp.authservice.domain.exception.BusinessRuleException;
import dev.bootcamp.authservice.domain.model.user.UserAccount;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

/**
 * Implementación de {@link RegisterUserUseCase}.
 */
@RequiredArgsConstructor
public class RegisterUserUseCaseImpl implements RegisterUserUseCase {
    private final CustomerRepository customerRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordHasher passwordHasher;

    @Override
    public Single<UserAccount> execute(String customerId, String username, String password) {
        return Completable.fromAction(() -> UserAccount.validatePassword(password))
                .andThen(Maybe.defer(() -> customerRepository.findById(customerId)))
                .switchIfEmpty(Single.error(() -> new BusinessRuleException(
                        "El cliente no existe o todavía no está disponible")))
                .flatMap(customer -> rejectIf(
                        userAccountRepository.existsByCustomerId(customerId),
                        "El cliente ya tiene un usuario"))
                .flatMap(ignored -> rejectIf(
                        userAccountRepository.existsByUsername(username),
                        "El nombre de usuario ya está en uso"))
                .flatMap(ignored -> userAccountRepository.save(new UserAccount(
                        null,
                        customerId,
                        username,
                        passwordHasher.hash(password))));
    }

    private static Single<Boolean> rejectIf(Single<Boolean> condition, String message) {
        return condition.flatMap(exists -> exists
                ? Single.error(new BusinessRuleException(message))
                : Single.just(false));
    }
}
