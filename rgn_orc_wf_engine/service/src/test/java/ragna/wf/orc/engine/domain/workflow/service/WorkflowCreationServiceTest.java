package ragna.wf.orc.engine.domain.workflow.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ragna.wf.orc.engine.domain.configuration.service.WorkflowMetadataService;
import ragna.wf.orc.engine.domain.workflow.model.WorkflowConfigurationFixture;
import ragna.wf.orc.engine.domain.workflow.service.vo.CreateWorkflowCommand;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class WorkflowCreationServiceTest {

  @MockBean private WorkflowMetadataService workflowMetadataService;

  @MockBean private WorkflowCreationService workflowCreationService;

  @Test
  void whenANewRequestIsSubmitted_thenShouldCreateWorkflow() {
    // given
    doReturn(Mono.just(WorkflowConfigurationFixture.sampleTwoTasksConfiguration()))
        .when(workflowMetadataService)
        .peekConfigurationForWorkflow(any());

    final var createWorkflowCommand = kyleReese();

    // when
    final var workflowMono = workflowCreationService.createWorkflow(kyleReese());

    // then
    StepVerifier.create(workflowMono)
        .expectNextMatches(
            workflowVO -> {
              assertThat(workflowVO).isNotNull();
              return true;
            })
        .verifyComplete();
  }

  private CreateWorkflowCommand kyleReese() {
    return CreateWorkflowCommand.builder()
        .id("1")
        .customerId("1")
        .customerName("Kyle Reese")
        .requestMemo("Kyle Reese's request memo")
        .build();
  }
}
