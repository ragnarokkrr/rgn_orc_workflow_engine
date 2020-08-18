package ragna.wf.orc.engine.application.rest.workflow.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Setter(AccessLevel.NONE)
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RegisterTaskResultsCommand {
  // @NotBlank private String workflowId;
  @NotNull private TaskType taskType;
  @NotNull private int order;
  @NotNull private List<TaskCriteriaResult> result;

  public enum TaskType {
    ANALYSIS,
    DECISION
  }

  @Data
  @Builder(toBuilder = true)
  @Setter(AccessLevel.NONE)
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static class TaskCriteriaResult {
    public static final String NO_ERROR = StringUtils.EMPTY;
    @NotNull private String id;
    @NotNull private String value;
    @NotNull private TaskCriteriaResult.Result result;
    @NotNull private TaskCriteriaResult.Status status;
    @Builder.Default private String error = NO_ERROR;

    public enum Result {
      APPROVED,
      DISAPPROVED,
      ERROR
    }

    public enum Status {
      UNMATCHED,
      MATCHED,
      PUBLISHED,
      ERROR
    }
  }
}
