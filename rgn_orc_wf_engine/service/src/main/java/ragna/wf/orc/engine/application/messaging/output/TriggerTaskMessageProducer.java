package ragna.wf.orc.engine.application.messaging.output;

import org.springframework.stereotype.Component;
import ragna.wf.orc.engine.application.messaging.output.vo.TriggerTaskDto;

@Component
public class TriggerTaskMessageProducer {
  public TriggerTaskDto send(final TriggerTaskDto triggerTaskDto) {
    return triggerTaskDto;
  }
}
