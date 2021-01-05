package ragna.wf.orc.engine.application.messaging.output;

import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.stereotype.Component;
import ragna.wf.orc.engine.application.messaging.output.vo.TriggerTaskDto;

@Component
public class TriggerTaskMessageProducer {
  private static final FluentLogger LOGGER =
      FluentLoggerFactory.getLogger(TriggerTaskMessageProducer.class);

  public TriggerTaskDto send(final TriggerTaskDto triggerTaskDto) {
    LOGGER.info().log("Sending message MOCK: {}", triggerTaskDto);
    return triggerTaskDto;
  }
}
