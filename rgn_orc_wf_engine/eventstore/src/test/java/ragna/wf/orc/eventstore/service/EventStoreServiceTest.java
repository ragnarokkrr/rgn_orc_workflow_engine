package ragna.wf.orc.eventstore.service;

import static org.assertj.core.api.Assertions.assertThat;

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
import ragna.wf.orc.eventstore.EventStoreTestApplication;
import ragna.wf.orc.eventstore.config.MongoDBTestContainers;
import ragna.wf.orc.eventstore.model.StoredEventStatus;
import ragna.wf.orc.eventstore.repository.StoredEventRepository;
import ragna.wf.orc.eventstore.service.vo.StoredEventVo;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventStoreTestApplication.class)
public class EventStoreServiceTest {
  @Autowired private EventStoreService eventStoreService;
  @Autowired private StoredEventRepository storedEventRepository;
  @Autowired private EventSerializationDelegate eventSerializationDelegate;
  @Autowired private ReactiveMongoOperations reactiveMongoOperations;

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
  void whenDomainEventIsAppended_thenStoredEventIsFound() {
    // given
    final var johnConnor = "John Connor";
    final var id = "1";
    final var person = new Person(id, johnConnor);
    final var personEvent = new PersonCreated(person);
    final var serializedEvent = eventSerializationDelegate.serializeEvent(personEvent);

    // when
    final var savedStoredEvent = new StoredEventVo[1];
    final var storedEventSaveMono = eventStoreService.append(personEvent);
    StepVerifier.create(storedEventSaveMono)
        .expectNextMatches(
            storedEvent -> {
              assertThat(storedEvent)
                  .hasNoNullFieldsOrPropertiesExcept("processingOn", "processedOn", "errorMessage");
              savedStoredEvent[0] = storedEvent;
              return true;
            })
        .verifyComplete();
    // then
    StepVerifier.create(storedEventRepository.findById(savedStoredEvent[0].getId()))
        .expectNextMatches(
            storedEvent -> {
              assertThat(storedEvent)
                  .isNotNull()
                  .hasFieldOrPropertyWithValue("objectId", "1")
                  .hasFieldOrPropertyWithValue("eventStatus", StoredEventStatus.UNPROCESSED)
                  .hasFieldOrPropertyWithValue("typedName", PersonCreated.class.getName())
                  .hasNoNullFieldsOrPropertiesExcept(
                      "processingOn", "processedOn", "replayTracking", "errorMessage");

              assertThat(storedEvent.getPayload()).containsExactly(serializedEvent);

              final var domainEvent =
                  eventSerializationDelegate.deserialize(
                      PersonCreated.class.getName(), storedEvent.getPayload());

              assertThat((Person) domainEvent.getSource())
                  .hasFieldOrPropertyWithValue("name", johnConnor);

              return true;
            })
        .verifyComplete();
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  static class Person {
    private String id;
    private String name;
  }

  static class PersonCreated extends DomainEvent {
    public PersonCreated() {}

    public PersonCreated(Person person) {
      super(person, person.getId(), "testAction()");
    }
  }
}
