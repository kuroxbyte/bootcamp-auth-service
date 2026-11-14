package dev.bootcamp.authservice.infrastructure.cache;

import dev.bootcamp.authservice.domain.model.wallet.Wallet;
import dev.bootcamp.authservice.infrastructure.persistence.WalletDocument;
import dev.bootcamp.authservice.infrastructure.persistence.WalletMongoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CachedWalletRepositoryTest {

    private static final Duration TTL = Duration.ofMinutes(10);
    private static final Wallet WALLET = new Wallet("w1", "987654321");
    private static final String KEY = "wallet:987654321";
    private static final String JSON = "{\"id\":\"w1\",\"phone\":\"987654321\"}";

    @Mock private WalletMongoRepository mongoRepository;

    @Mock private ReactiveStringRedisTemplate redisTemplate;

    @Mock private ReactiveValueOperations<String, String> valueOperations;

    private CachedWalletRepository repository;

    @BeforeEach
    void createRepository() {
        // Las pruebas de borrado no usan opsForValue().
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        repository = new CachedWalletRepository(
                mongoRepository,
                redisTemplate,
                JsonMapper.builder().build(),
                TTL);
    }

    @Test
    void savesInMongoAndCachesByPhone() {
        when(mongoRepository.save(any()))
                .thenAnswer(inv -> Mono.just(inv.<WalletDocument>getArgument(0)));
        when(valueOperations.set(KEY, JSON, TTL)).thenReturn(Mono.just(true));

        repository.save(WALLET).test().awaitDone(1, TimeUnit.SECONDS).assertComplete();

        verify(valueOperations).set(KEY, JSON, TTL);
    }

    @Test
    void readsFromCacheWithoutQueryingMongo() {
        when(valueOperations.get(KEY)).thenReturn(Mono.just(JSON));
        when(mongoRepository.findByPhone("987654321")).thenReturn(Mono.empty());

        repository.findByPhone("987654321").test().assertValue(WALLET);

        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void readsFromMongoWhenCacheMissesOrFails() {
        when(valueOperations.get(KEY))
                .thenReturn(Mono.empty())
                .thenReturn(Mono.error(new IllegalStateException("redis down")));
        when(mongoRepository.findByPhone("987654321"))
                .thenReturn(Mono.just(new WalletDocument("w1", "987654321")));
        when(valueOperations.set(KEY, JSON, TTL))
                .thenReturn(Mono.just(true))
                .thenReturn(Mono.error(new IllegalStateException("redis down")));

        repository.findByPhone("987654321").test().assertValue(WALLET);
        repository.findByPhone("987654321").test().assertValue(WALLET);
    }

    @Test
    void deletesFromMongoAndEvictsThePhoneKey() {
        when(mongoRepository.deleteById("w1")).thenReturn(Mono.empty());
        when(redisTemplate.delete("wallet:987654321")).thenReturn(Mono.just(1L));

        repository.delete(WALLET).test().awaitDone(1, TimeUnit.SECONDS).assertComplete();

        verify(mongoRepository).deleteById("w1");
    }

    @Test
    void deletesEvenWhenRedisFails() {
        when(mongoRepository.deleteById("w1")).thenReturn(Mono.empty());
        when(redisTemplate.delete("wallet:987654321"))
                .thenReturn(Mono.error(new IllegalStateException("redis down")));

        repository.delete(WALLET).test().awaitDone(1, TimeUnit.SECONDS).assertComplete();
    }

    @Test
    void deletesTheCopyOfAnotherWalletWithThePhone() {
        when(mongoRepository.deleteByPhoneAndIdNot("987654321", "w1")).thenReturn(Mono.empty());

        repository.deleteOtherWithPhone(WALLET).test().awaitDone(1, TimeUnit.SECONDS)
                .assertComplete();

        verify(mongoRepository).deleteByPhoneAndIdNot("987654321", "w1");
    }
}
