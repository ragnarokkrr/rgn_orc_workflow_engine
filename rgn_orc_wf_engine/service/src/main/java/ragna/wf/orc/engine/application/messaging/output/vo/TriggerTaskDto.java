package ragna.wf.orc.engine.application.messaging.output.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TriggerTaskDto {
  private String requestId;
  private Integer order;
  private String taskType;
}
