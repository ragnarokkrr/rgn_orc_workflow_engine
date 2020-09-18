package ragna.wf.orc.engine.application.replay;

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
import ragna.wf.orc.common.data.mongodb.utils.MongoDbUtils;
import ragna.wf.orc.engine.domain.metadata.service.WorkflowMetadataService;
import ragna.wf.orc.engine.domain.workflow.model.WorkflowModelFixture;
import ragna.wf.orc.engine.domain.workflow.repository.WorkflowRepository;
import ragna.wf.orc.engine.domain.workflow.service.ServiceFixtures;
import ragna.wf.orc.engine.domain.workflow.service.WorkflowCreationService;
import ragna.wf.orc.engine.domain.workflow.service.WorkflowTaskManagementService;
import ragna.wf.orc.engine.domain.workflow.service.mapper.ConfigurationMapper;
import ragna.wf.orc.engine.domain.workflow.service.vo.WorkflowVO;
import ragna.wf.orc.eventstore.config.EmbeddedMongoWithTransactionsConfig;
import ragna.wf.orc.eventstore.repository.StoredEventRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@Profile("embedMongoWithTx")
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"orc.feature.toggles.replay-enabled=true",
        "orc.events.replayInitialDelaySecs=1"})
@Import(EmbeddedMongoWithTransactionsConfig.class)
public class HappyPathReplayITest {
  @Autowired private ReactiveMongoOperations reactiveMongoOperations;

  @MockBean private WorkflowMetadataService workflowMetadataService;

  @Autowired private WorkflowCreationService workflowCreationService;

  @Autowired private WorkflowTaskManagementService workflowTaskManagementService;

  @Autowired private WorkflowRepository workflowRepository;

  @Autowired StoredEventRepository storedEventRepository;

  @BeforeEach
  void before() {

    final var createCollectionFlux = MongoDbUtils.reCreateCollections(this.reactiveMongoOperations);

    StepVerifier.create(createCollectionFlux)
        .expectNextCount(MongoDbUtils.getCollectionNames().size())
        .verifyComplete();

    final var configuration = WorkflowModelFixture.sampleTwoTasksConfiguration();
    doReturn(Mono.just(ConfigurationMapper.INSTANCE.toService(configuration)))
        .when(workflowMetadataService)
        .peekConfigurationForWorkflow(any());
  }

  @Test
  void whenWorkflowRootFinishesAndAdvanceAllTasksAndAchieveConclusionState_thenWorkflowIsFinished()
      throws InterruptedException {
    // given
    final var createWorkflowCommand = ServiceFixtures.kyleReeseCreateWorkflowCommand();
    TimeUnit.SECONDS.sleep(1);

    // 1 - create workflow
    final var createWorkflowMono = workflowCreationService.createWorkflow(createWorkflowCommand);
    final WorkflowVO[] createdWorkflowVoArray = new WorkflowVO[1];
    StepVerifier.create(createWorkflowMono)
        .expectNextMatches(
            workflowVO -> {
              createdWorkflowVoArray[0] = workflowVO;
              return true;
            })
        .verifyComplete();

    TimeUnit.SECONDS.sleep(2);
    System.out.println();
    final var storedEventList =
        storedEventRepository
            .findByObjectIdOrderByOccurredOnAsc(createdWorkflowVoArray[0].getId())
            .collectList()
            .block();
    System.out.println(storedEventList);
  }
}
