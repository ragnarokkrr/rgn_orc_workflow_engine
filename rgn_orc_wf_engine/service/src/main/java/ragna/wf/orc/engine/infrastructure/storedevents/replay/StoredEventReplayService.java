package ragna.wf.orc.engine.infrastructure.storedevents.replay;

import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.main.vo.MainReplayContextVo;
import ragna.wf.orc.eventstore.service.EventStoreStreamingService;
import ragna.wf.orc.eventstore.service.vo.StoredEventVo;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.time.Duration;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StoredEventReplayService {
  private static final FluentLogger LOGGER =
      FluentLoggerFactory.getLogger(StoredEventReplayService.class);
  private final EventStoreStreamingService eventStoreStreamingService;
  private final ReplayProcessor<MainReplayContextVo> mainReplayContextVoReplay;

  @PostConstruct
  void init() {
    eventStoreStreamingService
        .streamUnprocessedEvents()
        .map(this::dispatchEvent)
        .subscribeOn(Schedulers.newElastic("StoredEventReplay", 3))
        .delaySubscription(Duration.ofSeconds(30))
        .subscribe();
  }

  private StoredEventVo dispatchEvent(final StoredEventVo storedEventVo) {
    mainReplayContextVoReplay.onNext(MainReplayContextVo.createContext(storedEventVo));
    LOGGER.info().log("Stored Event {} dispatched to [mainReplayContextVoReplay]", storedEventVo);
    return storedEventVo;
  }
}
