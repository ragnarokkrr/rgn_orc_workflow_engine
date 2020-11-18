package ragna.wf.orc.engine.domain.workflow.model;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@Setter(AccessLevel.NONE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExecutionPlan {
  @Builder.Default private LocalDateTime createdOn = LocalDateTime.now();
  private List<PlannedTask> plannedTasks;
}
