package dev.bootcamp.authservice.infrastructure.persistence;

import dev.bootcamp.authservice.domain.model.user.UserAccount;
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
@Import(UserAccountPersistenceAdapter.class)
class UserAccountPersistenceAdapterTest {

    @Container @ServiceConnection static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @Autowired private UserAccountPersistenceAdapter adapter;

    @Autowired private UserAccountMongoRepository mongoRepository;

    @Autowired private ReactiveMongoTemplate mongoTemplate;

    @BeforeEach
    void cleanCollectionAndCreateIndexes() {
        mongoRepository.deleteAll().block();
        // La creación automática de índices es asíncrona: se crean aquí para probarlos.
        new MongoPersistentEntityIndexResolver(mongoTemplate.getConverter().getMappingContext())
                .resolveIndexFor(UserAccountDocument.class)
                .forEach(index -> mongoTemplate.indexOps(UserAccountDocument.class)
                        .createIndex(index)
                        .block());
    }

    @Test
    void savesAndFindsUserByUsername() {
        UserAccount saved =
                adapter.save(new UserAccount(null, "c1", "ana", "hash")).blockingGet();

        assertThat(saved.id()).isNotBlank();
        adapter.findByUsername("ana").test().awaitDone(5, TimeUnit.SECONDS).assertValue(saved);
        adapter.findByUsername("nadie").test().awaitDone(5, TimeUnit.SECONDS).assertNoValues();
    }

    @Test
    void tellsIfUsernameOrCustomerAlreadyHasUser() {
        adapter.save(new UserAccount(null, "c1", "ana", "hash")).blockingGet();

        adapter.existsByUsername("ana").test().awaitDone(5, TimeUnit.SECONDS).assertValue(true);
        adapter.existsByUsername("luis").test().awaitDone(5, TimeUnit.SECONDS).assertValue(false);
        adapter.existsByCustomerId("c1").test().awaitDone(5, TimeUnit.SECONDS).assertValue(true);
        adapter.existsByCustomerId("c2").test().awaitDone(5, TimeUnit.SECONDS).assertValue(false);
    }

    @Test
    void uniqueIndexesRejectDuplicates() {
        adapter.save(new UserAccount(null, "c1", "ana", "hash")).blockingGet();

        adapter.save(new UserAccount(null, "c2", "ana", "hash")).test()
                .awaitDone(5, TimeUnit.SECONDS)
                .assertError(DuplicateKeyException.class);
        adapter.save(new UserAccount(null, "c1", "otra", "hash")).test()
                .awaitDone(5, TimeUnit.SECONDS)
                .assertError(DuplicateKeyException.class);
    }

    @Test
    void deletesTheUserOfTheCustomer() {
        adapter.save(new UserAccount(null, "c1", "ana", "hash")).blockingGet();

        adapter.deleteByCustomerId("c1").blockingAwait();
        adapter.deleteByCustomerId("nadie").blockingAwait();

        adapter.existsByCustomerId("c1").test().awaitDone(5, TimeUnit.SECONDS).assertValue(false);
    }
}
