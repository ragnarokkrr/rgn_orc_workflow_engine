package ragna.wf.orc.engine.domain.workflow.service;

import org.junit.jupiter.api.AfterEach;
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
import ragna.wf.orc.engine.domain.workflow.service.mapper.ConfigurationMapper;
import ragna.wf.orc.engine.domain.workflow.service.vo.WorkflowVO;
import ragna.wf.orc.eventstore.config.EmbeddedMongoWithTransactionsConfig;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@Profile("embedMongoWithTx")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@Import(EmbeddedMongoWithTransactionsConfig.class)
class WorkflowCreationServiceTest {

  @MockBean private WorkflowMetadataService workflowMetadataService;

  @Autowired private WorkflowCreationService workflowCreationService;

  @Autowired private ReactiveMongoOperations reactiveMongoOperations;

  @BeforeEach
  void before() {

    final var createCollectionFlux = MongoDbUtils.reCreateCollections(this.reactiveMongoOperations);

    StepVerifier.create(createCollectionFlux)
        .expectNextCount(MongoDbUtils.getCollectionNames().size())
        .verifyComplete();
  }

  @AfterEach
  void tearDown() {
    // mongoTestUtils.tearDown();
  }

  @Test
  void whenANewRequestIsSubmitted_thenShouldCreateWorkflow() {
    // given
    final var configuration = WorkflowModelFixture.sampleTwoTasksConfiguration();
    doReturn(Mono.just(ConfigurationMapper.INSTANCE.toService(configuration)))
        .when(workflowMetadataService)
        .peekConfigurationForWorkflow(any());

    final var createWorkflowCommand = ServiceFixtures.kyleReese();

    // when
    final var createWorkflowMono = workflowCreationService.createWorkflow(createWorkflowCommand);
    // then
    StepVerifier.create(createWorkflowMono)
        .expectNextMatches(
            workflowVO -> {
              assertThat(workflowVO)
                  .hasFieldOrPropertyWithValue("configurationId", configuration.getId())
                  .hasFieldOrPropertyWithValue("customerId", createWorkflowCommand.getCustomerId())
                  .hasFieldOrPropertyWithValue("customerRequestId", createWorkflowCommand.getId())
                  .hasFieldOrPropertyWithValue("result", WorkflowVO.Result.WORKFLOW_ONGOING)
                  .hasFieldOrPropertyWithValue("status", WorkflowVO.Status.CONFIGURED)
                  .hasNoNullFieldsOrProperties()
                  .isNotNull();
              return true;
            })
        .verifyComplete();

    // TODO fix mockito inline configuration
    // verify(workflowRepository, times(1)).findByCustomerRequest(any());
    // verify(workflowRepository, times(1)).save(any());
  }
}
