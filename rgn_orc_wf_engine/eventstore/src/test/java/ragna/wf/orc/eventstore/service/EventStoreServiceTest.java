package ragna.wf.orc.eventstore.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ragna.wf.orc.common.events.DomainEvent;
import ragna.wf.orc.eventstore.EventStoreTestApplication;
import ragna.wf.orc.eventstore.model.StoredEventStatus;
import ragna.wf.orc.eventstore.repository.StoredEventRepository;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventStoreTestApplication.class)
public class EventStoreServiceTest {
  @Autowired
  private EventStoreService eventStoreService;
  @Autowired
  private StoredEventRepository storedEventRepository;
  @Autowired
  private EventSerializationDelegate eventSerializationDelegate;

  @Test
  void whenDomainEventIsAppended_thenStoredEventIsFound() {
    // given
    final var johnConnor = "John Connor";
    final var id = "1";
    final var person = new Person(id, johnConnor);
    final var personEvent = new PersonCreated(person);
    final var serializedEvent = eventSerializationDelegate.serializeEvent(personEvent);

    // when
    final var storedEventMono = eventStoreService.append(personEvent);

    StepVerifier.create(storedEventMono).expectNextCount(1).verifyComplete();

    // then
    StepVerifier.create(storedEventRepository.findById(1L))
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
    public PersonCreated() {
    }

    public PersonCreated(Person person) {
      super(person, person.getId(), "testAction()");
    }
  }
}
