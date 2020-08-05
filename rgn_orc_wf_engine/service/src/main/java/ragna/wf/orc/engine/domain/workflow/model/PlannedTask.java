package ragna.wf.orc.engine.domain.workflow.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Setter(AccessLevel.NONE)
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PlannedTask {
  private TaskType taskType;
  private int order;
  @Builder.Default private Result result = Result.WAITING_FOR_RESULT;
  @Builder.Default private Status status = Status.PLANNED;
  @Builder.Default private Map<String, TaskCriteriaResult> taskCriteriaResult = new HashMap<>();
  private LocalDateTime triggeredOn;
  private LocalDateTime evaluatedOn;
  private LocalDateTime finishedOn;

  public PlannedTask trigger() {
    this.triggeredOn = LocalDateTime.now();
    this.status = Status.TRIGGERED;
    return this;
  }

  public PlannedTask finish(Result result) {
    this.result = result;
    this.status = Status.CONCLUDED;
    this.finishedOn = LocalDateTime.now();
    return this;
  }

  public PlannedTask addTaskCriteriaEvaluation(final List<TaskCriteriaResult> taskCriteriaResult) {
    this.taskCriteriaResult =
        taskCriteriaResult.stream()
            .collect(
                Collectors.toMap(
                    PlannedTask.TaskCriteriaResult::getId,
                    taskCriteriaResult1 -> taskCriteriaResult1));
    this.evaluatedOn = LocalDateTime.now();
    return this;
  }

  public enum Result {
    APPROVED,
    DISAPPROVED,
    FORWARDED,
    ERROR,
    WAITING_FOR_RESULT,
    RECOMMENDED;

    private static final Set<Result> DISAPPROVED_RESULTS = Set.of(DISAPPROVED);
    private static final Set<Result> APPROVED_RESULTS = Set.of(APPROVED, RECOMMENDED, FORWARDED);

    public boolean isDisapproved() {
      return DISAPPROVED_RESULTS.contains(this);
    }

    public boolean isApproved() {
      return APPROVED_RESULTS.contains(this);
    }
  }

  public enum Status {
    PLANNED,
    TRIGGERED,
    CONCLUDED,
    CANCELLED,
    ERROR;
  }

  @Data
  @Builder(toBuilder = true)
  @Setter(AccessLevel.NONE)
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static class TaskCriteriaResult {
    public static final String NO_ERROR = StringUtils.EMPTY;
    private String id;
    private String value;
    private TaskCriteriaResult.Result result;
    private TaskCriteriaResult.Status status;
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
