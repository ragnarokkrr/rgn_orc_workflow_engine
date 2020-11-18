package ragna.wf.orc.engine.domain.workflow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

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
    final var createWorkflowMono = workflowCreationService.createWorkflow(createWorkflowCommand);
    final WorkflowVO[] createdWorkflowVoArray = new WorkflowVO[1];
    StepVerifier.create(createWorkflowMono)
        .expectNextMatches(
            workflowVO -> {
              createdWorkflowVoArray[0] = workflowVO;
              return true;
            })
        .verifyComplete();

    final var createdWorkflowVO = createdWorkflowVoArray[0];
    final var triggerFirstTaskCommand =
        TriggerFirstTaskCommand.builder().workflowId(createdWorkflowVO.getId()).build();

    final var triggerFirstTaskMono =
        workflowTaskManagementService.triggerFirstTask(triggerFirstTaskCommand);

    StepVerifier.create(triggerFirstTaskMono)
        .expectNextMatches(
            workflowVO -> {
              assertThat(workflowVO)
                  .hasFieldOrPropertyWithValue("id", createdWorkflowVO.getId())
                  .hasFieldOrPropertyWithValue("configurationId", configuration.getId())
                  .hasFieldOrPropertyWithValue("customerId", createWorkflowCommand.getCustomerId())
                  .hasFieldOrPropertyWithValue("customerRequestId", createWorkflowCommand.getId())
                  .hasFieldOrPropertyWithValue("result", WorkflowVO.Result.WORKFLOW_ONGOING)
                  .hasFieldOrPropertyWithValue("status", WorkflowVO.Status.ORCHESTRATING)
                  .hasNoNullFieldsOrProperties()
                  .isNotNull();
              return true;
            })
        .verifyComplete();

    final var finishFirstTaskCommand =
        FinishTaskCommand.builder()
            .workflowId(createdWorkflowVO.getId())
            .taskType(FinishTaskCommand.TaskType.ANALYSIS)
            .order(1)
            .result(FinishTaskCommand.Result.RECOMMENDED)
            .build();

    final var finishFirstTaskAndAdvanceMono =
        workflowTaskManagementService.finishTaskAndAdvance(finishFirstTaskCommand);
    StepVerifier.create(finishFirstTaskAndAdvanceMono)
        .expectNextMatches(
            workflowVO -> {
              assertThat(workflowVO)
                  .hasFieldOrPropertyWithValue("id", createdWorkflowVO.getId())
                  .hasFieldOrPropertyWithValue("configurationId", configuration.getId())
                  .hasFieldOrPropertyWithValue("customerId", createWorkflowCommand.getCustomerId())
                  .hasFieldOrPropertyWithValue("customerRequestId", createWorkflowCommand.getId())
                  .hasFieldOrPropertyWithValue("result", WorkflowVO.Result.WORKFLOW_ONGOING)
                  .hasFieldOrPropertyWithValue("status", WorkflowVO.Status.ORCHESTRATING)
                  .hasNoNullFieldsOrProperties()
                  .isNotNull();
              return true;
            })
        .verifyComplete();

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
    StepVerifier.create(registerFirstTaskResultsMono)
        .expectNextMatches(
            workflowVO -> {
              assertThat(workflowVO)
                  .hasFieldOrPropertyWithValue("id", createdWorkflowVO.getId())
                  .hasFieldOrPropertyWithValue("configurationId", configuration.getId())
                  .hasFieldOrPropertyWithValue("customerId", createWorkflowCommand.getCustomerId())
                  .hasFieldOrPropertyWithValue("customerRequestId", createWorkflowCommand.getId())
                  .hasFieldOrPropertyWithValue("result", WorkflowVO.Result.WORKFLOW_ONGOING)
                  .hasFieldOrPropertyWithValue("status", WorkflowVO.Status.ORCHESTRATING)
                  .hasNoNullFieldsOrProperties()
                  .isNotNull();
              return true;
            })
        .verifyComplete();

    final var finishSecondTaskCommand =
        FinishTaskCommand.builder()
            .workflowId(createdWorkflowVO.getId())
            .taskType(FinishTaskCommand.TaskType.DECISION)
            .order(2)
            .result(FinishTaskCommand.Result.APPROVED)
            .build();

    final var finishSecondTaskAndAdvanceMono =
        workflowTaskManagementService.finishTaskAndAdvance(finishSecondTaskCommand);
    StepVerifier.create(finishSecondTaskAndAdvanceMono)
        .expectNextMatches(
            workflowVO -> {
              assertThat(workflowVO)
                  .hasFieldOrPropertyWithValue("id", createdWorkflowVO.getId())
                  .hasFieldOrPropertyWithValue("configurationId", configuration.getId())
                  .hasFieldOrPropertyWithValue("customerId", createWorkflowCommand.getCustomerId())
                  .hasFieldOrPropertyWithValue("customerRequestId", createWorkflowCommand.getId())
                  .hasFieldOrPropertyWithValue("result", WorkflowVO.Result.APPROVED)
                  .hasFieldOrPropertyWithValue("status", WorkflowVO.Status.FINISHED)
                  .hasNoNullFieldsOrProperties()
                  .isNotNull();
              return true;
            })
        .verifyComplete();

    final var workflowRepositoryByIdMono = workflowRepository.findById(createdWorkflowVO.getId());
    StepVerifier.create(workflowRepositoryByIdMono)
        .expectNextMatches(
            workflowRoot -> {
              // TODO assertions
              System.out.println(workflowRoot);
              assertThat(workflowRoot)
                  .isNotNull()
                  // .hasFieldOrPropertyWithValue("customerRequest", WorkflowModelFixture.)
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

              assertThat(
                      workflowRoot
                          .getExecutionPlan()
                          .getPlannedTasks()
                          .get(0)
                          .getTaskCriteriaResult())
                  .containsKeys("crit01", "crit02");
              assertThat(
                      workflowRoot
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
                      workflowRoot
                          .getExecutionPlan()
                          .getPlannedTasks()
                          .get(1)
                          .getTaskCriteriaResult())
                  .isEmpty();

              return true;
            })
        .verifyComplete();
  }
}
