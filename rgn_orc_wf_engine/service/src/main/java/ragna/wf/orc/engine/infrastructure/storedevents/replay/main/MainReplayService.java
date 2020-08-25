package ragna.wf.orc.engine.infrastructure.storedevents.replay.main;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.main.vo.MainReplayContextVo;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.secondary.vo.SecondaryReplayContextVo;
import ragna.wf.orc.eventstore.service.EventStoreService;
import ragna.wf.orc.eventstore.service.vo.UpdateStoredEventCommand;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MainReplayService {
  private static final FluentLogger LOGGER = FluentLoggerFactory.getLogger(MainReplayService.class);
  private final EventStoreService eventStoreService;
  private final ReplayProcessor<MainReplayContextVo> mainReplayContextVoReplayProcessor;
  private final ReplayProcessor<SecondaryReplayContextVo> sideReplayContextVoReplayProcessor;
  private final ApplicationContext applicationContext;

  @PostConstruct
  void init() {
    mainReplayContextVoReplayProcessor
        .doOnNext(
            mainReplayContextVo ->
                LOGGER
                    .info()
                    .log(
                        "MainReplay => {}", mainReplayContextVo.getStoredEventVo()))
        .flatMap(this::findHandler)
        .flatMap(this::replay)
        .flatMap(this::markStoredEventProcessingStatus)
        .map(this::dispatch)
        .subscribeOn(Schedulers.newElastic("MainReplay", 3))
        .subscribe();
  }

  private Mono<MainReplayContextVo> findHandler(final MainReplayContextVo mainReplayContextVo) {
    final var domainEventType = mainReplayContextVo.getStoredEventVo().getDomainEvent().getClass();
    final var matchedReplayer = MainStoredEventReplayerRegistry.matchReplayer(domainEventType);

    if (matchedReplayer.isEmpty()) {
      LOGGER.warn().log("Replay handler not found {}", mainReplayContextVo.getStoredEventVo());
      return Mono.just(mainReplayContextVo.noHandlerFound("Check MainStoredEventReplayerRegistry"));
    }

    final var mainStoredEventReplayerBean = Try.ofCallable(() -> Optional.of(applicationContext.getBean(matchedReplayer.get())))
            .getOrElseGet(throwable -> Optional.empty());

    mainReplayContextVo.mainStoredEventReplayerCallback(mainStoredEventReplayerBean);

    if(mainReplayContextVo.getMainStoredEventReplayerCallback().isEmpty()) {
      mainReplayContextVo.noHandlerFound("Check Spring Registry");
    }
    return Mono.just(mainReplayContextVo);
  }

  private Mono<MainReplayContextVo> replay(final MainReplayContextVo mainReplayContextVo) {
    if(mainReplayContextVo.getMainStoredEventReplayerCallback().isEmpty()) {
      LOGGER.warn().log("No MainStoredEventReplayerCallback found '{}', skipping {}", mainReplayContextVo.getReplayResult(),
              mainReplayContextVo.getStoredEventVo());
      return Mono.just(mainReplayContextVo);
    }

    final var mainStoredEventReplayerCallback =
            mainReplayContextVo.getMainStoredEventReplayerCallback().get();

    return mainStoredEventReplayerCallback.doReplay(mainReplayContextVo);
  }

  private Mono<MainReplayContextVo> markStoredEventProcessingStatus(
          final MainReplayContextVo mainReplayContextVo) {
    final var updateStoredEventCommand = mapProcessingResult(mainReplayContextVo);

    if (updateStoredEventCommand.isEmpty()) {
      LOGGER.debug().log("Stored Event Processing result not mapped for event store update: {}", mainReplayContextVo.getStoredEventVo());
      return Mono.just(mainReplayContextVo);
    }

    return eventStoreService.updateStatus(updateStoredEventCommand.get())
            .doOnSuccess(storedEventVo ->
                    LOGGER.debug().log("Stored Event replayed! {}", storedEventVo))
            .then(Mono.just(mainReplayContextVo));
  }

  private Mono<MainReplayContextVo> dispatch(MainReplayContextVo mainReplayContextVo) {
    final var secondaryReplayContextVo = SecondaryReplayContextVo.createContext(mainReplayContextVo.getStoredEventVo());
    sideReplayContextVoReplayProcessor.onNext(secondaryReplayContextVo);
    return Mono.just(mainReplayContextVo);
  }

  private Optional<UpdateStoredEventCommand> mapProcessingResult(final MainReplayContextVo mainReplayContextVo) {
    return switch (mainReplayContextVo.getReplayResult().getReplayResultType()) {
      case PROCESSED, NO_HANDLER_FOUND -> Optional.of(UpdateStoredEventCommand.builder()
              .id(mainReplayContextVo.getStoredEventVo().getId())
              .targetState(UpdateStoredEventCommand.TargetState.PROCESSED)
              .build());
      case ERROR -> Optional.of(UpdateStoredEventCommand.builder()
              .id(mainReplayContextVo.getStoredEventVo().getId())
              .targetState(UpdateStoredEventCommand.TargetState.FAILED)
              .build());
      case PROCESSING, PUBLISHED, IGNORED, MATCHED, UNMATCHED -> Optional.empty();
    };
  }
}
