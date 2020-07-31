package ragna.wf.orc.engine.domain.model.events;

import ragna.wf.orc.common.events.DomainEvent;
import ragna.wf.orc.engine.domain.model.WorkflowRoot;

public class WorkflowRootTaskFinished extends DomainEvent {
    private WorkflowRootTaskFinished() {
    }

    public WorkflowRootTaskFinished(final WorkflowRoot workflowRoot) {
        super(workflowRoot, workflowRoot.getId());
    }

    public WorkflowRootTaskFinished(Object source, String objectId, String action) {
        super(source, objectId, action);
    }
}
