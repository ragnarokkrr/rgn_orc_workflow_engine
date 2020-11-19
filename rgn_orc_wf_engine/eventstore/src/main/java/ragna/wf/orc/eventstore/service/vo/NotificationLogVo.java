package ragna.wf.orc.eventstore.service.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationLogVo {
  private String notificationId;
  @Builder.Default private List<NotificationVo> notifications = new ArrayList<>();
  private String next;
  private String previous;
  private Boolean archived;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class NotificationVo {
    private Long id;
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

    private NotificationLogId(String aNotificationLogId) {
      super();
      String[] textIds = aNotificationLogId.split(",");
      this.setLow(Long.parseLong(textIds[0]));
      this.setHigh(Long.parseLong(textIds[1]));
    }

    public static NotificationLogId createNotificationLogId(String aNotificationLogId) {
      return new NotificationLogId(aNotificationLogId);
    }

    public NotificationLogId next(Long next) {
      return NotificationLogId.builder().high(this.high + next).low(this.low + next).build();
    }

    public NotificationLogId previous(Long previous) {
      boolean firstPage = this.low <= 1;
      if (firstPage) {
        return new NotificationLogId(encode());
      }

      return NotificationLogId.builder()
          .high(this.high - previous)
          .low(this.low - previous)
          .build();
    }

    public String encode() {
      return String.format("%s,%s", this.low, this.high);
    }
  }
}
