package ragna.wf.orc.engine.application.replay;

import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ragna.wf.orc.engine.domain.workflow.model.events.WorkflowRootFinished;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.main.MainStoredEventReplayerCallback;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.main.vo.MainReplayContextVo;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WorkflowRootFinishedReplayer
    implements MainStoredEventReplayerCallback<WorkflowRootFinished> {
  private static final FluentLogger LOGGER =
      FluentLoggerFactory.getLogger(WorkflowRootFinishedReplayer.class);

  @Override
  public Mono<MainReplayContextVo> doReplay(MainReplayContextVo mainReplayContextVo) {

    final var matchResult =
        MainReplayContextVo.MatchResult.builder()
            .matchResultType(MainReplayContextVo.MatchResultEnum.MATCHED)
            .build();
    return Mono.just(mainReplayContextVo.matchResult(matchResult))
        .doOnSuccess(
            mainReplayContextVo1 ->
                LOGGER
                    .info()
                    .log(
                        "Worfklow Finished Event replayed! {}",
                        mainReplayContextVo1.getStoredEventVo()));
  }

  // TODO: implement send finish workflow message
  @Override
  public Mono<MainReplayContextVo> publish(MainReplayContextVo mainReplayContextVo) {
    LOGGER
        .info()
        .log(
            "Workflow Finished Event will be published! {}",
            mainReplayContextVo.getStoredEventVo());

    return Mono.just(mainReplayContextVo)
        .doOnSuccess(
            mainReplayContextVo1 ->
                LOGGER
                    .info()
                    .log(
                        "Workflow Finished Event published! {}",
                        mainReplayContextVo1.getStoredEventVo()));
  }
}
