package dev.bootcamp.authservice.infrastructure.cache;

import dev.bootcamp.authservice.domain.model.customer.Customer;
import dev.bootcamp.authservice.infrastructure.persistence.CustomerDocument;
import dev.bootcamp.authservice.infrastructure.persistence.CustomerMongoRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CachedCustomerRepositoryTest {

    private static final Duration TTL = Duration.ofMinutes(10);
    private static final Customer CUSTOMER = new Customer("c1", "PERSONAL", "VIP");
    private static final String JSON =
            "{\"id\":\"c1\",\"type\":\"PERSONAL\",\"profile\":\"VIP\"}";

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Mock private CustomerMongoRepository mongoRepository;

    @Mock private ReactiveStringRedisTemplate redisTemplate;

    @Mock private ReactiveValueOperations<String, String> valueOperations;

    private CachedCustomerRepository repository;

    @BeforeEach
    void createRepository() {
        // Las pruebas de borrado no usan opsForValue().
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        repository = new CachedCustomerRepository(mongoRepository, redisTemplate, jsonMapper, TTL);
    }

    @Test
    void savesInMongoAndCachesWithTtl() {
        when(mongoRepository.save(any()))
                .thenAnswer(inv -> Mono.just(inv.<CustomerDocument>getArgument(0)));
        when(valueOperations.set("customer:c1", JSON, TTL)).thenReturn(Mono.just(true));

        repository.save(CUSTOMER).test().awaitDone(1, TimeUnit.SECONDS)
                .assertComplete();

        verify(valueOperations).set("customer:c1", JSON, TTL);
    }

    @Test
    void readsFromCacheWithoutQueryingMongo() {
        when(valueOperations.get("customer:c1")).thenReturn(Mono.just(JSON));
        when(mongoRepository.findById("c1")).thenReturn(Mono.empty());

        repository.findById("c1").test().assertValue(CUSTOMER);

        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void readsFromMongoOnCacheMissAndCachesIt() {
        when(valueOperations.get("customer:c1")).thenReturn(Mono.empty());
        when(mongoRepository.findById("c1"))
                .thenReturn(Mono.just(new CustomerDocument("c1", "PERSONAL", "VIP")));
        when(valueOperations.set(eq("customer:c1"), anyString(), eq(TTL)))
                .thenReturn(Mono.just(true));

        repository.findById("c1").test().assertValue(CUSTOMER);

        verify(valueOperations).set("customer:c1", JSON, TTL);
    }

    @Test
    void fallsBackToMongoWhenRedisFails() {
        when(valueOperations.get("customer:c1"))
                .thenReturn(Mono.error(new IllegalStateException("redis down")));
        when(mongoRepository.findById("c1"))
                .thenReturn(Mono.just(new CustomerDocument("c1", "PERSONAL", "VIP")));
        when(valueOperations.set(eq("customer:c1"), anyString(), eq(TTL)))
                .thenReturn(Mono.error(new IllegalStateException("redis down")));

        repository.findById("c1").test().assertValue(CUSTOMER);
    }

    @Test
    void emitsNothingForUnknownCustomer() {
        when(valueOperations.get("customer:c9")).thenReturn(Mono.empty());
        when(mongoRepository.findById("c9")).thenReturn(Mono.empty());

        repository.findById("c9").test().assertNoValues().assertComplete();
    }

    @Test
    void deletesFromMongoAndEvictsTheCache() {
        when(mongoRepository.deleteById("c1")).thenReturn(Mono.empty());
        when(redisTemplate.delete("customer:c1")).thenReturn(Mono.just(1L));

        repository.deleteById("c1").test().awaitDone(1, TimeUnit.SECONDS).assertComplete();

        verify(mongoRepository).deleteById("c1");
        verify(redisTemplate).delete("customer:c1");
    }

    @Test
    void deletesEvenWhenRedisFails() {
        when(mongoRepository.deleteById("c1")).thenReturn(Mono.empty());
        when(redisTemplate.delete("customer:c1"))
                .thenReturn(Mono.error(new IllegalStateException("redis down")));

        repository.deleteById("c1").test().awaitDone(1, TimeUnit.SECONDS).assertComplete();
    }
}
