package ragna.wf.orc.engine.application.replay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MongoDBContainer;
import ragna.wf.orc.common.data.mongodb.utils.MongoDbUtils;
import ragna.wf.orc.engine.domain.metadata.service.WorkflowMetadataService;
import ragna.wf.orc.engine.domain.workflow.model.*;
import ragna.wf.orc.engine.domain.workflow.model.events.*;
import ragna.wf.orc.engine.domain.workflow.repository.WorkflowRepository;
import ragna.wf.orc.engine.domain.workflow.service.ServiceFixtures;
import ragna.wf.orc.engine.domain.workflow.service.WorkflowCreationService;
import ragna.wf.orc.engine.domain.workflow.service.WorkflowTaskManagementService;
import ragna.wf.orc.engine.domain.workflow.service.mapper.ConfigurationMapper;
import ragna.wf.orc.engine.domain.workflow.service.vo.FinishTaskCommand;
import ragna.wf.orc.engine.domain.workflow.service.vo.WorkflowVO;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.main.vo.MainReplayContextVo;
import ragna.wf.orc.eventstore.config.MongoDBTestContainers;
import ragna.wf.orc.eventstore.model.StoredEvent;
import ragna.wf.orc.eventstore.model.StoredEventStatus;
import ragna.wf.orc.eventstore.repository.StoredEventRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    properties = {"orc.feature.toggles.replay-enabled=true", "orc.events.replayInitialDelaySecs=1"})
public class HappyPathReplayTest {
  @Autowired private ReactiveMongoOperations reactiveMongoOperations;

  @MockBean private WorkflowMetadataService workflowMetadataService;

  @Autowired private WorkflowCreationService workflowCreationService;

  @Autowired private WorkflowTaskManagementService workflowTaskManagementService;

  @Autowired private WorkflowRepository workflowRepository;

  @SpyBean private WorkflowRootFinishedReplayer workflowRootFinishedReplayer;

  @SpyBean private WorkflowRootCreatedReplayer workflowRootCreatedReplayer;

  @SpyBean private WorkflowRootTaskTriggeredReplayer workflowRootTaskTriggeredReplayer;
  @SpyBean private WorkflowRootTaskEvaluatedReplayer workflowRootTaskEvaluatedReplayer;
  @SpyBean private WorkflowRootTaskFinishedReplayer workflowRootTaskFinishedReplayer;

  @Autowired StoredEventRepository storedEventRepository;
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
  void before() throws InterruptedException {

    final var createCollectionFlux = MongoDbUtils.reCreateCollections(this.reactiveMongoOperations);

    StepVerifier.create(createCollectionFlux)
        .expectNextCount(MongoDbUtils.getCollectionNames().size())
        .verifyComplete();

    final var configuration = WorkflowModelFixture.sampleTwoTasksConfiguration();
    doReturn(Mono.just(ConfigurationMapper.INSTANCE.toService(configuration)))
        .when(workflowMetadataService)
        .peekConfigurationForWorkflow(any());
    waitForStoredEventReplay();
    return;
  }

  @Test
  void whenWorkflowRootFinishesAndAdvanceAllTasksAndAchieveConclusionState_thenWorkflowIsFinished()
      throws InterruptedException {
    // *** 1) create workflow
    final var createWorkflowCommand = ServiceFixtures.kyleReeseCreateWorkflowCommand();
    final var createWorkflowMono = workflowCreationService.createWorkflow(createWorkflowCommand);

    final var createdWorkflowVOs = new ArrayList<WorkflowVO>();
    StepVerifier.create(createWorkflowMono)
        .recordWith(() -> createdWorkflowVOs)
        .expectNextCount(1)
        .verifyComplete();

    waitForStoredEventReplay();
    final var createdWorkflowVo = createdWorkflowVOs.get(0);
    // *** 2) finish task 1
    final var finishTaskCommand =
        FinishTaskCommand.builder()
            .workflowId(createdWorkflowVo.getId())
            .order(1)
            .taskType(FinishTaskCommand.TaskType.ANALYSIS)
            .result(FinishTaskCommand.Result.RECOMMENDED)
            .build();

    final var finishTaskWorkflowVOs = new ArrayList<WorkflowVO>();
    final var finishTaskAndAdvanceMono =
        workflowTaskManagementService.finishTaskAndAdvance(finishTaskCommand);

    StepVerifier.create(finishTaskAndAdvanceMono)
        .recordWith(() -> finishTaskWorkflowVOs)
        .expectNextCount(1)
        .verifyComplete();

    //
    waitForStoredEventReplay();

    // 4 - finish task 2
    final var finishTask2Command =
        FinishTaskCommand.builder()
            .workflowId(createdWorkflowVo.getId())
            .order(2)
            .taskType(FinishTaskCommand.TaskType.DECISION)
            .result(FinishTaskCommand.Result.APPROVED)
            .build();

    final var finishTask2WorkflowVoArray = new ArrayList<WorkflowVO>();
    final var finishTask2AndAdvanceMono =
        workflowTaskManagementService.finishTaskAndAdvance(finishTask2Command);

    StepVerifier.create(finishTask2AndAdvanceMono)
        .recordWith(() -> finishTask2WorkflowVoArray)
        .expectNextCount(1)
        .verifyComplete();

    //
    waitForStoredEventReplay();

    final var storedEventList =
        storedEventRepository
            .findByObjectIdOrderByOccurredOnAsc(createdWorkflowVo.getId())
            .collectList()
            .block();

    final var workflowRootFinalResult =
        workflowRepository.findById(createdWorkflowVo.getId()).block();

    assertThat(storedEventList).isNotNull().hasSize(7);

    assertThat(storedEventList)
        .extracting(StoredEvent::getTypedName)
        .containsExactly(
            WorkflowRootCreated.class.getName(),
            WorkflowRootTaskTriggered.class.getName(),
            WorkflowRootTaskEvaluated.class.getName(),
            WorkflowRootTaskFinished.class.getName(),
            WorkflowRootTaskTriggered.class.getName(),
            WorkflowRootTaskFinished.class.getName(),
            WorkflowRootFinished.class.getName());

    assertThat(storedEventList)
        .extracting(StoredEvent::getEventStatus)
        .containsExactly(
            StoredEventStatus.PROCESSED,
            StoredEventStatus.PUBLISHED,
            StoredEventStatus.PROCESSED,
            StoredEventStatus.PROCESSED,
            StoredEventStatus.PROCESSED,
            StoredEventStatus.PROCESSED,
            StoredEventStatus.PROCESSED);

    verify(workflowRootCreatedReplayer, times(1)).doReplay(any(MainReplayContextVo.class));
    verify(workflowRootFinishedReplayer, times(1)).doReplay(any(MainReplayContextVo.class));
    verify(workflowRootTaskTriggeredReplayer, times(2)).doReplay(any(MainReplayContextVo.class));
    verify(workflowRootTaskTriggeredReplayer, times(1)).publish(any(MainReplayContextVo.class));
    verify(workflowRootTaskTriggeredReplayer, times(2))
        .saveTaskCriteriaMatchResultIfNecessary(any(MainReplayContextVo.class));
    verify(workflowRootTaskTriggeredReplayer, times(1))
        .saveTaskCriteriaMatchResult(any(MainReplayContextVo.class), any());
    verify(workflowRootTaskEvaluatedReplayer, times(1)).doReplay(any(MainReplayContextVo.class));
    verify(workflowRootTaskFinishedReplayer, times(2)).doReplay(any(MainReplayContextVo.class));

    assertThat(workflowRootFinalResult)
        .isNotNull()
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
                .value("4")
                .result(PlannedTask.TaskCriteriaResult.Result.APPROVED)
                // TODO Fix TaskCriteriaResult.Status
                // .status(PlannedTask.TaskCriteriaResult.Status.MATCHED)
                .build(),
            PlannedTask.TaskCriteriaResult.builder()
                .id("crit02")
                .value("5")
                .result(PlannedTask.TaskCriteriaResult.Result.APPROVED)
                // TODO Fix TaskCriteriaResult.Status
                // .status(PlannedTask.TaskCriteriaResult.Status.MATCHED)
                .build());

    assertThat(
            workflowRootFinalResult
                .getExecutionPlan()
                .getPlannedTasks()
                .get(1)
                .getTaskCriteriaResult())
        .isEmpty();
  }

  private void waitForStoredEventReplay() throws InterruptedException {
    TimeUnit.MILLISECONDS.sleep(1000);
  }
}
