package ragna.wf.orc.engine.infrastructure.storedevents.replay.main;

import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.vo.MainReplayContextVo;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.vo.SecondaryReplayContextVo;
import ragna.wf.orc.eventstore.service.EventStoreService;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MainReplayService {
  private static final FluentLogger LOGGER = FluentLoggerFactory.getLogger(MainReplayService.class);
  private final EventStoreService eventStoreService;
  private final ReplayProcessor<MainReplayContextVo> mainReplayContextVoReplayProcessor;
  private final ReplayProcessor<SecondaryReplayContextVo> sideReplayContextVoReplayProcessor;

  @PostConstruct
  void init() {
    mainReplayContextVoReplayProcessor
        .doOnNext(
            mainReplayContextVo ->
                LOGGER
                    .info()
                    .log(
                        "MainReplay => {}", mainReplayContextVo.getStoredEventVo().shortToString()))
        .flatMap(this::replay)
        .map(MainReplayContextVo::getStoredEventVo)
        .flatMap(eventStoreService::processed)
        .map(SecondaryReplayContextVo::createContext)
        .doOnNext(sideReplayContextVoReplayProcessor::onNext)
        .subscribeOn(Schedulers.newElastic("MainReplay", 3))
        .subscribe();
  }

  private Mono<MainReplayContextVo> replay(final MainReplayContextVo mainReplayContextVo) {
    return Mono.just(mainReplayContextVo);
  }
}
