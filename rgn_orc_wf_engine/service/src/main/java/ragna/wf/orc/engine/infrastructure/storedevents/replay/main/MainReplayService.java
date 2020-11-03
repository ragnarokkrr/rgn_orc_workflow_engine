package ragna.wf.orc.engine.infrastructure.storedevents.replay.main;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.fissore.slf4j.FluentLogger;
import org.fissore.slf4j.FluentLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import ragna.wf.orc.common.events.DomainEvent;
import ragna.wf.orc.engine.domain.tasks.vo.CriteriaEvaluationResult;
import ragna.wf.orc.engine.infrastructure.config.FeatureTogglesConfigProperties;
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
  private final FeatureTogglesConfigProperties featureTogglesConfig;

  @PostConstruct
  public void init() {
    if (!featureTogglesConfig.isReplayEngineEnabled()) {
      LOGGER.info().log("MAIN REPLAY ENGINE: DISABLED!");
      return;
    }
    LOGGER.info().log("MAIN REPLAY ENGINE: ENABLED!");
    mainReplayContextVoReplayProcessor
            .doOnNext(
                    mainReplayContextVo ->
                            LOGGER
                                    .info()
                                    .log(
                                            "MainReplay => {}", mainReplayContextVo.getStoredEventVo()))
            .flatMap(this::findHandler)
            .flatMap(this::evaluateTaskActivationCriteria)
            .flatMap(this::replay)
            .flatMap(this::publishEventIfNecessary)
            .flatMap(this::markStoredEventProcessingStatus)
            .map(this::dispatchToSideReplay)
            .subscribeOn(Schedulers.newElastic("MainReplay", 3))
            .subscribe();
  }

  Mono<MainReplayContextVo> findHandler(final MainReplayContextVo mainReplayContextVo) {
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

  Mono<MainReplayContextVo> evaluateTaskActivationCriteria(final MainReplayContextVo mainReplayContextVo) {
    if (mainReplayContextVo.getMainStoredEventReplayerCallback().isEmpty()
            || mainReplayContextVo.getReplayResult().getReplayResultType() == MainReplayContextVo.ReplayResultEnum.NO_HANDLER_FOUND) {
      LOGGER.warn().log("No MainStoredEventReplayerCallback found '{}', skipping {}", mainReplayContextVo.getReplayResult(),
              mainReplayContextVo.getStoredEventVo());
      return Mono.just(mainReplayContextVo);
    }

    final var mainStoredEventReplayerCallback =
            (MainStoredEventReplayerCallback<? extends DomainEvent>) mainReplayContextVo.getMainStoredEventReplayerCallback().get();
    return mainStoredEventReplayerCallback.activateTaskIfConfigured(mainReplayContextVo);
  }

  Mono<MainReplayContextVo> replay(final MainReplayContextVo mainReplayContextVo) {
    if (mainReplayContextVo.getMainStoredEventReplayerCallback().isEmpty()
            || mainReplayContextVo.getReplayResult().getReplayResultType() == MainReplayContextVo.ReplayResultEnum.NO_HANDLER_FOUND) {
      LOGGER.warn().log("No MainStoredEventReplayerCallback found '{}', skipping {}", mainReplayContextVo.getReplayResult(),
              mainReplayContextVo.getStoredEventVo());
      return Mono.just(mainReplayContextVo);
    }

    final var mainStoredEventReplayerCallback =
            (MainStoredEventReplayerCallback<? extends DomainEvent>) mainReplayContextVo.getMainStoredEventReplayerCallback().get();

    return mainStoredEventReplayerCallback.doReplay(mainReplayContextVo).map(MainReplayContextVo::processed);
  }

  Mono<MainReplayContextVo> publishEventIfNecessary(final MainReplayContextVo mainReplayContextVo) {
    if (mainReplayContextVo.getCriteriaEvaluationResult().isEmpty()) {
      LOGGER.warn().log("Activation criteria criteria is empty for {}: ",
              mainReplayContextVo.getStoredEventVo());
      return Mono.just(mainReplayContextVo);
    }

    final var criteriaEvaluationResult = mainReplayContextVo.getCriteriaEvaluationResult().get();
    if (criteriaEvaluationResult.getCriteriaResultType() != CriteriaEvaluationResult.CriteriaResultType.MATCHED) {
      LOGGER.warn().log("Activation criteria unmatched for {}: ",
              mainReplayContextVo.getStoredEventVo(), criteriaEvaluationResult);
      return Mono.just(mainReplayContextVo.unmatched() );
    }

    final var mainStoredEventReplayerCallback =
            (MainStoredEventReplayerCallback<? extends DomainEvent>) mainReplayContextVo.getMainStoredEventReplayerCallback().get();
    return mainStoredEventReplayerCallback.publish(mainReplayContextVo)
            .map(MainReplayContextVo::published)
            .doOnNext(mainReplayContextVo1 -> LOGGER.info().log("PUBLISHED!!!! {}", mainReplayContextVo1));
  }

  Mono<MainReplayContextVo> markStoredEventProcessingStatus(
          final MainReplayContextVo mainReplayContextVo) {
    final var updateStoredEventCommand = mapReplayProcessingResult(mainReplayContextVo);

    if (updateStoredEventCommand.isEmpty()) {
      LOGGER.debug().log("Stored Event Processing result not mapped for event store update: {}", mainReplayContextVo.getStoredEventVo());
      return Mono.just(mainReplayContextVo);
    }

    return eventStoreService.updateStatus(updateStoredEventCommand.get())
            .doOnSuccess(storedEventVo ->
                    LOGGER.debug().log("Stored Event replayed! {}", storedEventVo))
            .then(Mono.just(mainReplayContextVo));
  }

  Mono<MainReplayContextVo> dispatchToSideReplay(MainReplayContextVo mainReplayContextVo) {
    final var secondaryReplayContextVo = SecondaryReplayContextVo.createContext(mainReplayContextVo.getStoredEventVo());
    sideReplayContextVoReplayProcessor.onNext(secondaryReplayContextVo);
    return Mono.just(mainReplayContextVo);
  }

  Optional<UpdateStoredEventCommand> mapReplayProcessingResult(final MainReplayContextVo mainReplayContextVo) {
    final var replayResultType = mainReplayContextVo.getReplayResult().getReplayResultType();
    LOGGER.debug().log("mapReplayProcessingResult() replayResultType={}, {}", replayResultType,mainReplayContextVo.getStoredEventVo() );
    return switch (replayResultType) {
      case PROCESSED, NO_HANDLER_FOUND -> Optional.of(buildUpdateStoredEventCommand(mainReplayContextVo, UpdateStoredEventCommand.TargetState.PROCESSED));
      case PUBLISHED -> Optional.of(buildUpdateStoredEventCommand(mainReplayContextVo, UpdateStoredEventCommand.TargetState.PUBLISHED));
      case UNMATCHED -> Optional.of(buildUpdateStoredEventCommand(mainReplayContextVo, UpdateStoredEventCommand.TargetState.UNPUBLISHED));
      case ERROR -> Optional.of(buildUpdateStoredEventCommand(mainReplayContextVo, UpdateStoredEventCommand.TargetState.FAILED));
      case PROCESSING, IGNORED, MATCHED -> Optional.empty();
    };
  }

  private UpdateStoredEventCommand buildUpdateStoredEventCommand(final MainReplayContextVo mainReplayContextVo, final UpdateStoredEventCommand.TargetState failed) {
    return UpdateStoredEventCommand.builder()
            .id(mainReplayContextVo.getStoredEventVo().getId())
            .targetState(failed)
            .build();
  }
}
