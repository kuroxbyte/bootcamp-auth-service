package dev.bootcamp.authservice.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * Repositorio de Spring Data para {@link WalletDocument}.
 */
public interface WalletMongoRepository extends ReactiveMongoRepository<WalletDocument, String> {

    /**
     * Busca el monedero por su celular.
     */
    Mono<WalletDocument> findByPhone(String phone);

    /**
     * Elimina los monederos con ese celular y otro id.
     */
    Mono<Void> deleteByPhoneAndIdNot(String phone, String id);
}
