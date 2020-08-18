package ragna.wf.orc.eventstore.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import ragna.wf.orc.eventstore.model.StoredEvent;
import reactor.core.publisher.Flux;

@Repository
public interface StoredEventRepository extends ReactiveMongoRepository<StoredEvent, Long> {
  @Query(value = "{'eventStatus': {$eq: 'UNPROCESSED'} }", sort = "{'id': 1}")
  Flux<StoredEvent> findByStatusUnprocessedOrderByIdAsc(final Pageable pageable);

  @Query("{'id': { $gte: ?0, $lte: ?1 }}")
  Flux<StoredEvent> findByEventIdBetween(final long low, final long high);

  // TODO wait for stored event id seq fix
  Flux<StoredEvent> findByObjectIdOrderByOccurredOnAsc(final String objectId);
}
