package ragna.wf.orc.engine.domain.workflow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MongoDBContainer;
import ragna.wf.orc.common.data.mongodb.utils.MongoDbUtils;
import ragna.wf.orc.engine.domain.metadata.service.WorkflowMetadataService;
import ragna.wf.orc.engine.domain.workflow.model.*;
import ragna.wf.orc.engine.domain.workflow.repository.WorkflowRepository;
import ragna.wf.orc.engine.domain.workflow.service.mapper.ConfigurationMapper;
import ragna.wf.orc.engine.domain.workflow.service.mapper.RegisterTaskResultsCommandMapper;
import ragna.wf.orc.engine.domain.workflow.service.vo.FinishTaskCommand;
import ragna.wf.orc.engine.domain.workflow.service.vo.RegisterTaskResultsCommand;
import ragna.wf.orc.engine.domain.workflow.service.vo.TriggerFirstTaskCommand;
import ragna.wf.orc.engine.domain.workflow.service.vo.WorkflowVO;
import ragna.wf.orc.eventstore.config.MongoDBTestContainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Profile("embedMongoWithTx")
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"orc.feature.toggles.replay-enabled=false"})
class WorkflowServiceHappyPathTest {
  @MockBean private WorkflowMetadataService workflowMetadataService;

  @Autowired private WorkflowCreationService workflowCreationService;

  @Autowired private WorkflowTaskManagementService workflowTaskManagementService;

  @Autowired private ReactiveMongoOperations reactiveMongoOperations;

  @Autowired private WorkflowRepository workflowRepository;

  private static final MongoDBContainer MONGO_DB_CONTAINER =
      MongoDBTestContainers.defaultMongoContainer();

  @BeforeAll
  static void setUpAll() {
    MONGO_DB_CONTAINER.start();
    MongoDBTestContainers.setSpringDataProperties(MONGO_DB_CONTAINER);
  }

  @AfterAll
  static void tearDownAll() {
    if (!MONGO_DB_CONTAINER.isShouldBeReused()) {
      MONGO_DB_CONTAINER.stop();
    }
  }

  @BeforeEach
  void before() {

    final var createCollectionFlux = MongoDbUtils.reCreateCollections(this.reactiveMongoOperations);

    StepVerifier.create(createCollectionFlux)
        .expectNextCount(MongoDbUtils.getCollectionNames().size())
        .verifyComplete();
  }

  @Test
  void whenWorkflowRootFinishesAndAdvanceAllTasksAndAchieveConclusionState_thenWorkflowIsFinished()
      throws InterruptedException {
    // given
    final var configuration = WorkflowModelFixture.sampleTwoTasksConfiguration();
    doReturn(Mono.just(ConfigurationMapper.INSTANCE.toService(configuration)))
        .when(workflowMetadataService)
        .peekConfigurationForWorkflow(any());
    final var createWorkflowCommand = ServiceFixtures.kyleReeseCreateWorkflowCommand();

    // when
    // *** 1) Workflow creation
    final var workflowCreationVOS = new ArrayList<WorkflowVO>();
    final var createWorkflowMono = workflowCreationService.createWorkflow(createWorkflowCommand);
    StepVerifier.create(createWorkflowMono)
        .recordWith(() -> workflowCreationVOS)
        .expectNextCount(1)
        .verifyComplete();
    final var workflowCreation = workflowCreationVOS.get(0);
    assertThat(workflowCreation)
        .isNotNull()
        .hasNoNullFieldsOrProperties()
        .hasFieldOrPropertyWithValue("configurationId", configuration.getId())
        .hasFieldOrPropertyWithValue("customerId", createWorkflowCommand.getCustomerId())
        .hasFieldOrPropertyWithValue("customerRequestId", createWorkflowCommand.getId())
        .hasFieldOrPropertyWithValue("result", WorkflowVO.Result.WORKFLOW_ONGOING)
        .hasFieldOrPropertyWithValue("status", WorkflowVO.Status.CONFIGURED);

    // *** 2) First task triggering
    final var createdWorkflowVO = workflowCreationVOS.get(0);
    final var triggerFirstTaskCommand =
        TriggerFirstTaskCommand.builder().workflowId(createdWorkflowVO.getId()).build();

    final var triggerFirstTaskMono =
        workflowTaskManagementService.triggerFirstTask(triggerFirstTaskCommand);

    final var workflowFirstTaskTriggeredVOS = new ArrayList<WorkflowVO>();
    StepVerifier.create(triggerFirstTaskMono)
        .recordWith(() -> workflowFirstTaskTriggeredVOS)
        .expectNextCount(1)
        .verifyComplete();
    final var workflowFirstTask = workflowFirstTaskTriggeredVOS.get(0);
    assertThat(workflowFirstTask)
        .isNotNull()
        .hasNoNullFieldsOrProperties()
        .hasFieldOrPropertyWithValue("id", createdWorkflowVO.getId())
        .hasFieldOrPropertyWithValue("configurationId", configuration.getId())
        .hasFieldOrPropertyWithValue("customerId", createWorkflowCommand.getCustomerId())
        .hasFieldOrPropertyWithValue("customerRequestId", createWorkflowCommand.getId())
        .hasFieldOrPropertyWithValue("result", WorkflowVO.Result.WORKFLOW_ONGOING)
        .hasFieldOrPropertyWithValue("status", WorkflowVO.Status.ORCHESTRATING);

    // *** 3) First task finishing and advance
    final var finishFirstTaskCommand =
        FinishTaskCommand.builder()
            .workflowId(createdWorkflowVO.getId())
            .taskType(FinishTaskCommand.TaskType.ANALYSIS)
            .order(1)
            .result(FinishTaskCommand.Result.RECOMMENDED)
            .build();

    final var finishFirstTaskAndAdvanceMono =
        workflowTaskManagementService.finishTaskAndAdvance(finishFirstTaskCommand);
    final var workflowFirstTaskFinishedVOS = new ArrayList<WorkflowVO>();

    StepVerifier.create(finishFirstTaskAndAdvanceMono)
        .recordWith(() -> workflowFirstTaskFinishedVOS)
        .expectNextCount(1)
        .verifyComplete();

    final var workflowFirstTaskFinishedVO = workflowFirstTaskFinishedVOS.get(0);
    assertThat(workflowFirstTaskFinishedVO)
        .isNotNull()
        .hasNoNullFieldsOrProperties()
        .hasFieldOrPropertyWithValue("id", createdWorkflowVO.getId())
        .hasFieldOrPropertyWithValue("configurationId", configuration.getId())
        .hasFieldOrPropertyWithValue("customerId", createWorkflowCommand.getCustomerId())
        .hasFieldOrPropertyWithValue("customerRequestId", createWorkflowCommand.getId())
        .hasFieldOrPropertyWithValue("result", WorkflowVO.Result.WORKFLOW_ONGOING)
        .hasFieldOrPropertyWithValue("status", WorkflowVO.Status.ORCHESTRATING);

    // *** 4) First task results registering
    final var taskCriteriaResults =
        WorkflowModelFixture.johnConnorCriteriaEvaluation().stream()
            .map(RegisterTaskResultsCommandMapper.INSTANCE::toService)
            .collect(Collectors.toList());

    final var registerFirsTaskResultsCommand =
        RegisterTaskResultsCommand.builder()
            .workflowId(createdWorkflowVO.getId())
            .taskType(RegisterTaskResultsCommand.TaskType.ANALYSIS)
            .order(1)
            .taskCriteriaResults(taskCriteriaResults)
            .build();

    final var registerFirstTaskResultsMono =
        workflowTaskManagementService.registerTaskActivationResult(registerFirsTaskResultsCommand);
    final var workflowFirstTaskResultsVOS = new ArrayList<WorkflowVO>();
    StepVerifier.create(registerFirstTaskResultsMono)
        .recordWith(() -> workflowFirstTaskResultsVOS)
        .expectNextCount(1)
        .verifyComplete();
    final var registerFirstTaskResult = workflowFirstTaskResultsVOS.get(0);
    assertThat(registerFirstTaskResult)
        .isNotNull()
        .hasNoNullFieldsOrProperties()
        .hasFieldOrPropertyWithValue("id", createdWorkflowVO.getId())
        .hasFieldOrPropertyWithValue("configurationId", configuration.getId())
        .hasFieldOrPropertyWithValue("customerId", createWorkflowCommand.getCustomerId())
        .hasFieldOrPropertyWithValue("customerRequestId", createWorkflowCommand.getId())
        .hasFieldOrPropertyWithValue("result", WorkflowVO.Result.WORKFLOW_ONGOING)
        .hasFieldOrPropertyWithValue("status", WorkflowVO.Status.ORCHESTRATING);

    // *** 5) Second task - finishing
    final var finishSecondTaskCommand =
        FinishTaskCommand.builder()
            .workflowId(createdWorkflowVO.getId())
            .taskType(FinishTaskCommand.TaskType.DECISION)
            .order(2)
            .result(FinishTaskCommand.Result.APPROVED)
            .build();

    final var finishSecondTaskAndAdvanceMono =
        workflowTaskManagementService.finishTaskAndAdvance(finishSecondTaskCommand);
    final var workflowSecondTaskFinishingVOS = new ArrayList<WorkflowVO>();
    StepVerifier.create(finishSecondTaskAndAdvanceMono)
        .recordWith(() -> workflowSecondTaskFinishingVOS)
        .expectNextCount(1)
        .verifyComplete();
    final var workflowSecondTaskFinishingVO = workflowSecondTaskFinishingVOS.get(0);
    assertThat(workflowSecondTaskFinishingVO)
        .isNotNull()
        .hasNoNullFieldsOrProperties()
        .hasFieldOrPropertyWithValue("id", createdWorkflowVO.getId())
        .hasFieldOrPropertyWithValue("configurationId", configuration.getId())
        .hasFieldOrPropertyWithValue("customerId", createWorkflowCommand.getCustomerId())
        .hasFieldOrPropertyWithValue("customerRequestId", createWorkflowCommand.getId())
        .hasFieldOrPropertyWithValue("result", WorkflowVO.Result.APPROVED)
        .hasFieldOrPropertyWithValue("status", WorkflowVO.Status.FINISHED);

    // *** 6) final Workflow Result assertions
    final var workflowFinalResults = new ArrayList<WorkflowRoot>();
    final var workflowRepositoryByIdMono = workflowRepository.findById(createdWorkflowVO.getId());
    StepVerifier.create(workflowRepositoryByIdMono)
        .recordWith(() -> workflowFinalResults)
        .expectNextCount(1)
        .verifyComplete();
    assertThat(workflowFinalResults).isNotNull().hasSize(1);

    final var workflowRootFinalResult = workflowFinalResults.get(0);
    assertThat(workflowRootFinalResult)
        .isNotNull()
        // .hasFieldOrPropertyWithValue("customerRequest", WorkflowModelFixture.)
        // TODO Cloningconfiguration.date issue on cloning
        // .hasFieldOrPropertyWithValue("configuration",
        // WorkflowConfigurationFixture.sampleTwoTasksConfiguration())
        .hasFieldOrPropertyWithValue("status", WorkflowStatus.FINISHED)
        .hasFieldOrPropertyWithValue("result", WorkflowResult.APPROVED)
        .hasNoNullFieldsOrProperties();

    assertThat(workflowRootFinalResult.getExecutionPlan()).hasNoNullFieldsOrProperties();

    assertThat(workflowRootFinalResult.getExecutionPlan().getPlannedTasks())
        .extracting(PlannedTask::getEvaluatedOn)
        .filteredOn(Objects::isNull)
        .hasSize(1);

    assertThat(workflowRootFinalResult.getExecutionPlan().getPlannedTasks())
        .extracting(PlannedTask::getEvaluatedOn)
        .filteredOn(Objects::nonNull)
        .hasSize(1);

    assertThat(workflowRootFinalResult.getExecutionPlan().getPlannedTasks())
        .extracting(PlannedTask::getFinishedOn)
        .filteredOn(Objects::nonNull)
        .hasSize(2);

    assertThat(workflowRootFinalResult.getExecutionPlan().getPlannedTasks())
        .extracting(PlannedTask::getStatus)
        .contains(PlannedTask.Status.CONCLUDED, PlannedTask.Status.CONCLUDED);

    assertThat(workflowRootFinalResult.getExecutionPlan().getPlannedTasks())
        .extracting(PlannedTask::getResult)
        .contains(PlannedTask.Result.RECOMMENDED, PlannedTask.Result.APPROVED);

    assertThat(workflowRootFinalResult.getExecutionPlan().getPlannedTasks())
        .extracting(PlannedTask::getTaskType)
        .contains(TaskType.ANALYSIS, TaskType.DECISION);

    assertThat(workflowRootFinalResult.getExecutionPlan().getPlannedTasks())
        .extracting(PlannedTask::getOrder)
        .contains(1, 2);

    assertThat(
            workflowRootFinalResult
                .getExecutionPlan()
                .getPlannedTasks()
                .get(0)
                .getTaskCriteriaResult())
        .containsKeys("crit01", "crit02");
    assertThat(
            workflowRootFinalResult
                .getExecutionPlan()
                .getPlannedTasks()
                .get(0)
                .getTaskCriteriaResult())
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

    assertThat(
            workflowRootFinalResult
                .getExecutionPlan()
                .getPlannedTasks()
                .get(1)
                .getTaskCriteriaResult())
        .isEmpty();
  }
}
