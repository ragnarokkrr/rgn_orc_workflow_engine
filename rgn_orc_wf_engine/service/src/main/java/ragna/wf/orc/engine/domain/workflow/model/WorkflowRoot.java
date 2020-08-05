package ragna.wf.orc.engine.domain.workflow.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.Assert;
import ragna.wf.orc.common.events.DomainEvent;
import ragna.wf.orc.common.events.spring.ApplicationEventWrapper;
import ragna.wf.orc.common.exceptions.ErrorCode;
import ragna.wf.orc.common.exceptions.OrcIllegalStateException;
import ragna.wf.orc.engine.domain.workflow.model.events.WorkflowRootCreated;
import ragna.wf.orc.engine.domain.workflow.model.events.WorkflowRootFinished;
import ragna.wf.orc.engine.domain.workflow.model.events.WorkflowRootTaskFinished;
import ragna.wf.orc.engine.domain.workflow.model.events.WorkflowRootTaskTriggered;
import ragna.wf.orc.engine.domain.workflow.model.mapper.TaskCriteriaMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Document(collection = "workflows")
@Data
@Setter(AccessLevel.NONE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkflowRoot {
  private static final FluentLogger LOGGER = FluentLoggerFactory.getLogger(WorkflowRoot.class);
  private static final String TRIGGER_FIRST_TASK = "triggerFirstTask()";
  private static final int PLANNED_TASK_LIST_OFFSET = 2;

  private final Collection<DomainEvent> domainEvents = new ArrayList<>();

  @Id
  private String id;
  private CustomerRequest customerRequest;
  private Configuration configuration;
  private ExecutionPlan executionPlan;
  private WorkflowStatus status;
  private WorkflowResult result;
  private LocalDateTime createdOn;
  private LocalDateTime updatedOn;

  public static WorkflowRoot createWorkflowRoot(final CustomerRequest customerRequest) {
    Assert.notNull(customerRequest, "customerRequest must not be null");
    final var workflowRoot = new WorkflowRoot();
    workflowRoot.customerRequest = customerRequest.toBuilder().build();
    workflowRoot.id = UUID.randomUUID().toString();
    workflowRoot.status = WorkflowStatus.INSTANTIATED;
    workflowRoot.result = WorkflowResult.WORKFLOW_ONGOING;
    workflowRoot.createdOn = LocalDateTime.now();
    workflowRoot.updatedOn = LocalDateTime.now();
    return workflowRoot;
  }

  public WorkflowRoot addWorkflowConfiguration(final Configuration configuration) {
    WorkflowStateAssertions.WORKFLOW_ALREADY_CONFIGURED.assertState(
        this, "addWorkflowConfiguration()");
    this.configuration = configuration.toBuilder().build();
    return this;
  }

  public WorkflowRoot createExecutionPlan() {
    WorkflowStateAssertions.EXECUTION_PLAN_REQUIREMENTS.assertState(this, "createExecutionPlan()");

    final var plannedTaskList =
        configuration.getConfiguredTasks().stream()
            .map(
                configuredTask ->
                    PlannedTask.builder()
                        .order(configuredTask.getOrder())
                        .taskType(configuredTask.getTaskType())
                        .build())
            .collect(Collectors.toList());

    this.executionPlan =
        ExecutionPlan.builder()
            .plannedTasks(plannedTaskList)
            .createdOn(LocalDateTime.now())
            .build();
    return this;
  }

  public WorkflowRoot configured() {
    WorkflowStateAssertions.CONFIGURATION_REQUIREMENTS.assertState(this, "configured()");

    this.status = WorkflowStatus.CONFIGURED;
    registerEvent(new WorkflowRootCreated(this, this.id, "configured()"));
    return this;
  }

  public WorkflowRoot triggerFirstTask() {
    LOGGER
        .debug()
        .log(WorkflowUtils.fornatedMessage(this, "Triggering first task ", TRIGGER_FIRST_TASK));
    WorkflowStateAssertions.NO_TASK_TRIGGER_VALID_STATUSES.assertState(this, TRIGGER_FIRST_TASK);
    if (!WorkflowPlannedTaskService.onlyPlannedStatuses(this)) {
      final var message = WorkflowUtils.fornatedMessage(this, TRIGGER_FIRST_TASK);
      throw new OrcIllegalStateException(
          message, ErrorCode.CANT_TRIGGER_FIRST_TASK_ALL_TASKS_SHOULD_BE_PLANNED);
    }
    final var firstTask = WorkflowPlannedTaskService.findFirst(this, TRIGGER_FIRST_TASK);
    firstTask.trigger();
    this.status = WorkflowStatus.ORCHESTRATING;
    registerEvent(new WorkflowRootTaskTriggered(this, this.id, "triggerFirstTask()"));
    return this;
  }

  public WorkflowRoot addTaskCriteriaEvaluationResults(
      final TaskType taskType,
      final int order,
      final List<TaskCriteriaEvaluationCommand> taskCriteriaEvaluationCommands) {
    LOGGER
        .debug()
        .log(
            WorkflowUtils.fornatedMessage(
                this, "Adding criteria evaluation to task", "addTaskCriteriaEvaluationResults"));
    final var plannedTask =
        WorkflowPlannedTaskService.findTask(
            this, taskType, order, "addTaskCriteriaEvaluationResults()");

    final var taskCriteriaResultList =
        taskCriteriaEvaluationCommands.stream()
            .map(TaskCriteriaMapper.INSTANCE::toModel)
            .collect(Collectors.toList());
    plannedTask.addTaskCriteriaEvaluation(taskCriteriaResultList);
    return this;
  }

  public WorkflowRoot finishTaskAndAdvance(
      TaskType taskType, int order, PlannedTask.Result result) {
    LOGGER.debug().log(WorkflowUtils.fornatedMessage(this, "Finishing task and then advance"));
    finishTask(taskType, order, result);
    return advanceIfAnyTaskRemains();
  }

  WorkflowRoot triggerTask(TaskType taskType, int order) {
    // TODO status assertion
    final var taskToTrigger =
        WorkflowPlannedTaskService.findTaskToTrigger(this, taskType, order, "triggerTask()");
    if (!Objects.equals(taskToTrigger.getStatus(), PlannedTask.Status.PLANNED)) {
      final var message = WorkflowUtils.fornatedMessage(this, "triggerTask()");
      throw new OrcIllegalStateException(message, ErrorCode.INVALID_STATE_TO_RIGGER_TASK);
    }

    final var firstTask = WorkflowPlannedTaskService.findFirst(this, "triggerTask()");

    if (Objects.equals(taskToTrigger, firstTask)) {
      final var message = WorkflowUtils.fornatedMessage(this, "triggerTask()");
      throw new OrcIllegalStateException(
          message, ErrorCode.TRIED_TO_TRIGGER_FIRST_TASK_IN_WRONG_PLACE);
    }
    final var previousTaskOrder = taskToTrigger.getOrder() - PLANNED_TASK_LIST_OFFSET;
    final var sortedTasksByExecutionOrder = WorkflowPlannedTaskService.sortByExecutionOrder(this);
    final var previousTask = sortedTasksByExecutionOrder.get(previousTaskOrder);

    if (!Objects.equals(previousTask.getStatus(), PlannedTask.Status.CONCLUDED)) {
      LOGGER
          .debug()
          .log(
              "triggerTask() -> PreviousTask is not concluded, so current task will not be triggered"
                  + "previousTask(taskId={}, order={}) taskToTrigger(taskId={}, order={}) (workflowId={})",
              previousTask.getTaskType(),
              previousTask.getOrder(),
              taskToTrigger.getTaskType(),
              taskToTrigger.getOrder(),
              id);
      return this;
    }

    taskToTrigger.trigger();

    this.status = WorkflowStatus.ORCHESTRATING;
    registerEvent(new WorkflowRootTaskTriggered(this, this.id, "triggerTask()"));
    return this;
  }

  private WorkflowRoot finishTask(TaskType taskType, int order, PlannedTask.Result result) {
    WorkflowStateAssertions.TASK_FINISH_VALID_STATUS.assertState(this, "finishTask()");
    final var taskToFinish =
        WorkflowPlannedTaskService.findTaskToFinish(this, taskType, order, "finishTask()");

    if (!Objects.equals(taskToFinish.getStatus(), PlannedTask.Status.TRIGGERED)) {
      final var message = WorkflowUtils.fornatedMessage(this, "finishTask()");
      throw new OrcIllegalStateException(message, ErrorCode.CANT_FINISH_TASK_INVALID_TASK_STATE);
    }
    taskToFinish.finish(result);

    this.status = WorkflowStatus.ORCHESTRATING;
    registerEvent(new WorkflowRootTaskFinished(this, this.id, "finishTask()"));
    finishWorkflowIfConclusionStateAchieved(taskToFinish);
    return this;
  }

  private WorkflowRoot finishWorkflowIfConclusionStateAchieved(final PlannedTask currentTask) {
    final var lastTask =
        WorkflowPlannedTaskService.findLastTask(this, "finishWorkflowIfConclusionStateAchieved()");
    final var isWorkFinished = Objects.equals(currentTask, lastTask);

    if (isWorkFinished) {
      status = WorkflowStatus.FINISHED;
      evaluateWorkFlowResult();
      registerEvent(new WorkflowRootFinished(this, this.id, "finishWorkflowIfConclusionStateAchieved()"));
    }

    return this;
  }

  private void evaluateWorkFlowResult() {
    // TODO assert state

    if (WorkflowPlannedTaskService.anyTaskDisapproved(this)) {
      result = WorkflowResult.DISAPPROVED;
      return;
    }

    if (WorkflowPlannedTaskService.allTasksApproved(this)) {
      result = WorkflowResult.APPROVED;
      return;
    }

    result = WorkflowResult.UNKNOWN_RESULT;
  }

  private WorkflowRoot advanceIfAnyTaskRemains() {
    final var nextPlannedTaskOptional = WorkflowPlannedTaskService.findNextPlannedTask(this);
    if (nextPlannedTaskOptional.isEmpty()) {
      LOGGER
          .debug()
          .log(
              "No more tasks to trigger.",
              WorkflowUtils.fornatedMessage(this, "advanceIfAnyTaskRemains"));
      return this;
    }

    final var nextPlannedTask = nextPlannedTaskOptional.get();

    return triggerTask(nextPlannedTask.getTaskType(), nextPlannedTask.getOrder());
  }

  private void registerEvent(final DomainEvent domainEvent) {
    this.updatedOn = LocalDateTime.now();
    domainEvents.add(domainEvent);
  }

  @AfterDomainEventPublication
  public void clearDomainEventsCallback() {
    this.domainEvents.clear();
  }

  @DomainEvents
  public List<ApplicationEventWrapper> aggregateDomainEvents() {
    return domainEvents.stream().map(ApplicationEventWrapper::wrap).collect(Collectors.toList());
  }
}
