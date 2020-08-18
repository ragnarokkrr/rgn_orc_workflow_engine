package ragna.wf.orc.eventstore.service.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import ragna.wf.orc.eventstore.model.SerializationEngine;
import ragna.wf.orc.eventstore.model.StoredEventStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoredEventVo {
  @Id private Long id;
  private String objectId;
  private String typedName;
  private byte[] payload;
  private LocalDateTime occurredOn;
  private LocalDateTime processingOn;
  private LocalDateTime processedOn;
  private StoredEventStatus eventStatus;
  private SerializationEngine serializationEngine;
}
