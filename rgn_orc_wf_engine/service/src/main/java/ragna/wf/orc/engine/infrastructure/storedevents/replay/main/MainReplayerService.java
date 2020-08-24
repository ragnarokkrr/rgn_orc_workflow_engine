package ragna.wf.orc.engine.infrastructure.storedevents.replay.main;

import ragna.wf.orc.common.events.DomainEvent;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.main.vo.MainReplayContextVo;
import reactor.core.publisher.Mono;

public interface MainReplayerService<T extends DomainEvent> {
  T domainEvent();

  Mono<MainReplayContextVo> replay(MainReplayContextVo mainReplayContextVo);
}
