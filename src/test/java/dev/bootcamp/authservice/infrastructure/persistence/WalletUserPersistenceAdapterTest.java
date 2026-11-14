package dev.bootcamp.authservice.infrastructure.persistence;

import dev.bootcamp.authservice.domain.model.user.WalletUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataMongoTest
@Import(WalletUserPersistenceAdapter.class)
class WalletUserPersistenceAdapterTest {

    @Container @ServiceConnection static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @Autowired private WalletUserPersistenceAdapter adapter;

    @Autowired private WalletUserMongoRepository mongoRepository;

    @Autowired private ReactiveMongoTemplate mongoTemplate;

    @BeforeEach
    void cleanCollectionAndCreateIndexes() {
        mongoRepository.deleteAll().block();
        // La creación automática de índices es asíncrona: se crean aquí para probarlos.
        new MongoPersistentEntityIndexResolver(mongoTemplate.getConverter().getMappingContext())
                .resolveIndexFor(WalletUserDocument.class)
                .forEach(index -> mongoTemplate.indexOps(WalletUserDocument.class)
                        .createIndex(index)
                        .block());
    }

    @Test
    void savesAndFindsAccessByPhone() {
        WalletUser saved =
                adapter.save(new WalletUser(null, "w1", "987654321", "hash")).blockingGet();

        assertThat(saved.id()).isNotBlank();
        adapter.findByPhone("987654321").test().awaitDone(5, TimeUnit.SECONDS).assertValue(saved);
        adapter.existsByPhone("987654321").test().awaitDone(5, TimeUnit.SECONDS)
                .assertValue(true);
        adapter.existsByPhone("912345678").test().awaitDone(5, TimeUnit.SECONDS)
                .assertValue(false);
    }

    @Test
    void uniqueIndexesRejectSecondAccessForWalletOrPhone() {
        adapter.save(new WalletUser(null, "w1", "987654321", "hash")).blockingGet();

        adapter.save(new WalletUser(null, "w1", "912345678", "hash")).test()
                .awaitDone(5, TimeUnit.SECONDS)
                .assertError(DuplicateKeyException.class);
        adapter.save(new WalletUser(null, "w2", "987654321", "hash")).test()
                .awaitDone(5, TimeUnit.SECONDS)
                .assertError(DuplicateKeyException.class);
    }

    @Test
    void deletesTheAccessOfTheWallet() {
        adapter.save(new WalletUser(null, "w1", "987654321", "hash")).blockingGet();

        adapter.deleteByWalletId("w1").blockingAwait();

        adapter.existsByPhone("987654321").test().awaitDone(5, TimeUnit.SECONDS)
                .assertValue(false);
    }
}
