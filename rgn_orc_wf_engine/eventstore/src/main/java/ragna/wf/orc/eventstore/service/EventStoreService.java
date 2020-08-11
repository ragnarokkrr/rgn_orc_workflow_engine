package ragna.wf.orc.eventstore.service;

import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ragna.wf.orc.common.events.DomainEvent;
import ragna.wf.orc.eventstore.model.StoredEvent;
import ragna.wf.orc.eventstore.repository.StoredEventRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EventStoreService {
    private static final FluentLogger LOGGER = FluentLoggerFactory.getLogger(EventStoreService.class);
    private final StoredEventRepository storedEventRepository;
    private final EventSerializationDelegate eventSerializationDelegate;

    // Todo Fix transactional
    // @Transactional
    public Mono<StoredEvent> append(final DomainEvent domainEvent) {
        LOGGER.debug().log("appending to event store: {}", domainEvent);
        final var objectId = domainEvent.getObjectId();
        final var typeName = domainEvent.getEventName();
        final var payload = serializeEvent(domainEvent);

        return this.storedEventRepository
                .save(
                        StoredEvent.createStoredEvent(
                                objectId,
                                typeName,
                                payload,
                                domainEvent.getTimestamp(),
                                eventSerializationDelegate.getSerializationEngine()))
                .doOnError(throwable -> LOGGER.error().log("Error on StoredEvent stored!", throwable))
                .doOnNext(
                        storedEvent ->
                                LOGGER.debug().log("StoredEvent stored! {}", storedEvent.shortToString()));
    }

    public Flux<StoredEvent> findByObjectId(String objectId) {
        return storedEventRepository.findByObjectIdOrderByIdAsc(objectId);
    }

    private byte[] serializeEvent(final DomainEvent sourceObject) {
        return eventSerializationDelegate.serializeEvent(sourceObject);
    }
}
