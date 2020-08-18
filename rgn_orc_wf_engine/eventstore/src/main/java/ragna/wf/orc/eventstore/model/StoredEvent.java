package ragna.wf.orc.eventstore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.StringJoiner;

@Document(collection = "stored_events")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoredEvent {
  static final String SEQUENCE_NAME = "stored_events_seq";
  @Id private String id;
  private String objectId;
  private String typedName;
  private byte[] payload;
  private LocalDateTime occurredOn;
  private LocalDateTime processingOn;
  private LocalDateTime processedOn;
  private StoredEventStatus eventStatus;
  private SerializationEngine serializationEngine;

  private StoredEvent(
      final String objectId,
      final String typedName,
      final byte[] payload,
      final LocalDateTime occurredOn,
      final SerializationEngine serializationEngine) {
    this.objectId = objectId;
    this.typedName = typedName;
    this.payload = payload;
    this.occurredOn = occurredOn;
    this.eventStatus = StoredEventStatus.UNPROCESSED;
    this.serializationEngine = serializationEngine;
  }

  public static StoredEvent createStoredEvent(
      final String objectId,
      final String typedName,
      final byte[] payload,
      final LocalDateTime occurredOn,
      final SerializationEngine serializationEngine) {
    Assert.notNull(objectId, "objectId must not be null.");
    Assert.notNull(typedName, "typedName must not be null.");
    Assert.notNull(payload, "payload must not be null.");
    Assert.notNull(occurredOn, "occurredOn must  not be null.");
    Assert.notNull(serializationEngine, "serializationEngine must  not be null.");

    return new StoredEvent(objectId, typedName, payload, occurredOn, serializationEngine);
  }

  public String shortToString() {
    return new StringJoiner(", ", StoredEvent.class.getSimpleName() + "[", "]")
        .add("id=" + id)
        .add("objectId='" + objectId + "'")
        .add("typedName='" + typedName + "'")
        .add("eventStatus=" + eventStatus)
        .add("occurredOn=" + occurredOn)
        .add("processingOn=" + processingOn)
        .add("processedOn=" + processedOn)
        .toString();
  }
}
