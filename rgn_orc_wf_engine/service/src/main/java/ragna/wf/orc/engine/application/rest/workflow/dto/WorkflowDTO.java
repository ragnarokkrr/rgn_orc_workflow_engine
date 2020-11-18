package ragna.wf.orc.engine.application.rest.workflow.dto;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class WorkflowDTO {
  private String id;
  private String configurationId;
  private String customerId;
  private String customerRequestId;
  private Result result;
  private Status status;
  private LocalDateTime createdOn;
  private LocalDateTime updatedOn;

  public enum Result {
    WORKFLOW_ONGOING,
    APPROVED,
    DISAPPROVED,
    UNKNOWN_RESULT,
    ERROR
  }

  public enum Status {
    INSTANTIATED,
    CONFIGURED,
    ORCHESTRATING,
    FINISHED,
    CANCELLED
  }
}
