package dev.bootcamp.authservice.infrastructure.persistence;

import dev.bootcamp.authservice.application.port.WalletUserRepository;
import dev.bootcamp.authservice.domain.model.user.WalletUser;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Implementa el puerto {@link WalletUserRepository} con MongoDB reactivo.
 */
@Component
@RequiredArgsConstructor
public class WalletUserPersistenceAdapter implements WalletUserRepository {

    private final WalletUserMongoRepository mongoRepository;

    @Override
    public Maybe<WalletUser> findByPhone(String phone) {
        return Maybe.fromPublisher(mongoRepository.findByPhone(phone))
                .map(WalletUserPersistenceAdapter::toDomain);
    }

    @Override
    public Single<Boolean> existsByPhone(String phone) {
        return Single.fromPublisher(mongoRepository.existsByPhone(phone));
    }

    @Override
    public Single<WalletUser> save(WalletUser walletUser) {
        return Single.fromPublisher(mongoRepository.save(new WalletUserDocument(
                        walletUser.id(),
                        walletUser.walletId(),
                        walletUser.phone(),
                        walletUser.passwordHash())))
                .map(WalletUserPersistenceAdapter::toDomain);
    }

    private static WalletUser toDomain(WalletUserDocument document) {
        return new WalletUser(
                document.getId(),
                document.getWalletId(),
                document.getPhone(),
                document.getPasswordHash());
    }
}
