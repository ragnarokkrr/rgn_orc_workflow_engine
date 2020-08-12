package ragna.wf.orc.engine.infrastructure.domainevents.handlers;

import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ragna.wf.orc.common.events.DomainEvent;
import ragna.wf.orc.eventstore.model.StoredEvent;
import ragna.wf.orc.eventstore.service.EventStoreService;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EventStoreAppenderHandler {
  private static final FluentLogger LOGGER =
      FluentLoggerFactory.getLogger(EventStoreAppenderHandler.class);

  private final ReplayProcessor<DomainEvent> domainEventReplayProcessor;
  private final EventStoreService eventStoreService;

  @PostConstruct
  void dispatchDomainEvents() {
    LOGGER.info().log("EventStoreAppenderHandler ONLINE!");
    domainEventReplayProcessor
        .doOnNext(domainEvent -> LOGGER.info().log("Appending event to EventStore {}", domainEvent))
        .flatMap(this::handleDomainEvent)
        .subscribe(
            domainEvent ->
                LOGGER.info().log("Finish appending event to EventStore {}", domainEvent));
  }

  public Mono<DomainEvent> handleDomainEvent(final DomainEvent domainEvent) {
    return this.appendToEventStore(domainEvent)
        .map(storedEvent -> domainEvent)
        .onErrorResume(throwable -> resumeHandling(domainEvent, throwable));
  }

  private Mono<DomainEvent> resumeHandling(
      final DomainEvent domainEvent, final Throwable throwable) {
    return Mono.just(domainEvent)
        .doOnNext(
            (domainEvent1) ->
                LOGGER
                    .warn()
                    .log(
                        "Resuming from error processing event {}. Error=({})",
                        domainEvent1,
                        throwable.getMessage()));
  }

  private Mono<StoredEvent> appendToEventStore(final DomainEvent domainEvent) {
    return this.eventStoreService
        .append(domainEvent)
        .doOnSuccess(event -> LOGGER.debug().log("Domain event stored: {}", event.shortToString()));
  }
}
