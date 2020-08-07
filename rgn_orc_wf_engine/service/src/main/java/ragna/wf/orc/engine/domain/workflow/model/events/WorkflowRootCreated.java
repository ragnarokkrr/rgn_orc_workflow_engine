package ragna.wf.orc.engine.domain.workflow.model.events;

import ragna.wf.orc.common.events.DomainEvent;
import ragna.wf.orc.engine.domain.workflow.model.WorkflowRoot;

public class WorkflowRootCreated extends DomainEvent {
  private WorkflowRootCreated() {
    super();
  }

  public WorkflowRootCreated(WorkflowRoot workflowRoot, String objectId, String action) {
    super(workflowRoot, objectId, action);
  }
}
