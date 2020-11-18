package ragna.wf.orc.engine.application.rest.workflow.dto;

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
