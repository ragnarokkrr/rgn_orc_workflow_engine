package ragna.wf.orc.engine.domain.workflow.service;

import java.util.Set;
import javax.validation.ConstraintViolation;
import ragna.wf.orc.common.exceptions.ErrorCode;
import ragna.wf.orc.common.exceptions.OrcInvalidArgument;
import ragna.wf.orc.common.exceptions.OrcNotFoundException;

class WorkflowExceptionFactory {
  private WorkflowExceptionFactory() {}

  static <E> OrcInvalidArgument newInvalidArgumentException(
      final Object command, final Set<ConstraintViolation<E>> constraintViolations) {
    return new OrcInvalidArgument(
        String.format("invalid command=%s, violations=%s", command, constraintViolations));
  }

  static OrcNotFoundException newOrcNotFoundException(final String workflowId) {

    return new OrcNotFoundException(
        String.format("%s: Workflow '%s' not found", ErrorCode.WORKFLOW_NOT_FOUND, workflowId),
        ErrorCode.WORKFLOW_NOT_FOUND);
  }
}
