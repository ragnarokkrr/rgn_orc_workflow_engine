package ragna.wf.orc.engine.application.replay;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ragna.wf.orc.engine.domain.workflow.model.WorkflowRoot;
import ragna.wf.orc.engine.domain.workflow.model.events.WorkflowRootTaskFinished;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.main.MainStoredEventReplayerCallback;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.main.vo.MainReplayContextVo;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WorkflowRootTaskFinishedReplayer
    implements MainStoredEventReplayerCallback<WorkflowRootTaskFinished> {

  private static final FluentLogger LOGGER =
      FluentLoggerFactory.getLogger(WorkflowRootTaskFinishedReplayer.class);

  public Mono<MainReplayContextVo> doReplay(final MainReplayContextVo mainReplayContextVo) {
    final var workflowRoot =
        (WorkflowRoot) mainReplayContextVo.getStoredEventVo().getDomainEvent().getSource();

    // TODO fix finished condition
    final var taskFinished =
        workflowRoot.getExecutionPlan().getPlannedTasks().stream()
            .filter(plannedTask -> Objects.nonNull(plannedTask.getFinishedOn()))
            .findFirst();
    final var match =
        taskFinished
            .map(plannedTask -> MainReplayContextVo.MatchResultEnum.MATCHED)
            .orElse(MainReplayContextVo.MatchResultEnum.UNMATCHED);

    return Mono.just(
            mainReplayContextVo.matchResult(
                MainReplayContextVo.MatchResult.builder().matchResultType(match).build()))
        .doOnSuccess(
            mainReplayContextVo1 ->
                LOGGER
                    .info()
                    .log(
                        "Task Finished Event replayed! {}",
                        mainReplayContextVo1.getStoredEventVo()));
  }

  // TODO: implement send finish task message
  @Override
  public Mono<MainReplayContextVo> publish(final MainReplayContextVo mainReplayContextVo) {
    LOGGER
        .info()
        .log("Task Finished Event will be published! {}", mainReplayContextVo.getStoredEventVo());

    return Mono.just(mainReplayContextVo)
        .doOnSuccess(
            mainReplayContextVo1 ->
                LOGGER
                    .info()
                    .log(
                        "Task Finished Event published! {}",
                        mainReplayContextVo1.getStoredEventVo()));
  }
}
