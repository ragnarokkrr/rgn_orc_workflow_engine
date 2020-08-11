package ragna.wf.orc.eventstore.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ragna.wf.orc.eventstore.EventStoreTestApplication;
import ragna.wf.orc.eventstore.model.SerializationEngine;
import ragna.wf.orc.eventstore.model.StoredEvent;
import ragna.wf.orc.eventstore.model.StoredEventStatus;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventStoreTestApplication.class)
public class StoredEventRepositoryTest {
    @Autowired
    private StoredEventRepository storedEventRepository;

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
        final var storedEventSaveMono = storedEventRepository.save(newStoredEvent);
        StepVerifier.create(storedEventSaveMono)
                .expectNextMatches(
                        storedEvent -> {
                            assertThat(storedEvent).hasFieldOrPropertyWithValue("id", 1L);

                            return true;
                        })
                .verifyComplete();

        StepVerifier.create(storedEventRepository.count()).expectNext(1L).verifyComplete();
        StepVerifier.create(storedEventRepository.findById(1L))
                .expectNextMatches(
                        storedEvent -> {
                            assertThat(storedEvent)
                                    .hasFieldOrPropertyWithValue("id", 1L)
                                    .hasFieldOrPropertyWithValue("objectId", "1")
                                    .hasFieldOrPropertyWithValue("payload", payload.getBytes())
                                    .hasFieldOrPropertyWithValue("typedName", typedName)
                                    .hasFieldOrPropertyWithValue("eventStatus", StoredEventStatus.UNPROCESSED)
                                    .hasFieldOrPropertyWithValue("serializationEngine", SerializationEngine.KRYO)
                                    .hasNoNullFieldsOrPropertiesExcept("processingOn", "processedOn");

                            return true;
                        })
                .verifyComplete();
    }
}
