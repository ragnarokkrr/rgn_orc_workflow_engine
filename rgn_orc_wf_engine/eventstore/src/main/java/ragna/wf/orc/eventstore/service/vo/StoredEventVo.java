package ragna.wf.orc.eventstore.service.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ragna.wf.orc.common.events.DomainEvent;

import java.time.LocalDateTime;
import java.util.StringJoiner;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoredEventVo {
  private Long id;
  private String objectId;
  private String typedName;
  private byte[] payload;
  private DomainEvent domainEvent;
  private LocalDateTime occurredOn;
  private LocalDateTime processingOn;
  private LocalDateTime processedOn;
  private StoredEventStatus eventStatus;
  private SerializationEngine serializationEngine;

  public String shortToString() {
    return new StringJoiner(", ", StoredEventVo.class.getSimpleName() + "[", "]")
        .add("id=" + id)
        .add("objectId='" + objectId + "'")
        .add("typedName='" + typedName + "'")
        .add("eventStatus=" + eventStatus)
        .add("occurredOn=" + occurredOn)
        .add("processingOn=" + processingOn)
        .add("processedOn=" + processedOn)
        .toString();
  }

  public StoredEventVo processed() {
    this.eventStatus = StoredEventStatus.PROCESSED;
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
