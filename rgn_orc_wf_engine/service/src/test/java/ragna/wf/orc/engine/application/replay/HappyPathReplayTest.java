package ragna.wf.orc.engine.application.replay;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
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
import ragna.wf.orc.eventstore.config.EmbeddedMongoWithTransactionsConfig;
import ragna.wf.orc.eventstore.config.MongoDBTestContainers;
import ragna.wf.orc.eventstore.model.StoredEvent;
import ragna.wf.orc.eventstore.model.StoredEventStatus;
import ragna.wf.orc.eventstore.repository.StoredEventRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@Profile("embedMongoWithTx")
@ExtendWith(SpringExtension.class)
@SpringBootTest(
    properties = {"orc.feature.toggles.replay-enabled=true", "orc.events.replayInitialDelaySecs=1"})
@Import(EmbeddedMongoWithTransactionsConfig.class)
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
      TimeUnit.MILLISECONDS.sleep(1000);
  }

  @Test
  void whenWorkflowRootFinishesAndAdvanceAllTasksAndAchieveConclusionState_thenWorkflowIsFinished()
      throws InterruptedException {
    TimeUnit.MILLISECONDS.sleep(1000);

    // 1 - create workflow
    final var createWorkflowCommand = ServiceFixtures.kyleReeseCreateWorkflowCommand();
    final var createWorkflowMono = workflowCreationService.createWorkflow(createWorkflowCommand);
    final var createdWorkflowVoArray = new WorkflowVO[1];
    StepVerifier.create(createWorkflowMono)
        .expectNextMatches(
            workflowVO -> {
              createdWorkflowVoArray[0] = workflowVO;
              return true;
            })
        .verifyComplete();

    TimeUnit.MILLISECONDS.sleep(1000);

    // 2 - finish task 1
    final var finishTaskCommand =
        FinishTaskCommand.builder()
            .workflowId(createdWorkflowVoArray[0].getId())
            .order(1)
            .taskType(FinishTaskCommand.TaskType.ANALYSIS)
            .result(FinishTaskCommand.Result.RECOMMENDED)
            .build();

    final var finishTaskWorkflowVoArray = new WorkflowVO[1];
    final var finishTaskAndAdvanceMono =
        workflowTaskManagementService.finishTaskAndAdvance(finishTaskCommand);

    StepVerifier.create(finishTaskAndAdvanceMono)
        .expectNextMatches(
            workflowVO -> {
              finishTaskWorkflowVoArray[0] = workflowVO;
              return true;
            })
        .verifyComplete();

    //
    TimeUnit.MILLISECONDS.sleep(1000);

    // 3 - finish task 2
    final var finishTask2Command =
        FinishTaskCommand.builder()
            .workflowId(createdWorkflowVoArray[0].getId())
            .order(2)
            .taskType(FinishTaskCommand.TaskType.DECISION)
            .result(FinishTaskCommand.Result.APPROVED)
            .build();

    final var finishTask2WorkflowVoArray = new WorkflowVO[1];
    final var finishTask2AndAdvanceMono =
        workflowTaskManagementService.finishTaskAndAdvance(finishTask2Command);

    StepVerifier.create(finishTask2AndAdvanceMono)
        .expectNextMatches(
            workflowVO -> {
              finishTask2WorkflowVoArray[0] = workflowVO;
              return true;
            })
        .verifyComplete();

    //
    TimeUnit.MILLISECONDS.sleep(1000);

    final var storedEventList =
        storedEventRepository
            .findByObjectIdOrderByOccurredOnAsc(createdWorkflowVoArray[0].getId())
            .collectList()
            .block();

    final var workflowRoot = workflowRepository.findById(createdWorkflowVoArray[0].getId()).block();

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
                StoredEventStatus.PROCESSING
        );
  }
}
