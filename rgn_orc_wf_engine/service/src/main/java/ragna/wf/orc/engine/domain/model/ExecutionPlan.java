package ragna.wf.orc.engine.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Setter(AccessLevel.NONE)
public class ExecutionPlan {
    @Builder.Default
    private LocalDateTime createdOn = LocalDateTime.now();
    private List<PlannedTask> plannedTasks;
}
