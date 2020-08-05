package ragna.wf.orc.engine.domain.model.events;

import ragna.wf.orc.common.events.DomainEvent;
import ragna.wf.orc.engine.domain.model.WorkflowRoot;

public class WorkflowRootCreated extends DomainEvent {
  private WorkflowRootCreated() {}

  public WorkflowRootCreated(final WorkflowRoot workflowRoot) {
    super(workflowRoot, workflowRoot.getId());
  }

  public WorkflowRootCreated(Object source, String objectId, String action) {
    super(source, objectId, action);
  }
}
