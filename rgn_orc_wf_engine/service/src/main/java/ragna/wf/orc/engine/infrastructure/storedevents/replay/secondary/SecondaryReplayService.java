package ragna.wf.orc.engine.infrastructure.storedevents.replay.secondary;

import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.secondary.vo.SecondaryReplayContextVo;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SecondaryReplayService {
  private static final FluentLogger LOGGER =
      FluentLoggerFactory.getLogger(SecondaryReplayService.class);
  final ReplayProcessor<SecondaryReplayContextVo> secondaryReplayContextVoReplayProcessor;

  @PostConstruct
  void init() {
    secondaryReplayContextVoReplayProcessor
        .doOnNext(
            secondaryReplayContextVo ->
                LOGGER
                    .info()
                    .log("SecondaryReplay => {}", secondaryReplayContextVo.getStoredEventVo()))
        .subscribeOn(Schedulers.newElastic("SecondaryReplay", 3))
        .subscribe();
  }
}
