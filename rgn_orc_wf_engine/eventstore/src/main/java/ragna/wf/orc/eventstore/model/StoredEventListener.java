package ragna.wf.orc.eventstore.model;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;
import ragna.wf.orc.common.data.mongodb.sequences.SequenceGenerator;

import java.util.UUID;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StoredEventListener extends AbstractMongoEventListener<StoredEvent> {
  private final SequenceGenerator sequenceGenerator;

  @Override
  public void onBeforeConvert(BeforeConvertEvent<StoredEvent> event) {
    final var source = event.getSource();
    if (source.getId() == null || StringUtils.isNoneBlank(source.getId())) {
      source.setId(generateUUId());
    }
  }

  private String generateUUId() {
    return UUID.randomUUID().toString();
  }

  // TODO transaction does not finish and hangs if we call generateSequence
  // see SequenceGenerator
  private long generateId() {
    return sequenceGenerator.generateSequence(StoredEvent.SEQUENCE_NAME);
  }
}
