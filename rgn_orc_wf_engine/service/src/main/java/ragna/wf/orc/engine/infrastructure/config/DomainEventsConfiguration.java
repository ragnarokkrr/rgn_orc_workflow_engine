package ragna.wf.orc.engine.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ragna.wf.orc.common.events.DomainEvent;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.vo.MainReplayContextVo;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.vo.SecondaryReplayContextVo;
import reactor.core.publisher.ReplayProcessor;

@Configuration
public class DomainEventsConfiguration {

  @Bean
  ReplayProcessor<DomainEvent> domainEventReplay() {
    return ReplayProcessor.create();
  }

  @Bean
  ReplayProcessor<MainReplayContextVo> mainReplayContextVoReplay() {
    return ReplayProcessor.create();
  }

  @Bean
  ReplayProcessor<SecondaryReplayContextVo> sideReplayContextVoReplay() {
    return ReplayProcessor.create();
  }
}
