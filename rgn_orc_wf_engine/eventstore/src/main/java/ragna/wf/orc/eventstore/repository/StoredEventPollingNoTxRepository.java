package ragna.wf.orc.eventstore.repository;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import ragna.wf.orc.eventstore.model.StoredEvent;
import ragna.wf.orc.eventstore.model.StoredEventStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.RetrySpec;

@Repository
@Profile({"no-tx", "default"})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StoredEventPollingNoTxRepository implements StoreEventPollingRepository {
  private static final FluentLogger LOGGER =
      FluentLoggerFactory.getLogger(StoredEventPollingNoTxRepository.class);
  private final ReactiveMongoOperations reactiveMongoOperations;

  @Override
  public Flux<StoredEvent> findByStatusUnprocessedOrderByIdAscAndMarkAsProcessing(
      final Pageable pageable) {

    final var unprocessedEventsQuery = queryUnprocessedEvents(pageable);
    return findAndUpdatePage(unprocessedEventsQuery)
        .retryWhen(RetrySpec.backoff(3, Duration.ofMillis(100)));
  }

  private Flux<StoredEvent> findAndUpdatePage(final Query unprocessedEventsQuery) {
    return reactiveMongoOperations
        .find(unprocessedEventsQuery, StoredEvent.class)
        .transform(this::addStoredEventLogging)
        .flatMap(this::findAndReplaceStoredEvent);
  }

  private Mono<StoredEvent> findAndReplaceStoredEvent(final StoredEvent storedEvent) {
    return reactiveMongoOperations
        .update(StoredEvent.class)
        .matching(queryStoredEventById(storedEvent))
        .replaceWith(storedEvent.processing())
        .withOptions(FindAndReplaceOptions.options().returnNew())
        .findAndReplace();
  }

  private Query queryUnprocessedEvents(final Pageable pageable) {
    return new Query()
        .addCriteria(Criteria.where("eventStatus").is(StoredEventStatus.UNPROCESSED.toString()))
        .with(Sort.by(Sort.Direction.ASC, "id"))
        .limit(pageable.getPageSize());
  }

  private Query queryStoredEventById(final StoredEvent storedEvent) {
    return new Query().addCriteria(Criteria.where("id").is(storedEvent.getId()));
  }

  private Flux<StoredEvent> addStoredEventLogging(final Flux<StoredEvent> storedEventFlux) {
    return storedEventFlux
        .doOnSubscribe(subscription -> LOGGER.trace().log("Stored Event NO-TX poll: subscribed!"))
        .doOnNext(
            storedEvent ->
                LOGGER.debug().log("Stored Event NO-TX poll: Found StoredEvent: {}", storedEvent))
        .doOnError(throwable -> LOGGER.error().log("Stored Event NO-TX poll ERROR", throwable));
  }
}
