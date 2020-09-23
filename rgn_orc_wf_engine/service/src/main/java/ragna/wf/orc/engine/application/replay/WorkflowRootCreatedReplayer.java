package ragna.wf.orc.engine.application.replay;

import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ragna.wf.orc.engine.domain.workflow.model.WorkflowRoot;
import ragna.wf.orc.engine.domain.workflow.model.events.WorkflowRootCreated;
import ragna.wf.orc.engine.domain.workflow.service.WorkflowTaskManagementService;
import ragna.wf.orc.engine.domain.workflow.service.vo.TriggerFirstTaskCommand;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.main.MainStoredEventReplayerCallback;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.main.vo.MainReplayContextVo;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WorkflowRootCreatedReplayer
    implements MainStoredEventReplayerCallback<WorkflowRootCreated> {
  private static final FluentLogger LOGGER =
      FluentLoggerFactory.getLogger(WorkflowRootCreatedReplayer.class);
  private final WorkflowTaskManagementService workflowTaskManagementService;

  @Override
  public Mono<MainReplayContextVo> doReplay(final MainReplayContextVo mainReplayContextVo) {
    final var workflowRootCreated =
        (WorkflowRootCreated) mainReplayContextVo.getStoredEventVo().getDomainEvent();
    final var workflowRoot = (WorkflowRoot) workflowRootCreated.getSource();

    final var triggerFirstTaskCommand = TriggerFirstTaskCommand.builder()
            .workflowId(workflowRoot.getId())
            .build();

    return workflowTaskManagementService
        .triggerFirstTask(triggerFirstTaskCommand)
        .doOnNext(workflowVO -> LOGGER.debug().log("First Task triggered: {}", workflowVO))
        .then(
            Mono.just(mainReplayContextVo)
                .onErrorResume(
                    throwable ->
                        Mono.just(mainReplayContextVo.errorProcessing(throwable.getMessage()))));
  }
}
