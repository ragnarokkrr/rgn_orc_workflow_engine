package ragna.wf.orc.eventstore.service;

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
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ragna.wf.orc.common.events.DomainEvent;
import ragna.wf.orc.eventstore.EventStoreTestApplication;
import ragna.wf.orc.eventstore.config.EmbeddedMongoWithTransactionsConfig;
import ragna.wf.orc.eventstore.model.StoredEvent;
import ragna.wf.orc.eventstore.model.StoredEventStatus;
import ragna.wf.orc.eventstore.repository.StoredEventRepository;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@Profile("embedMongoWithTx")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventStoreTestApplication.class)
@Import(EmbeddedMongoWithTransactionsConfig.class)
public class EventStoreServiceTest {
  @Autowired private EventStoreService eventStoreService;
  @Autowired private StoredEventRepository storedEventRepository;
  @Autowired private EventSerializationDelegate eventSerializationDelegate;
  @Autowired private ReactiveMongoOperations reactiveMongoOperations;

  @BeforeEach
  void before() {
    final var createCollectionFlux =
        Flux.just("stored_events", "database_sequences")
            .flatMap(reactiveMongoOperations::createCollection);
    StepVerifier.create(createCollectionFlux).expectNextCount(2).verifyComplete();
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
    final var savedStoredEvent = new StoredEvent[1];
    final var storedEventSaveMono = eventStoreService.append(personEvent);
    StepVerifier.create(storedEventSaveMono)
        .expectNextMatches(
            storedEvent -> {
              assertThat(storedEvent)
                  .hasNoNullFieldsOrPropertiesExcept("processingOn", "processedOn");
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
                      "processingOn", "processedOn", "replayTracking");

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
