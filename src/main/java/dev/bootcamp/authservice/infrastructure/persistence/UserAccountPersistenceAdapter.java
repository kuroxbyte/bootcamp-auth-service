package dev.bootcamp.authservice.infrastructure.persistence;

import dev.bootcamp.authservice.application.port.UserAccountRepository;
import dev.bootcamp.authservice.domain.model.user.UserAccount;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Implementa el puerto {@link UserAccountRepository} con MongoDB reactivo.
 */
@Component
@RequiredArgsConstructor
public class UserAccountPersistenceAdapter implements UserAccountRepository {

    private final UserAccountMongoRepository mongoRepository;

    @Override
    public Maybe<UserAccount> findByUsername(String username) {
        return Maybe.fromPublisher(mongoRepository.findByUsername(username))
                .map(UserAccountPersistenceAdapter::toDomain);
    }

    @Override
    public Single<Boolean> existsByUsername(String username) {
        return Single.fromPublisher(mongoRepository.existsByUsername(username));
    }

    @Override
    public Single<Boolean> existsByCustomerId(String customerId) {
        return Single.fromPublisher(mongoRepository.existsByCustomerId(customerId));
    }

    @Override
    public Single<UserAccount> save(UserAccount userAccount) {
        return Single.fromPublisher(mongoRepository.save(new UserAccountDocument(
                        userAccount.id(),
                        userAccount.customerId(),
                        userAccount.username(),
                        userAccount.passwordHash())))
                .map(UserAccountPersistenceAdapter::toDomain);
    }

    private static UserAccount toDomain(UserAccountDocument document) {
        return new UserAccount(
                document.getId(),
                document.getCustomerId(),
                document.getUsername(),
                document.getPasswordHash());
    }
}
