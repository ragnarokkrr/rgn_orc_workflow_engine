package ragna.wf.orc.engine.application.messaging.consumer;

import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;
import ragna.wf.orc.engine.application.messaging.OchestratorChannels;
import ragna.wf.orc.engine.application.messaging.consumer.vo.CreateWorkflowCommand;
import ragna.wf.orc.engine.application.messaging.mapper.CreateWorkflowMapper;
import ragna.wf.orc.engine.domain.workflow.service.WorkflowCreationService;
import reactor.core.publisher.Flux;

@Component
@EnableBinding(OchestratorChannels.class)
@RequiredArgsConstructor
public class CreateWorkflowConsumer {
  private static final FluentLogger LOGGER =
      FluentLoggerFactory.getLogger(CreateWorkflowConsumer.class);

  private final WorkflowCreationService workflowCreationService;

  @StreamListener
  public void receive(
      @Input(OchestratorChannels.Channels.CREATE_WORKFLOW)
          Flux<CreateWorkflowCommand> createWorkflowFlux) {

    createWorkflowFlux
        .map(CreateWorkflowMapper.INSTANCE::toService)
        .doOnNext(
            createWorkflowCommand ->
                LOGGER.info().log("Creating Workflow {}", createWorkflowCommand))
        .flatMap(workflowCreationService::createWorkflow)
        .doOnNext(workflowVO -> LOGGER.info().log("Workflow created {}!", workflowVO))
        .subscribe();
  }
}
