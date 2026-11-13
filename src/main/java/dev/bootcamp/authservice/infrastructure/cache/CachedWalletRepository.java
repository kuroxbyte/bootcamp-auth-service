package dev.bootcamp.authservice.infrastructure.cache;

import dev.bootcamp.authservice.application.port.WalletRepository;
import dev.bootcamp.authservice.domain.model.wallet.Wallet;
import dev.bootcamp.authservice.infrastructure.persistence.WalletDocument;
import dev.bootcamp.authservice.infrastructure.persistence.WalletMongoRepository;
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
 * Implementa el puerto {@link WalletRepository} con MongoDB como almacén y Redis como caché
 * (cache-aside).
 *
 * <ul>
 *   <li>Guardar: escribe en MongoDB y después en Redis ({@code wallet:{celular}}).
 *   <li>Buscar: lee de Redis; si no está, lee de MongoDB y lo deja en Redis.
 *   <li>Cada entrada vence según {@code cache.wallets.ttl}.
 *   <li>Si Redis falla, se registra y se sigue con MongoDB: la caché solo acelera el acceso.
 * </ul>
 */
@Slf4j
@Component
public class CachedWalletRepository implements WalletRepository {

    private static final String KEY_PREFIX = "wallet:";

    private final WalletMongoRepository mongoRepository;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper;
    private final Duration ttl;

    /**
     * Crea el repositorio con el tiempo de vida configurado para la caché.
     */
    public CachedWalletRepository(
            WalletMongoRepository mongoRepository,
            ReactiveStringRedisTemplate redisTemplate,
            JsonMapper jsonMapper,
            @Value("${cache.wallets.ttl}") Duration ttl) {
        this.mongoRepository = mongoRepository;
        this.redisTemplate = redisTemplate;
        this.jsonMapper = jsonMapper;
        this.ttl = ttl;
    }

    @Override
    public Completable save(Wallet wallet) {
        return Completable.fromPublisher(mongoRepository
                .save(new WalletDocument(wallet.id(), wallet.phone()))
                .then(cache(wallet)));
    }

    @Override
    public Maybe<Wallet> findByPhone(String phone) {
        Mono<Wallet> fromDatabase = mongoRepository.findByPhone(phone)
                .map(document -> new Wallet(document.getId(), document.getPhone()))
                .flatMap(wallet -> cache(wallet).thenReturn(wallet));
        return Maybe.fromPublisher(cached(phone).switchIfEmpty(fromDatabase));
    }

    private Mono<Wallet> cached(String phone) {
        return redisTemplate.opsForValue()
                .get(KEY_PREFIX + phone)
                .map(json -> jsonMapper.readValue(json, Wallet.class))
                .onErrorResume(error -> {
                    log.warn("Redis read failed phone={}", phone, error);
                    return Mono.empty();
                });
    }

    private Mono<Void> cache(Wallet wallet) {
        return redisTemplate.opsForValue()
                .set(KEY_PREFIX + wallet.phone(), jsonMapper.writeValueAsString(wallet), ttl)
                .then()
                .onErrorResume(error -> {
                    log.warn("Redis write failed walletId={}", wallet.id(), error);
                    return Mono.empty();
                });
    }
}
