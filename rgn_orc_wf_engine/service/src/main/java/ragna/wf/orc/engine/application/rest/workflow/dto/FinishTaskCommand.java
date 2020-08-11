package ragna.wf.orc.engine.application.rest.workflow.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Data
@Setter(AccessLevel.NONE)
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FinishTaskCommand {
  // @NotBlank private String workflowId;
  @NotNull
  private TaskType taskType;
  @NotNull
  private int order;
  @NotNull
  private Result result;

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
