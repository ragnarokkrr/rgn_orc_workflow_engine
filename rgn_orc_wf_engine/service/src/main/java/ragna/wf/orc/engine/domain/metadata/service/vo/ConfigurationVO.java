package ragna.wf.orc.engine.domain.metadata.service.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder(toBuilder = true)
@Setter(AccessLevel.NONE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigurationVO {

  private String id;
  private LocalDateTime date;
  private Status status;

  private List<ConfiguredTaskVO> configuredTasks;

  public enum Status {
    ACTIVE,
    CLOSED
  }
}
