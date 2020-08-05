package ragna.wf.orc.engine.domain.model;

final class WorkflowUtils {
  static String fornatedMessage(WorkflowRoot workflowRoot, String action) {
    return String.format(
        " (action=%s) (workflowId=%s, status=%s, customerRequest=%s)",
        action, workflowRoot.getId(), workflowRoot.getStatus(), workflowRoot.getCustomerRequest());
  }

  static String fornatedMessage(WorkflowRoot workflowRoot, String prefix, String action) {
    return String.format(
        "%s. (action=%s) (workflowId=%s, status=%s, customerRequest=%s)",
        prefix,
        action,
        workflowRoot.getId(),
        workflowRoot.getStatus(),
        workflowRoot.getCustomerRequest());
  }

  private WorkflowUtils() {}
}
