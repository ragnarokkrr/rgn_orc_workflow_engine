package ragna.wf.orc.engine.domain.model.events;

import ragna.wf.orc.common.events.DomainEvent;
import ragna.wf.orc.engine.domain.model.WorkflowRoot;

public class WorkflowRootFinished extends DomainEvent {
  private WorkflowRootFinished() {}

  public WorkflowRootFinished(final WorkflowRoot workflowRoot) {
    super(workflowRoot, workflowRoot.getId());
  }

  public WorkflowRootFinished(Object source, String objectId, String action) {
    super(source, objectId, action);
  }
}
