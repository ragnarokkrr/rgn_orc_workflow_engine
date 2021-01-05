package ragna.wf.orc.engine.application.replay;

import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ragna.wf.orc.engine.domain.workflow.model.events.WorkflowRootTaskEvaluated;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.main.MainStoredEventReplayerCallback;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.main.vo.MainReplayContextVo;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WorkflowRootTaskEvaluatedReplayer
    implements MainStoredEventReplayerCallback<WorkflowRootTaskEvaluated> {
  private static final FluentLogger LOGGER =
      FluentLoggerFactory.getLogger(WorkflowRootTaskEvaluatedReplayer.class);

  @Override
  public Mono<MainReplayContextVo> doReplay(final MainReplayContextVo mainReplayContextVo) {
    return Mono.just(mainReplayContextVo)
        .doOnSuccess(
            mainReplayContextVo1 ->
                LOGGER
                    .info()
                    .log(
                        "Task Evaluated Event replayed! {}",
                        mainReplayContextVo1.getStoredEventVo()));
  }
}
