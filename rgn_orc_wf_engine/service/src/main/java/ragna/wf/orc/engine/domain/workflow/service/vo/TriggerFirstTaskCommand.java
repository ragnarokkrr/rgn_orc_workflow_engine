package ragna.wf.orc.engine.domain.workflow.service.vo;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TriggerFirstTaskCommand {
  @NotBlank private String workflowId;
}
