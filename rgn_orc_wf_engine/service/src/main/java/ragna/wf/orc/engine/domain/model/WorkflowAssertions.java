package ragna.wf.orc.engine.domain.model;

public interface WorkflowAssertions {
    interface StateAssertion {
        void assertState(WorkflowRoot WorkflowRoot, String action);
    }
}
