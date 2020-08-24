package ragna.wf.orc.eventstore.service.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
@AllArgsConstructor
@Builder
public class UpdateStoredEventCommand {
  private final Long id;
  private final TargetState targetState;
  @Builder.Default private final String message = StringUtils.EMPTY;

  public enum TargetState {
    PROCESSED,
    PUBLISHED,
    UNPUBLISHED,
    FAILED
  }
}
