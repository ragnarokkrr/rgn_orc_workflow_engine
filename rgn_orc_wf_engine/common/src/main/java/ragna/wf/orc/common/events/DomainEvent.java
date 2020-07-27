package ragna.wf.orc.common.events;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class DomainEvent {
    private String eventName;
    private String objectId;
    private Object source;
    private long timestamp;
}
