package ragna.wf.orc.engine.infrastructure.storedevents.replay.main;

import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import ragna.wf.orc.common.events.DomainEvent;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.main.vo.MainReplayContextVo;
import reactor.core.publisher.Mono;

public interface MainStoredEventReplayerCallback<T extends DomainEvent> {
  Class<T> domainEvent();

  default MainReplayContextVo.MatchResultEnum match(final MainReplayContextVo mainReplayContextVo) {
    LogHolder.LOGGER.info().log("Default no-op match {}", mainReplayContextVo.getStoredEventVo());
    return MainReplayContextVo.MatchResultEnum.DEFAULT;
  }

  default Mono<MainReplayContextVo> doMatch(final MainReplayContextVo mainReplayContextVo) {
    LogHolder.LOGGER.info().log("Default no-op doMatch {}", mainReplayContextVo.getStoredEventVo());
    return Mono.just(mainReplayContextVo);
  }

  default Mono<MainReplayContextVo> doReplay(final MainReplayContextVo mainReplayContextVo) {
    LogHolder.LOGGER.info().log("Default no-op replay {}", mainReplayContextVo.getStoredEventVo());
    return Mono.just(mainReplayContextVo);
  }

  default Mono<MainReplayContextVo> publish(final MainReplayContextVo mainReplayContextVo) {
    LogHolder.LOGGER.info().log("Default no-op publish {}", mainReplayContextVo.getStoredEventVo());
    return Mono.just(mainReplayContextVo);
  }

  class LogHolder {
    private static final FluentLogger LOGGER =
        FluentLoggerFactory.getLogger(MainStoredEventReplayerCallback.class.getName());

    private LogHolder() {}
  }
}
