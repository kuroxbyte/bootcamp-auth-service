package dev.bootcamp.authservice.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * Repositorio de Spring Data para {@link UserAccountDocument}.
 */
public interface UserAccountMongoRepository
        extends ReactiveMongoRepository<UserAccountDocument, String> {

    /**
     * Busca el usuario por su nombre.
     */
    Mono<UserAccountDocument> findByUsername(String username);

    /**
     * Indica si existe un usuario con ese nombre.
     */
    Mono<Boolean> existsByUsername(String username);

    /**
     * Indica si el cliente ya tiene un usuario.
     */
    Mono<Boolean> existsByCustomerId(String customerId);
}
