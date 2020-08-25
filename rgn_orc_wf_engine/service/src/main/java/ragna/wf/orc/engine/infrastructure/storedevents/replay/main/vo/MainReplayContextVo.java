package ragna.wf.orc.engine.infrastructure.storedevents.replay.main.vo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import ragna.wf.orc.engine.application.replay.WorkflowRootCreatedReplayer;
import ragna.wf.orc.eventstore.service.vo.StoredEventVo;

import java.util.Optional;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Setter(AccessLevel.NONE)
public class MainReplayContextVo {
  private final StoredEventVo storedEventVo;
  private ReplayResult replayResult;
  private MatchResult matchResult;
  private Optional<? extends WorkflowRootCreatedReplayer> mainStoredEventReplayerCallback;

  public static MainReplayContextVo createContext(final StoredEventVo storedEventVo) {
    final var mainReplayContextVo = new MainReplayContextVo(storedEventVo);
    mainReplayContextVo.replayResult =
        ReplayResult.builder().replayResultType(ReplayResultEnum.PROCESSING).build();
    mainReplayContextVo.matchResult =
        MatchResult.builder().matchResultType(MatchResultEnum.PROCESSING).build();
    mainReplayContextVo.mainStoredEventReplayerCallback = Optional.empty();
    return mainReplayContextVo;
  }

  public MainReplayContextVo mainStoredEventReplayerCallback(
      final Optional<? extends WorkflowRootCreatedReplayer> mainStoredEventReplayerCallback) {
    this.mainStoredEventReplayerCallback = mainStoredEventReplayerCallback;
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

  public MainReplayContextVo noHandlerFound(final String message) {
    this.replayResult =
        ReplayResult.builder()
            .message(message)
            .replayResultType(ReplayResultEnum.NO_HANDLER_FOUND)
            .build();

    return this;
  }

  public enum MatchResultEnum {
    MATCHED,
    UNMATCHED,
    DEFAULT,
    PROCESSING
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
