package ragna.wf.orc.engine.domain.workflow.service.vo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FinishTaskCommand {
  @NotBlank private String workflowId;
  @NotNull private TaskType taskType;
  @NotNull private int order;
  @NotNull private Result result;

  public enum TaskType {
    ANALYSIS,
    DECISION
  }

  public enum Result {
    APPROVED,
    DISAPPROVED,
    FORWARDED,
    ERROR,
    WAITING_FOR_RESULT,
    RECOMMENDED;
  }
}
