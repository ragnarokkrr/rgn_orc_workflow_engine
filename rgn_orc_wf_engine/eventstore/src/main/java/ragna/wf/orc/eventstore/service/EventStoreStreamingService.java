package ragna.wf.orc.eventstore.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ragna.wf.orc.eventstore.repository.StoreEventPollingRepository;
import ragna.wf.orc.eventstore.service.mappers.StoredEventMapper;
import ragna.wf.orc.eventstore.service.vo.StoredEventVo;
import reactor.core.publisher.Flux;
import reactor.util.retry.RetrySpec;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EventStoreStreamingService {
  private static final int DEFAULT_PAGE_SIZE = 10;
  private static final FluentLogger LOGGER =
      FluentLoggerFactory.getLogger(EventStoreStreamingService.class);
  private final StoreEventPollingRepository storeEventPollingRepository;
  private final EventSerializationDelegate eventSerializationDelegate;

  public Flux<StoredEventVo> streamUnprocessedEvents() {
    final var pageRequest = PageRequest.of(1, DEFAULT_PAGE_SIZE);

    return storeEventPollingRepository
        .findByStatusUnprocessedOrderByIdAscAndMarkAsProcessing(pageRequest)
        .retryWhen(RetrySpec.backoff(10, Duration.ofMillis(100)))
        .map(StoredEventMapper.INSTANCE::toService)
        .map(this::deserializeDomainEvent)
        .repeat();
  }

  private StoredEventVo deserializeDomainEvent(final StoredEventVo storedEventVo) {
    final var domainEvent =
        eventSerializationDelegate.deserialize(
            storedEventVo.getTypedName(), storedEventVo.getPayload());
    storedEventVo.setDomainEvent(domainEvent);
    return storedEventVo;
  }
}
