package ragna.wf.orc.eventstore.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ragna.wf.orc.common.data.mongodb.utils.MongoDbUtils;
import ragna.wf.orc.eventstore.EventStoreTestApplication;
import ragna.wf.orc.eventstore.config.EmbeddedMongoWithTransactionsConfig;
import ragna.wf.orc.eventstore.model.SerializationEngine;
import ragna.wf.orc.eventstore.model.StoredEvent;
import ragna.wf.orc.eventstore.model.StoredEventStatus;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Profile("embedMongoWithTx")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventStoreTestApplication.class)
@Import(EmbeddedMongoWithTransactionsConfig.class)
public class StoredEventRepositoryTest {
  @Autowired private StoredEventRepository storedEventRepository;
  @Autowired private ReactiveMongoOperations reactiveMongoOperations;

  @BeforeEach
  void before() {
    final var createCollectionFlux = MongoDbUtils.reCreateCollections(this.reactiveMongoOperations);
    StepVerifier.create(createCollectionFlux).expectNextCount(MongoDbUtils.getCollectionNames().size()).verifyComplete();
  }

  @Test
  void whenAStoredEventIsSaved_thenItCanBeFound() {
    // given
    final var time = LocalDateTime.now();
    final var payload = "my payload";
    final var typedName = "java.lang.String";
    final var objectId = "1";
    final var newStoredEvent =
        StoredEvent.createStoredEvent(
            objectId, typedName, payload.getBytes(), time, SerializationEngine.KRYO);

    // when
    final var savedStoredEvent = new StoredEvent[1];
    final var storedEventSaveMono = storedEventRepository.save(newStoredEvent);
    StepVerifier.create(storedEventSaveMono)
        .expectNextMatches(
            storedEvent -> {
              assertThat(storedEvent)
                  .hasNoNullFieldsOrPropertiesExcept("processingOn", "processedOn");
              savedStoredEvent[0] = storedEvent;
              return true;
            })
        .verifyComplete();

    StepVerifier.create(storedEventRepository.count()).expectNext(1L).verifyComplete();
    StepVerifier.create(storedEventRepository.findById(savedStoredEvent[0].getId()))
        .expectNextMatches(
            storedEvent -> {
              assertThat(storedEvent)
                  .hasFieldOrPropertyWithValue("id", savedStoredEvent[0].getId())
                  .hasFieldOrPropertyWithValue("objectId", "1")
                  .hasFieldOrPropertyWithValue("payload", payload.getBytes())
                  .hasFieldOrPropertyWithValue("typedName", typedName)
                  .hasFieldOrPropertyWithValue("eventStatus", StoredEventStatus.UNPROCESSED)
                  .hasFieldOrPropertyWithValue("serializationEngine", SerializationEngine.KRYO)
                  .hasNoNullFieldsOrPropertiesExcept("processingOn", "processedOn");

              return true;
            })
        .verifyComplete();
    System.out.println();
  }
}
