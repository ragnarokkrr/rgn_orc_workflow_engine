package ragna.wf.orc.engine.infrastructure.storedevents.replay.main;

import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import ragna.wf.orc.common.events.DomainEvent;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.main.vo.MainReplayContextVo;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;

public interface MainStoredEventReplayerCallback<T extends DomainEvent> {
  default Class<T> domainEventType() {
    return (Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[0];
  }

  default Mono<MainReplayContextVo> activateTaskIfConfigured(final MainReplayContextVo mainReplayContextVo) {
    LogHolder.LOGGER.info().log("Default no-op match {}", mainReplayContextVo.getStoredEventVo());
    return Mono.just(mainReplayContextVo.matchResult(MainReplayContextVo.MatchResult.builder()
            .matchResultType(MainReplayContextVo.MatchResultEnum.DEFAULT)
            .build()));
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
