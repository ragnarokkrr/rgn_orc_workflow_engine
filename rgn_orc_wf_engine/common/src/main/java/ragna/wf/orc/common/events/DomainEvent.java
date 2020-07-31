package ragna.wf.orc.common.events;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import ragna.wf.orc.common.serialization.DefaultKryoContext;
import ragna.wf.orc.common.serialization.KryoContext;

import java.time.LocalDateTime;

@Data
@Setter(AccessLevel.NONE)
public class DomainEvent {
    private static final KryoContext KRYO = DefaultKryoContext.kryoContextWithDefaultSerializers();
    private String eventName;
    private String objectId;
    private Object source;
    private String action;
    private LocalDateTime timestamp;

    protected DomainEvent() {
    }

    protected DomainEvent(final Object source, final String objectId) {
        this();
        this.objectId = objectId;
        this.eventName = getClass().getName();
        this.timestamp = LocalDateTime.now();
        this.source = createSnapshot(source);
    }

    protected DomainEvent(final Object source, final String objectId, final String action) {
        this(source, objectId);
        this.action = action;
    }

    private Object createSnapshot(Object source) {
        return KRYO.copy(source);
    }
}
