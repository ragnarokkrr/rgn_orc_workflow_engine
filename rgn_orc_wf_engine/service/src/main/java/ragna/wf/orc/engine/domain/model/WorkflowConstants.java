package ragna.wf.orc.engine.domain.model;

import java.util.Set;

final class WorkflowConstants {
  static final Set<WorkflowStatus> TASK_TRIGGER_VALID_STATUSES =
      Set.of(WorkflowStatus.CONFIGURED, WorkflowStatus.ORCHESTRATING);

  static final Set<WorkflowStatus> TASK_FINISH_VALID_STATUSES =
      Set.of(WorkflowStatus.ORCHESTRATING);

  private WorkflowConstants() {}
}
