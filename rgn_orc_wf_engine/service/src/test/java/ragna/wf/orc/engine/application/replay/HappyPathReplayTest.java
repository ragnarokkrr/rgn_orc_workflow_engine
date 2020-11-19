package ragna.wf.orc.engine.application.replay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MongoDBContainer;
import ragna.wf.orc.common.data.mongodb.utils.MongoDbUtils;
import ragna.wf.orc.engine.domain.metadata.service.WorkflowMetadataService;
import ragna.wf.orc.engine.domain.workflow.model.WorkflowModelFixture;
import ragna.wf.orc.engine.domain.workflow.model.events.*;
import ragna.wf.orc.engine.domain.workflow.repository.WorkflowRepository;
import ragna.wf.orc.engine.domain.workflow.service.ServiceFixtures;
import ragna.wf.orc.engine.domain.workflow.service.WorkflowCreationService;
import ragna.wf.orc.engine.domain.workflow.service.WorkflowTaskManagementService;
import ragna.wf.orc.engine.domain.workflow.service.mapper.ConfigurationMapper;
import ragna.wf.orc.engine.domain.workflow.service.vo.FinishTaskCommand;
import ragna.wf.orc.engine.domain.workflow.service.vo.WorkflowVO;
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
    waitForStoredEventReplay();

    // 1 - create workflow
    final var createWorkflowCommand = ServiceFixtures.kyleReeseCreateWorkflowCommand();
    final var createWorkflowMono = workflowCreationService.createWorkflow(createWorkflowCommand);

    final var createdWorkflowVOs = new ArrayList<WorkflowVO>();
    StepVerifier.create(createWorkflowMono)
        .recordWith(() -> createdWorkflowVOs)
        .expectNextCount(1)
        .verifyComplete();

    waitForStoredEventReplay();
    final var createdWorkflowVo = createdWorkflowVOs.get(0);
    // 2 - finish task 1
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
        .expectNextCount(0)
        .verifyComplete();

    //
    waitForStoredEventReplay();

    final var storedEventList =
        storedEventRepository
            .findByObjectIdOrderByOccurredOnAsc(createdWorkflowVo.getId())
            .collectList()
            .block();

    final var workflowRoot = workflowRepository.findById(createdWorkflowVo.getId()).block();

    assertThat(storedEventList).isNotNull().hasSize(7);

    assertThat(workflowRoot).isNotNull();

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
            StoredEventStatus.UNPUBLISHED,
            StoredEventStatus.PROCESSED,
            StoredEventStatus.PROCESSING,
            StoredEventStatus.PROCESSING,
            StoredEventStatus.PROCESSING,
            StoredEventStatus.PROCESSING);
  }

  private void waitForStoredEventReplay() throws InterruptedException {
    TimeUnit.MILLISECONDS.sleep(1000);
  }
}
