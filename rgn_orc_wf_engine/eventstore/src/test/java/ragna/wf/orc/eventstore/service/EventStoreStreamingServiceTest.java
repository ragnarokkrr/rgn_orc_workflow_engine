package ragna.wf.orc.eventstore.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MongoDBContainer;
import ragna.wf.orc.common.data.mongodb.utils.MongoDbUtils;
import ragna.wf.orc.common.events.DomainEvent;
import ragna.wf.orc.common.serialization.DefaultKryoContext;
import ragna.wf.orc.common.serialization.KryoContext;
import ragna.wf.orc.eventstore.EventStoreTestApplication;
import ragna.wf.orc.eventstore.config.MongoDBTestContainers;
import ragna.wf.orc.eventstore.model.SerializationEngine;
import ragna.wf.orc.eventstore.model.StoredEvent;
import ragna.wf.orc.eventstore.repository.StoreEventPollingRepository;
import ragna.wf.orc.eventstore.repository.StoredEventRepository;
import ragna.wf.orc.eventstore.service.vo.StoredEventVo;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventStoreTestApplication.class)
class EventStoreStreamingServiceTest {
  @Autowired private StoredEventRepository storedEventRepository;
  @Autowired private ReactiveMongoOperations reactiveMongoOperations;
  @Autowired private StoreEventPollingRepository storeEventPollingRepository;
  @Autowired private EventStoreStreamingService eventStoreStreamingService;
  private KryoContext kryoContext = DefaultKryoContext.kryoContextWithDefaultSerializers();

  private static final MongoDBContainer MONGO_DB_CONTAINER =
      MongoDBTestContainers.defaultMongoContainer();

  @BeforeAll
  static void setUpAll() {
    MONGO_DB_CONTAINER.start();

    MongoDBTestContainers.setSpringDataProperties(MONGO_DB_CONTAINER);
  }

  @AfterAll
  static void tearDownAll() {
    if (!MONGO_DB_CONTAINER.isShouldBeReused()) {
      MONGO_DB_CONTAINER.stop();
    }
  }

  @BeforeEach
  void before() {
    final var createCollectionFlux = MongoDbUtils.reCreateCollections(this.reactiveMongoOperations);
    StepVerifier.create(createCollectionFlux)
        .expectNextCount(MongoDbUtils.getCollectionNames().size())
        .verifyComplete();
  }

  @Test
  void streamUnprocessedEventsTest() {
    // Given
    final var typedName = Person.class.getName();
    final var now = LocalDateTime.now();
    final var personList =
        IntStream.rangeClosed(1, 50)
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
    final var storedEventVoFlux = eventStoreStreamingService.streamUnprocessedEvents();

    final var storedEvents = new ArrayList<StoredEventVo>();
    StepVerifier.create(storedEventVoFlux)
        .recordWith(() -> storedEvents)
        .expectNextCount(50)
        .verifyTimeout(Duration.ofSeconds(1));

    // then
    assertThat(storedEvents).hasSize(50);

    assertThat(storedEvents)
        .flatExtracting(StoredEventVo::getObjectId)
        .containsAll(
            IntStream.rangeClosed(1, 50).mapToObj(String::valueOf).collect(Collectors.toList()));

    assertThat(storedEvents)
        .flatExtracting(StoredEventVo::getEventStatus)
        .containsOnly(StoredEventVo.StoredEventStatus.PROCESSING);

    assertThat(storedEvents).flatExtracting(StoredEventVo::getProcessingOn).doesNotContainNull();
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  static class Person extends DomainEvent {
    private String id;
    private String name;
  }
}
