package ragna.wf.orc.engine.infrastructure.storedevents.replay.main.vo;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import ragna.wf.orc.engine.domain.tasks.vo.CriteriaEvaluationResult;
import ragna.wf.orc.engine.infrastructure.storedevents.replay.main.MainStoredEventReplayerCallback;
import ragna.wf.orc.eventstore.service.vo.StoredEventVo;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Setter(AccessLevel.NONE)
public class MainReplayContextVo {
  private final StoredEventVo storedEventVo;
  private ReplayResult replayResult;
  private MatchResult matchResult;
  private Optional<CriteriaEvaluationResult> criteriaEvaluationResult;
  private Optional<? extends MainStoredEventReplayerCallback> mainStoredEventReplayerCallback;

  public static MainReplayContextVo createContext(final StoredEventVo storedEventVo) {
    final var mainReplayContextVo = new MainReplayContextVo(storedEventVo);
    mainReplayContextVo.replayResult =
        ReplayResult.builder().replayResultType(ReplayResultEnum.PROCESSING).build();
    mainReplayContextVo.matchResult =
        MatchResult.builder().matchResultType(MatchResultEnum.PROCESSING).build();
    mainReplayContextVo.mainStoredEventReplayerCallback = Optional.empty();
    mainReplayContextVo.criteriaEvaluationResult = Optional.empty();
    return mainReplayContextVo;
  }

  public MainReplayContextVo mainStoredEventReplayerCallback(
      final Optional<? extends MainStoredEventReplayerCallback> mainStoredEventReplayerCallback) {
    this.mainStoredEventReplayerCallback = mainStoredEventReplayerCallback;
    return this;
  }

  public MainReplayContextVo matchDefault() {
    this.matchResult = MatchResult.builder().matchResultType(MatchResultEnum.DEFAULT).build();
    return this;
  }

  public MainReplayContextVo processed() {
    this.replayResult =
        MainReplayContextVo.ReplayResult.builder()
            .replayResultType(ReplayResultEnum.PROCESSED)
            .build();
    return this;
  }

  public MainReplayContextVo errorProcessing(final String message) {
    this.replayResult =
        MainReplayContextVo.ReplayResult.builder()
            .message(message)
            .replayResultType(ReplayResultEnum.ERROR)
            .build();
    return this;
  }

  public MainReplayContextVo matchResult(final MatchResult matchResult) {
    this.matchResult = matchResult;
    return this;
  }

  public MainReplayContextVo criteriaEvaluationResult(
      final CriteriaEvaluationResult criteriaEvaluationResult) {
    this.criteriaEvaluationResult = Optional.of(criteriaEvaluationResult);
    return this;
  }

  public MainReplayContextVo noHandlerFound(final String message) {
    this.replayResult =
        ReplayResult.builder()
            .message(message)
            .replayResultType(ReplayResultEnum.NO_HANDLER_FOUND)
            .build();

    return this;
  }

  public MainReplayContextVo published() {
    this.replayResult = ReplayResult.builder().replayResultType(ReplayResultEnum.PUBLISHED).build();
    return this;
  }

  public MainReplayContextVo unmatched() {
    this.replayResult = ReplayResult.builder().replayResultType(ReplayResultEnum.UNMATCHED).build();
    return this;
  }

  public enum MatchResultEnum {
    PROCESSING,
    MATCHED,
    UNMATCHED,
    DEFAULT,
    TASK_NOT_FOUND,
    TASK_CONFIGURATION_NOT_FOUND,
    NO_CRITERIA_FOUND,
    ERROR
  }

  public enum ReplayResultEnum {
    UNMATCHED,
    MATCHED,
    PROCESSED,
    PUBLISHED,
    ERROR,
    IGNORED,
    PROCESSING,
    NO_HANDLER_FOUND
  }

  @Data
  @Builder
  @AllArgsConstructor
  public static class MatchResult {
    @Builder.Default private String message = StringUtils.EMPTY;
    private MatchResultEnum matchResultType;
  }

  @Data
  @Builder
  @AllArgsConstructor
  public static class ReplayResult {
    @Builder.Default private String message = StringUtils.EMPTY;
    private ReplayResultEnum replayResultType;
  }
}
