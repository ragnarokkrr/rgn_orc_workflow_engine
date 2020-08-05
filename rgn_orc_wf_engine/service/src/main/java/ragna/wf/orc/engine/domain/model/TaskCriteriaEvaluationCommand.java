package ragna.wf.orc.engine.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Data
@Setter(AccessLevel.NONE)
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TaskCriteriaEvaluationCommand {
  public static final String NO_ERROR = StringUtils.EMPTY;
  private String id;
  private String value;
  private TaskCriteriaEvaluationCommand.Result result;
  private TaskCriteriaEvaluationCommand.Status status;
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
