package ragna.wf.orc.engine.infrastructure.storedevents.replay.main.vo;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ragna.wf.orc.eventstore.service.vo.StoredEventVo;

@Data
@RequiredArgsConstructor(staticName = "createContext")
@Setter(AccessLevel.NONE)
public class MainReplayContextVo {
  private final StoredEventVo storedEventVo;
  private ReplayResult replayResult;

  public enum ReplayResultEnum {
    UNMATCHED,
    MATCHED,
    PROCESSED,
    PUBLISHED,
    ERROR,
    IGNORED
  }

  @Data
  public static class ReplayResult {
    private String message;
    private ReplayResultEnum replayresult;
  }
}
