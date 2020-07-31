package ragna.wf.orc.engine.domain.model.events;

import ragna.wf.orc.common.events.DomainEvent;
import ragna.wf.orc.engine.domain.model.WorkflowRoot;

public class WorkflowRootTaskTriggered extends DomainEvent {
    private WorkflowRootTaskTriggered() {
    }

    public WorkflowRootTaskTriggered(final WorkflowRoot workflowRoot) {
        super(workflowRoot, workflowRoot.getId());
    }

    public WorkflowRootTaskTriggered(Object source, String objectId, String action) {
        super(source, objectId, action);
    }
}
