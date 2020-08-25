package ragna.wf.orc.eventstore.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import ragna.wf.orc.common.events.DomainEvent;
import ragna.wf.orc.common.exceptions.eventstore.EventStoreException;
import ragna.wf.orc.eventstore.model.StoredEvent;
import ragna.wf.orc.eventstore.model.StoredEventStatus;
import ragna.wf.orc.eventstore.repository.StoredEventRepository;
import ragna.wf.orc.eventstore.service.mappers.StoredEventMapper;
import ragna.wf.orc.eventstore.service.vo.StoredEventVo;
import ragna.wf.orc.eventstore.service.vo.UpdateStoredEventCommand;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EventStoreService {
  private static final FluentLogger LOGGER = FluentLoggerFactory.getLogger(EventStoreService.class);
  private final StoredEventRepository storedEventRepository;
  private final EventSerializationDelegate eventSerializationDelegate;
  private final TransactionalOperator transactionalOperator;

  @Transactional
  public Mono<StoredEventVo> append(final DomainEvent domainEvent) {
    LOGGER.info().log("appending to event store: {}", domainEvent);
    final var objectId = domainEvent.getObjectId();
    final var typeName = domainEvent.getEventName();
    final var payload = serializeEvent(domainEvent);

    var saveStoredEventMono =
        this.storedEventRepository
            .save(
                StoredEvent.createStoredEvent(
                    objectId,
                    typeName,
                    payload,
                    domainEvent.getTimestamp(),
                    eventSerializationDelegate.getSerializationEngine()))
            .map(StoredEventMapper.INSTANCE::toService)
            .doOnError(throwable -> LOGGER.error().log("Error on StoredEvent stored!", throwable))
            .doOnNext(
                storedEvent ->
                    LOGGER.info().log("StoredEvent stored! {}", storedEvent));
    return this.transactionalOperator.transactional(saveStoredEventMono);
  }

  public Flux<StoredEvent> findByObjectId(String objectId) {
    return storedEventRepository.findByObjectIdOrderByOccurredOnAsc(objectId);
  }

  private byte[] serializeEvent(final DomainEvent sourceObject) {
    return eventSerializationDelegate.serializeEvent(sourceObject);
  }

  @Transactional
  public Mono<StoredEventVo> updateStatus(final UpdateStoredEventCommand updateStoredEventCommand) {

    return storedEventRepository
        .findById(updateStoredEventCommand.getId())
        .map(
            storedEvent ->
                this.updateStoredEventStatus(storedEvent, updateStoredEventCommand))
        .flatMap(storedEventRepository::save)
        .map(StoredEventMapper.INSTANCE::toService);
  }

  private StoredEvent updateStoredEventStatus(
      final StoredEvent storedEvent, final UpdateStoredEventCommand updateStoredEventCommand) {

    return switch (updateStoredEventCommand.getTargetState()) {
      case PROCESSED -> storedEvent.processed();
      case PUBLISHED -> storedEvent.published();
      case UNPUBLISHED -> storedEvent.unpublished();
      case FAILED -> storedEvent.failed(updateStoredEventCommand.getMessage());
    };
  }
}
