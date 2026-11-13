package dev.bootcamp.authservice.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * Repositorio de Spring Data para {@link WalletUserDocument}.
 */
public interface WalletUserMongoRepository
        extends ReactiveMongoRepository<WalletUserDocument, String> {

    /**
     * Busca el acceso por el celular.
     */
    Mono<WalletUserDocument> findByPhone(String phone);

    /**
     * Indica si el celular ya tiene un acceso.
     */
    Mono<Boolean> existsByPhone(String phone);
}
