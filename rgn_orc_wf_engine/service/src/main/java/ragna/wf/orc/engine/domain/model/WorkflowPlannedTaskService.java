package ragna.wf.orc.engine.domain.model;

import ragna.wf.orc.common.exceptions.ErrorCode;
import ragna.wf.orc.common.exceptions.OrcIllegalStateException;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

final class WorkflowPlannedTaskService {
  static boolean onlyPlannedStatuses(final WorkflowRoot workflowRoot) {
    return workflowRoot.getExecutionPlan().getPlannedTasks().stream()
        .map(PlannedTask::getStatus)
        .allMatch(status -> Objects.equals(status, PlannedTask.Status.PLANNED));
  }

  static PlannedTask findFirst(final WorkflowRoot workflowRoot, final String action) {
    final var message =
        WorkflowUtils.fornatedMessage(workflowRoot, "Can't find first planned task", action);
    return workflowRoot.getExecutionPlan().getPlannedTasks().stream()
        .min(Comparator.comparing(PlannedTask::getOrder))
        .orElseThrow(
            () -> new OrcIllegalStateException(message, ErrorCode.CANT_TRIGGER_TASK_INVALID_STATE));
  }

  public static PlannedTask findTask(
      final WorkflowRoot workflowRoot,
      final TaskType taskType,
      final int order,
      final String action) {
    final var message =
        WorkflowUtils.fornatedMessage(workflowRoot, "Can't find planned task  ", action);

    return workflowRoot.getExecutionPlan().getPlannedTasks().stream()
        .filter(
            plannedTask ->
                Objects.equals(taskType, plannedTask.getTaskType())
                    && order == plannedTask.getOrder())
        .findFirst()
        .orElseThrow(
            () -> new OrcIllegalStateException(message, ErrorCode.CANT_FINISH_TASK_INVALID_STATE));
  }

  public static PlannedTask findTaskToFinish(
      final WorkflowRoot workflowRoot,
      final TaskType taskType,
      final int order,
      final String action) {
    final var message =
        WorkflowUtils.fornatedMessage(workflowRoot, "Can't find planned task to finish ", action);

    return workflowRoot.getExecutionPlan().getPlannedTasks().stream()
        .filter(
            plannedTask ->
                Objects.equals(taskType, plannedTask.getTaskType())
                    && order == plannedTask.getOrder())
        .findFirst()
        .orElseThrow(
            () -> new OrcIllegalStateException(message, ErrorCode.CANT_FINISH_TASK_INVALID_STATE));
  }

  public static Optional<PlannedTask> findNextPlannedTask(final WorkflowRoot workflowRoot) {
    return workflowRoot.getExecutionPlan().getPlannedTasks().stream()
        .filter(plannedTask -> Objects.equals(PlannedTask.Status.PLANNED, plannedTask.getStatus()))
        .min(Comparator.comparing(PlannedTask::getOrder));
  }

  static PlannedTask findTaskToTrigger(
      final WorkflowRoot workflowRoot,
      final TaskType taskType,
      final int order,
      final String action) {
    final var message =
        WorkflowUtils.fornatedMessage(workflowRoot, "Can't find planned task to trigger", action);
    return workflowRoot.getExecutionPlan().getPlannedTasks().stream()
        .filter(
            plannedTask ->
                Objects.equals(taskType, plannedTask.getTaskType())
                    && order == plannedTask.getOrder())
        .findFirst()
        .orElseThrow(() -> new OrcIllegalStateException(message, ErrorCode.TASK_NOT_FOUND));
  }

  static List<PlannedTask> sortByExecutionOrder(final WorkflowRoot workflowRoot) {
    return workflowRoot.getExecutionPlan().getPlannedTasks().stream()
        .sorted(Comparator.comparing(PlannedTask::getOrder))
        .collect(Collectors.toList());
  }

  static PlannedTask findLastTask(final WorkflowRoot workflowRoot, final String action) {
    final var message =
        WorkflowUtils.fornatedMessage(workflowRoot, "Can't find planned task to trigger", action);

    return workflowRoot.getExecutionPlan().getPlannedTasks().stream()
        .max(Comparator.comparing(PlannedTask::getOrder))
        .orElseThrow(() -> new OrcIllegalStateException(message, ErrorCode.TASK_NOT_FOUND));
  }

  static boolean anyTaskDisapproved(final WorkflowRoot workflowRoot) {
    return workflowRoot.getExecutionPlan().getPlannedTasks().stream()
        .map(PlannedTask::getResult)
        .anyMatch(PlannedTask.Result::isDisapproved);
  }

  static boolean allTasksApproved(final WorkflowRoot workflowRoot) {
    return workflowRoot.getExecutionPlan().getPlannedTasks().stream()
        .map(PlannedTask::getResult)
        .allMatch(PlannedTask.Result::isApproved);
  }

  private WorkflowPlannedTaskService() {}
}
