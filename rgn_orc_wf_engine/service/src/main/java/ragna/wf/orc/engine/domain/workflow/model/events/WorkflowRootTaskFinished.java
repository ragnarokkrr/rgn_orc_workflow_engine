package ragna.wf.orc.engine.domain.workflow.model.events;

import ragna.wf.orc.common.events.DomainEvent;
import ragna.wf.orc.engine.domain.workflow.model.WorkflowRoot;

public class WorkflowRootTaskFinished extends DomainEvent {
  private WorkflowRootTaskFinished() {}

  public WorkflowRootTaskFinished(final WorkflowRoot workflowRoot) {
    super(workflowRoot, workflowRoot.getId());
  }

  public WorkflowRootTaskFinished(WorkflowRoot workflowRoot, String objectId, String action) {
    super(workflowRoot, objectId, action);
  }
}
