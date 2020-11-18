package ragna.wf.orc.engine.infrastructure.storedevents.replay;

import java.time.Duration;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ragna.wf.orc.engine.infrastructure.config.DomainEventsConfigurationProperties;
import ragna.wf.orc.engine.infrastructure.config.FeatureTogglesConfigProperties;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.main.vo.MainReplayContextVo;
import ragna.wf.orc.eventstore.service.EventStoreStreamingService;
import ragna.wf.orc.eventstore.service.vo.StoredEventVo;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StoredEventReplayService {
  private static final FluentLogger LOGGER =
      FluentLoggerFactory.getLogger(StoredEventReplayService.class);
  private final EventStoreStreamingService eventStoreStreamingService;
  private final ReplayProcessor<MainReplayContextVo> mainReplayContextVoReplay;
  private final FeatureTogglesConfigProperties featureTogglesConfig;
  private final DomainEventsConfigurationProperties domainEventsConfigurationProperties;

  @PostConstruct
  void init() {
    final var replayInitialDelaySecs =
        domainEventsConfigurationProperties.getReplayInitialDelaySecs();
    if (!featureTogglesConfig.isReplayEngineEnabled()) {
      LOGGER.info().log("EVENT STORE STREAMING: DISABLED!");
      return;
    }
    LOGGER
        .info()
        .log(
            "EVENT STORE STREAMING: ENABLED! (wait {} secs until startup)", replayInitialDelaySecs);

    eventStoreStreamingService
        .streamUnprocessedEvents()
        .map(this::dispatchEvent)
        .subscribeOn(Schedulers.newElastic("StoredEventReplay", 3))
        .delaySubscription(Duration.ofSeconds(replayInitialDelaySecs))
        .subscribe();
  }

  private StoredEventVo dispatchEvent(final StoredEventVo storedEventVo) {
    mainReplayContextVoReplay.onNext(MainReplayContextVo.createContext(storedEventVo));
    LOGGER.info().log("Stored Event {} dispatched to [mainReplayContextVoReplay]", storedEventVo);
    return storedEventVo;
  }
}
