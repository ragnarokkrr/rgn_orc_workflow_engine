package ragna.wf.orc.engine.infrastructure.storedevents.replay.vo;

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
}
