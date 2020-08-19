package ragna.wf.orc.eventstore.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ragna.wf.orc.common.data.mongodb.utils.MongoDbUtils;
import ragna.wf.orc.common.serialization.DefaultKryoContext;
import ragna.wf.orc.common.serialization.KryoContext;
import ragna.wf.orc.eventstore.EventStoreTestApplication;
import ragna.wf.orc.eventstore.config.EmbeddedMongoWithTransactionsConfig;
import ragna.wf.orc.eventstore.model.SerializationEngine;
import ragna.wf.orc.eventstore.model.StoredEvent;
import ragna.wf.orc.eventstore.model.StoredEventStatus;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@Profile("embedMongoWithTx")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventStoreTestApplication.class)
@Import(EmbeddedMongoWithTransactionsConfig.class)
class StoredEventPollingMongoRepositoryTest {
  @Autowired private StoredEventRepository storedEventRepository;
  @Autowired private ReactiveMongoOperations reactiveMongoOperations;
  @Autowired private StoredEventPollingMongoRepository storedEventPollingMongoRepository;
  private KryoContext kryoContext = DefaultKryoContext.kryoContextWithDefaultSerializers();

  @BeforeEach
  void before() {
    final var createCollectionFlux = MongoDbUtils.reCreateCollections(this.reactiveMongoOperations);
    StepVerifier.create(createCollectionFlux)
        .expectNextCount(MongoDbUtils.getCollectionNames().size())
        .verifyComplete();
  }

  @Test
  void whenFindPagedUnprocessedStoredEvents_shouldReturnTheRequiredPagesAndMarkItProcessed() {
    // Given
    final var typedName = Person.class.getName();
    final var now = LocalDateTime.now();
    final var personList =
        IntStream.rangeClosed(1, 20)
            .mapToObj(
                id -> Person.builder().id(String.valueOf(id)).name("John Connor " + id).build())
            .map(
                person ->
                    StoredEvent.createStoredEvent(
                        person.getId(),
                        typedName,
                        kryoContext.serialize(person),
                        now,
                        SerializationEngine.KRYO))
            .collect(Collectors.toList());
    final var saveStoredEventListMono =
        Flux.fromIterable(personList).flatMap(storedEventRepository::save).collectList();

    StepVerifier.create(saveStoredEventListMono).expectNextCount(1).verifyComplete();
    // when

    final var firstPageMono =
        storedEventPollingMongoRepository
            .findByStatusUnprocessedOrderByIdAscAndMarkAsProcessing(PageRequest.of(1, 10))
            .collectList();

    final var secondPageMono =
        storedEventPollingMongoRepository
            .findByStatusUnprocessedOrderByIdAscAndMarkAsProcessing(PageRequest.of(1, 10))
            .collectList();

    // then
    StepVerifier.create(firstPageMono)
        .expectNextMatches(
            storedEvents -> {
              assertThat(storedEvents).isNotEmpty();

              assertThat(storedEvents)
                  .flatExtracting(StoredEvent::getObjectId)
                  .containsAll(
                      IntStream.range(1, 11)
                          .mapToObj(String::valueOf)
                          .collect(Collectors.toList()));

              assertThat(storedEvents)
                  .flatExtracting(StoredEvent::getEventStatus)
                  .containsOnly(StoredEventStatus.PROCESSING);

              assertThat(storedEvents)
                  .flatExtracting(StoredEvent::getProcessingOn)
                  .doesNotContainNull();
              return true;
            })
        .verifyComplete();

    StepVerifier.create(secondPageMono)
        .expectNextMatches(
            storedEvents -> {
              assertThat(storedEvents).isNotEmpty();

              assertThat(storedEvents)
                  .flatExtracting(StoredEvent::getObjectId)
                  .containsAll(
                      IntStream.range(11, 20)
                          .mapToObj(String::valueOf)
                          .collect(Collectors.toList()));

              assertThat(storedEvents)
                  .flatExtracting(StoredEvent::getEventStatus)
                  .containsOnly(StoredEventStatus.PROCESSING);

              assertThat(storedEvents)
                  .flatExtracting(StoredEvent::getProcessingOn)
                  .doesNotContainNull();
              return true;
            })
        .verifyComplete();
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  static class Person {
    private String id;
    private String name;
  }
}
