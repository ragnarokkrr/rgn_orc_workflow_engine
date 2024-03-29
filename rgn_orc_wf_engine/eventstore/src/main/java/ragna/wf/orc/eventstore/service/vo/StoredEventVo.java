package ragna.wf.orc.eventstore.service.vo;

import java.time.LocalDateTime;
import java.util.StringJoiner;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ragna.wf.orc.common.events.DomainEvent;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoredEventVo {
  private Long id;
  private String objectId;
  private String typedName;
  private byte[] payload;
  private String errorMessage;
  private DomainEvent domainEvent;
  private LocalDateTime occurredOn;
  private LocalDateTime processingOn;
  private LocalDateTime processedOn;
  private StoredEventStatus eventStatus;
  private SerializationEngine serializationEngine;

  public String toString() {
    return new StringJoiner(", ", StoredEventVo.class.getSimpleName() + "[", "]")
        .add("id=" + id)
        .add("objectId='" + objectId + "'")
        .add("typedName='" + typedName + "'")
        .add("eventStatus=" + eventStatus)
        .add("occurredOn=" + occurredOn)
        .add("processingOn=" + processingOn)
        .add("processedOn=" + processedOn)
        .add("domainEventExists=" + (domainEvent != null))
        .add("payloadExists=" + (payload != null))
        .toString();
  }

  public StoredEventVo processed() {
    this.eventStatus = StoredEventStatus.PROCESSED;
    this.processedOn = LocalDateTime.now();
    return this;
  }

  public StoredEventVo published() {
    this.eventStatus = StoredEventStatus.PUBLISHED;
    this.processedOn = LocalDateTime.now();
    return this;
  }

  public enum StoredEventStatus {
    UNPROCESSED,
    PROCESSING,
    PROCESSED,
    PUBLISHED,
    UNPUBLISHED,
    FAILED
  }

  public enum SerializationEngine {
    KRYO
  }
}
