package ragna.wf.orc.eventstore.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationLogDto {
  private String notificationId;
  @Builder.Default
  private List<NotificationDto> notifications = new ArrayList<>();
  private String next;
  private String previous;
  private Boolean archived;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class NotificationDto {
    private String id;
    private String objectId;
    private String typedName;
    private LocalDateTime occurredOn;
    private LocalDateTime processedOn;
    private LocalDateTime processingOn;
    private StoredEventStatus status;
    private SerializationEngine serializationEngine;
    private String payload;

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

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class NotificationLogId {
    private Long low;
    private Long high;

    public String encode() {
      return String.format("%s,%s", this.low, this.high);
    }
  }
}
