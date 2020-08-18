package ragna.wf.orc.eventstore.model;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;
import ragna.wf.orc.common.data.mongodb.sequences.SequenceGenerator;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StoredEventListener extends AbstractMongoEventListener<StoredEvent> {
  private final SequenceGenerator sequenceGenerator;

  @Override
  public void onBeforeConvert(BeforeConvertEvent<StoredEvent> event) {
    final var source = event.getSource();
    if (source.getId() == null) {
      source.setId(sequenceGenerator.nextId());
    }
  }
}
