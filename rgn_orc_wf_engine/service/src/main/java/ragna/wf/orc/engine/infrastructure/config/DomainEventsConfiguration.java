package ragna.wf.orc.engine.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ragna.wf.orc.common.events.DomainEvent;
import reactor.core.publisher.ReplayProcessor;

@Configuration
public class DomainEventsConfiguration {

  @Bean
  ReplayProcessor<DomainEvent> domainEventReplay() {
    return ReplayProcessor.create();
  }
}
