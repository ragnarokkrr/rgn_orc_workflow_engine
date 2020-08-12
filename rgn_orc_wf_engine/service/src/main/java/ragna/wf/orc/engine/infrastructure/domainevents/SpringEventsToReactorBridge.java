package ragna.wf.orc.engine.infrastructure.domainevents;

import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ragna.wf.orc.common.events.DomainEvent;
import ragna.wf.orc.common.events.spring.ApplicationEventWrapper;
import reactor.core.publisher.ReplayProcessor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SpringEventsToReactorBridge {
  private static final FluentLogger LOGGER =
      FluentLoggerFactory.getLogger(SpringEventsToReactorBridge.class);

  private final ReplayProcessor<DomainEvent> domainEventReplayProcessor;

  @EventListener()
  public void heandleApplicationEvent(final ApplicationEventWrapper applicationEventWrapper) {
    final var domainEvent = applicationEventWrapper.unwrap();
    LOGGER.info().log("Publishing domain event: {} ", domainEvent);
    domainEventReplayProcessor.onNext(domainEvent);
  }
}
