package ragna.wf.orc.engine.domain.workflow.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowRootTest {

  @Test
  void whenWorkflowRootIsCreated_thenStatusInstantiated() {
    // given
    final var customerRequest = kyleReese();

    // when
    final var workflowRoot = WorkflowRoot.createWorkflowRoot(customerRequest);

    // then
    assertThat(workflowRoot)
        .isNotNull()
        .hasFieldOrPropertyWithValue("customerRequest", customerRequest)
        .hasFieldOrPropertyWithValue("status", WorkflowStatus.INSTANTIATED)
        .hasFieldOrPropertyWithValue("result", WorkflowResult.WORKFLOW_ONGOING)
        .hasNoNullFieldsOrPropertiesExcept("configuration", "executionPlan");
  }

  @Test
  void whenWorkflowRootIsConfigured_thenStatusConfigured() {
    // given
    final var customerRequest = kyleReese();

    // when
    final var workflowRoot =
        WorkflowRoot.createWorkflowRoot(customerRequest)
            .addWorkflowConfiguration(WorkflowConfigurationFixture.sampleTwoTasksConfiguration())
            .createExecutionPlan()
            .configured();

    // then
    assertThat(workflowRoot)
        .isNotNull()
        .hasFieldOrPropertyWithValue("customerRequest", customerRequest)
        .hasFieldOrPropertyWithValue("status", WorkflowStatus.CONFIGURED)
        .hasFieldOrPropertyWithValue("result", WorkflowResult.WORKFLOW_ONGOING)
        .hasNoNullFieldsOrProperties();

    assertThat(workflowRoot.getExecutionPlan()).hasNoNullFieldsOrProperties();

    final var plannedTasks = workflowRoot.getExecutionPlan().getPlannedTasks();

    assertThat(plannedTasks).extracting(PlannedTask::getOrder).contains(1, 2);

    assertThat(plannedTasks)
        .extracting(PlannedTask::getTaskType)
        .contains(TaskType.ANALYSIS, TaskType.DECISION);

    assertThat(plannedTasks)
        .extracting(PlannedTask::getResult)
        .contains(PlannedTask.Result.WAITING_FOR_RESULT, PlannedTask.Result.WAITING_FOR_RESULT);

    assertThat(plannedTasks)
        .extracting(PlannedTask::getStatus)
        .contains(PlannedTask.Status.PLANNED, PlannedTask.Status.PLANNED);

    assertThat(plannedTasks)
        .extracting(PlannedTask::getTaskCriteriaResult)
        .extracting(Map::isEmpty)
        .contains(true, true);
  }

  // TODO assertions / validations tests

  private CustomerRequest kyleReese() {
    return CustomerRequest.builder()
        .id("1")
        .customerId("1")
        .customerName("Kyle Reese")
        .requestMemo("Kyle Reese's request memo")
        .build();
  }
}
