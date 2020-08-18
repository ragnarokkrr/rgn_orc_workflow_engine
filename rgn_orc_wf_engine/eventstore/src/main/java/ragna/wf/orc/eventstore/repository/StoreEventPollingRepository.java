package ragna.wf.orc.eventstore.repository;

import ragna.wf.orc.eventstore.model.StoredEvent;
import reactor.core.publisher.Flux;

public interface StoreEventPollingRepository {

    Flux<StoredEvent> findByStatusUnprocessedOrder();
}
