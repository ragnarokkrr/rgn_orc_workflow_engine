package ragna.wf.orc.eventstore.repository;

import org.springframework.data.domain.Pageable;
import ragna.wf.orc.eventstore.model.StoredEvent;
import reactor.core.publisher.Flux;

public interface StoreEventPollingRepository {

  Flux<StoredEvent> findByStatusUnprocessedOrderByIdAscAndMarkAsProcessing(Pageable pageable);
}
