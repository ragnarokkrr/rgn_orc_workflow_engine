package ragna.wf.orc.engine.domain.workflow.model;

import org.junit.jupiter.api.Test;
import ragna.wf.orc.common.events.spring.ApplicationEventWrapper;
import ragna.wf.orc.engine.domain.workflow.model.events.WorkflowRootCreated;
import ragna.wf.orc.engine.domain.workflow.model.events.WorkflowRootFinished;
import ragna.wf.orc.engine.domain.workflow.model.events.WorkflowRootTaskEvaluated;
import ragna.wf.orc.engine.domain.workflow.model.events.WorkflowRootTaskFinished;
import ragna.wf.orc.engine.domain.workflow.model.events.WorkflowRootTaskTriggered;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowRootHappyPathsTest {
  @Test
  void
      whenWorkflowRootFinishesAndAdvanceAllTasksAndAchieveConclusionState_thenWorkflowIsFinished() {
    // given
    final var workflowRoot =
        WorkflowRoot.createWorkflowRoot(kyleReese())
            .addWorkflowConfiguration(WorkflowModelFixture.sampleTwoTasksConfiguration())
            .createExecutionPlan()
            .configured();

    // when
    workflowRoot.triggerFirstTask();
    workflowRoot.finishTaskAndAdvance(TaskType.ANALYSIS, 1, PlannedTask.Result.RECOMMENDED);
    workflowRoot.registerTaskCriteriaEvaluationResults(
        TaskType.ANALYSIS, 1, WorkflowModelFixture.johnConnorCriteriaEvaluation());
    workflowRoot.finishTaskAndAdvance(TaskType.DECISION, 2, PlannedTask.Result.APPROVED);

    // then
    assertThat(workflowRoot)
        .isNotNull()
        .hasFieldOrPropertyWithValue("customerRequest", kyleReese().toBuilder().build())
        // TODO Cloningconfiguration.date issue on cloning
        // .hasFieldOrPropertyWithValue("configuration",
        // WorkflowConfigurationFixture.sampleTwoTasksConfiguration())
        .hasFieldOrPropertyWithValue("status", WorkflowStatus.FINISHED)
        .hasFieldOrPropertyWithValue("result", WorkflowResult.APPROVED)
        .hasNoNullFieldsOrProperties();

    assertThat(workflowRoot.getExecutionPlan()).hasNoNullFieldsOrProperties();

    assertThat(workflowRoot.getExecutionPlan().getPlannedTasks())
        .extracting(PlannedTask::getEvaluatedOn)
        .filteredOn(Objects::isNull)
        .hasSize(1);

    assertThat(workflowRoot.getExecutionPlan().getPlannedTasks())
        .extracting(PlannedTask::getEvaluatedOn)
        .filteredOn(Objects::nonNull)
        .hasSize(1);

    assertThat(workflowRoot.getExecutionPlan().getPlannedTasks())
        .extracting(PlannedTask::getFinishedOn)
        .filteredOn(Objects::nonNull)
        .hasSize(2);

    assertThat(workflowRoot.getExecutionPlan().getPlannedTasks())
        .extracting(PlannedTask::getStatus)
        .contains(PlannedTask.Status.CONCLUDED, PlannedTask.Status.CONCLUDED);

    assertThat(workflowRoot.getExecutionPlan().getPlannedTasks())
        .extracting(PlannedTask::getResult)
        .contains(PlannedTask.Result.RECOMMENDED, PlannedTask.Result.APPROVED);

    assertThat(workflowRoot.getExecutionPlan().getPlannedTasks())
        .extracting(PlannedTask::getTaskType)
        .contains(TaskType.ANALYSIS, TaskType.DECISION);

    assertThat(workflowRoot.getExecutionPlan().getPlannedTasks())
        .extracting(PlannedTask::getOrder)
        .contains(1, 2);

    assertThat(workflowRoot.getExecutionPlan().getPlannedTasks().get(0).getTaskCriteriaResult())
        .containsKeys("crit01", "crit02");
    assertThat(workflowRoot.getExecutionPlan().getPlannedTasks().get(0).getTaskCriteriaResult())
        .containsValues(
            PlannedTask.TaskCriteriaResult.builder()
                .id("crit01")
                .value("8.5")
                .result(PlannedTask.TaskCriteriaResult.Result.APPROVED)
                .status(PlannedTask.TaskCriteriaResult.Status.MATCHED)
                .build(),
            PlannedTask.TaskCriteriaResult.builder()
                .id("crit02")
                .value("2")
                .result(PlannedTask.TaskCriteriaResult.Result.APPROVED)
                .status(PlannedTask.TaskCriteriaResult.Status.MATCHED)
                .build());

    assertThat(workflowRoot.getExecutionPlan().getPlannedTasks().get(1).getTaskCriteriaResult())
        .isEmpty();

    assertThat(workflowRoot.aggregateDomainEvents())
        .isNotEmpty()
        .extracting(ApplicationEventWrapper::unwrap)
        .extracting(domainEvent -> domainEvent)
        .extracting(Object::getClass)
        .extracting(Object::toString)
        .containsExactly(
            "class " + WorkflowRootCreated.class.getName(),
            "class " + WorkflowRootTaskTriggered.class.getName(),
            "class " + WorkflowRootTaskFinished.class.getName(),
            "class " + WorkflowRootTaskTriggered.class.getName(),
            "class " + WorkflowRootTaskEvaluated.class.getName(),
            "class " + WorkflowRootTaskFinished.class.getName(),
            "class " + WorkflowRootFinished.class.getName());
  }

  private CustomerRequest kyleReese() {
    return CustomerRequest.builder()
        .id("1")
        .customerId("1")
        .customerName("Kyle Reese")
        .requestMemo("Kyle Reese's request memo")
        .build();
  }
}
