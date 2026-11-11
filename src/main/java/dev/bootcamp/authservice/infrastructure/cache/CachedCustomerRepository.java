package dev.bootcamp.authservice.infrastructure.cache;

import dev.bootcamp.authservice.application.port.CustomerRepository;
import dev.bootcamp.authservice.domain.model.customer.Customer;
import dev.bootcamp.authservice.infrastructure.persistence.CustomerDocument;
import dev.bootcamp.authservice.infrastructure.persistence.CustomerMongoRepository;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

/**
 * Implementa el puerto {@link CustomerRepository} con MongoDB como almacén y Redis como caché
 * (cache-aside).
 *
 * <ul>
 *   <li>Guardar: escribe en MongoDB y después en Redis.
 *   <li>Buscar: lee de Redis; si no está, lee de MongoDB y lo deja en Redis.
 *   <li>Cada entrada vence según {@code cache.customers.ttl}.
 *   <li>Si Redis falla, se registra y se sigue con MongoDB: la caché solo acelera el acceso.
 * </ul>
 */
@Slf4j
@Component
public class CachedCustomerRepository implements CustomerRepository {

    private static final String KEY_PREFIX = "customer:";

    private final CustomerMongoRepository mongoRepository;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper;
    private final Duration ttl;

    /**
     * Crea el repositorio con el tiempo de vida configurado para la caché.
     */
    public CachedCustomerRepository(
            CustomerMongoRepository mongoRepository,
            ReactiveStringRedisTemplate redisTemplate,
            JsonMapper jsonMapper,
            @Value("${cache.customers.ttl}") Duration ttl) {
        this.mongoRepository = mongoRepository;
        this.redisTemplate = redisTemplate;
        this.jsonMapper = jsonMapper;
        this.ttl = ttl;
    }

    @Override
    public Completable save(Customer customer) {
        return Completable.fromPublisher(mongoRepository
                .save(new CustomerDocument(customer.id(), customer.type(), customer.profile()))
                .then(cache(customer)));
    }

    @Override
    public Maybe<Customer> findById(String customerId) {
        Mono<Customer> fromDatabase = mongoRepository.findById(customerId)
                .map(document -> new Customer(
                        document.getId(),
                        document.getType(),
                        document.getProfile()))
                .flatMap(customer -> cache(customer).thenReturn(customer));
        return Maybe.fromPublisher(cached(customerId).switchIfEmpty(fromDatabase));
    }

    private Mono<Customer> cached(String customerId) {
        return redisTemplate.opsForValue()
                .get(KEY_PREFIX + customerId)
                .map(json -> jsonMapper.readValue(json, Customer.class))
                .doOnNext(customer -> log.debug("Customer cache hit customerId={}", customerId))
                .onErrorResume(error -> {
                    log.warn("Redis read failed customerId={}", customerId, error);
                    return Mono.empty();
                });
    }

    private Mono<Void> cache(Customer customer) {
        return redisTemplate.opsForValue()
                .set(KEY_PREFIX + customer.id(), jsonMapper.writeValueAsString(customer), ttl)
                .then()
                .onErrorResume(error -> {
                    log.warn("Redis write failed customerId={}", customer.id(), error);
                    return Mono.empty();
                });
    }
}
