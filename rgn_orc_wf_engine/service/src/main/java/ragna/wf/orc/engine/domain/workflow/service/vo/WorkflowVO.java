package ragna.wf.orc.engine.domain.workflow.service.vo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Setter(AccessLevel.NONE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkflowVO {
    private String id;
    private String configurationId;
    private String customerId;
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
