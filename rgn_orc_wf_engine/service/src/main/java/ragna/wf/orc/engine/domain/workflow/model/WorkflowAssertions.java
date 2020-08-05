package ragna.wf.orc.engine.domain.workflow.model;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import ragna.wf.orc.common.exceptions.ErrorCode;
import ragna.wf.orc.common.exceptions.OrcIllegalStateException;

import java.util.Objects;

interface WorkflowAssertions {
  interface StateAssertion {
    void assertState(WorkflowRoot WorkflowRoot, String action);
  }
}

enum WorkflowStateAssertions implements WorkflowAssertions.StateAssertion {
  WORKFLOW_ALREADY_CONFIGURED(Strategy.WORKFLOW_ALREADY_CONFIGURED),
  EXECUTION_PLAN_REQUIREMENTS(Strategy.EXECUTION_PLAN_REQUIREMENTS),
  CONFIGURATION_REQUIREMENTS(Strategy.CONFIGURATION_REQUIREMENTS),

  // OrcException Throwing Assertionts
  NO_TASK_TRIGGER_VALID_STATUSES(Strategy.NO_TASK_TRIGGER_VALID_STATUSES),
  TASK_FINISH_VALID_STATUS(Strategy.TASK_FINISH_VALID_STATUS);

  private WorkflowAssertions.StateAssertion delegate;

  WorkflowStateAssertions(WorkflowAssertions.StateAssertion delegate) {
    this.delegate = delegate;
  }

  @Override
  public void assertState(WorkflowRoot WorkflowRoot, String action) {
    final var myAction = action == null ? StringUtils.EMPTY : action;
    delegate.assertState(WorkflowRoot, myAction);
  }

  private static class Strategy {
    static final WorkflowAssertions.StateAssertion WORKFLOW_ALREADY_CONFIGURED =
        (workflowRoot, action) ->
            Assert.state(
                Objects.isNull(workflowRoot.getConfiguration()),
                String.format(
                    "Workflow already configured (%s, %s)",
                    workflowRoot.getId(), workflowRoot.getCustomerRequest()));

    static final WorkflowAssertions.StateAssertion EXECUTION_PLAN_REQUIREMENTS =
        (workflowRoot, action) ->
            Assert.state(
                Objects.nonNull(workflowRoot.getConfiguration())
                    && WorkflowStatus.INSTANTIATED == workflowRoot.getStatus(),
                String.format(
                    "Execution Plan requirements invalid (configuration=%s, status=%s). Request: %s",
                    !Objects.isNull(workflowRoot.getConfiguration()),
                    workflowRoot.getStatus(),
                    workflowRoot.getCustomerRequest()));

    static final WorkflowAssertions.StateAssertion CONFIGURATION_REQUIREMENTS =
        (workflowRoot, action) ->
            Assert.state(
                Objects.nonNull(workflowRoot.getConfiguration())
                    && Objects.nonNull(workflowRoot.getExecutionPlan())
                    && WorkflowStatus.INSTANTIATED == workflowRoot.getStatus(),
                String.format(
                    "Configuration requirements invalid (configuration=%s, status=%s). Request: %s",
                    Objects.nonNull(workflowRoot.getConfiguration()),
                    workflowRoot.getStatus(),
                    workflowRoot.getCustomerRequest()));
    // Orc Exception assertions
    static final WorkflowAssertions.StateAssertion NO_TASK_TRIGGER_VALID_STATUSES =
        (workflowRoot, action) -> {
          if (!WorkflowConstants.TASK_TRIGGER_VALID_STATUSES.contains(workflowRoot.getStatus())) {
            final var message = WorkflowUtils.fornatedMessage(workflowRoot, action);
            throw new OrcIllegalStateException(message, ErrorCode.CANT_TRIGGER_TASK_INVALID_STATE);
          }
        };

    static final WorkflowAssertions.StateAssertion TASK_FINISH_VALID_STATUS =
        (workflowRoot, action) -> {
          if (!WorkflowConstants.TASK_TRIGGER_VALID_STATUSES.contains(workflowRoot.getStatus())) {
            final var message = WorkflowUtils.fornatedMessage(workflowRoot, action);
            throw new OrcIllegalStateException(message, ErrorCode.CANT_FINISH_TASK_INVALID_STATE);
          }
        };
  }
}
