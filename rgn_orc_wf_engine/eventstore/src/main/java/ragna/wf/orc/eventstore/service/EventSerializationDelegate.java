package ragna.wf.orc.eventstore.service;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ragna.wf.orc.common.events.DomainEvent;
import ragna.wf.orc.common.exceptions.eventstore.SerializationException;
import ragna.wf.orc.common.serialization.KryoContext;
import ragna.wf.orc.eventstore.model.SerializationEngine;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EventSerializationDelegate {
  private final KryoContext kryoContext;
  private final SerializationEngine serializationEngine = SerializationEngine.KRYO;

  public byte[] serializeEvent(final DomainEvent sourceObject) {
    return Try.of(() -> this.kryoContext.serialize(sourceObject))
        .getOrElseThrow(
            t -> new SerializationException("Error serializing object to EVENT STORE", t));
  }

  public DomainEvent deserialize(final String className, final byte[] payload) {
    final var clazz =
        Try.of(() -> Class.forName(className))
            .getOrElseThrow(
                t ->
                    new SerializationException(
                        String.format("Cannot deserialize: %s", className), t));

    return (DomainEvent) this.kryoContext.deserialize(clazz, payload);
  }

  public SerializationEngine getSerializationEngine() {
    return serializationEngine;
  }
}
