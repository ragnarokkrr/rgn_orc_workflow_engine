package ragna.wf.orc.engine.domain.workflow.service.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TriggerFirstTaskCommand {
  @NotBlank private String workflowId;
}
