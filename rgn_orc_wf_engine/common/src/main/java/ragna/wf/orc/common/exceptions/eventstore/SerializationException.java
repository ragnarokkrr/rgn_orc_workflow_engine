package ragna.wf.orc.common.exceptions.eventstore;

import ragna.wf.orc.common.exceptions.ErrorCode;

public class SerializationException extends EventStoreException {
  public SerializationException(String message, Throwable throwable) {
    super(
        String.format("%s: %s", ErrorCode.EVS_SERIALIZATION_ERROR.getMessage(), message),
        ErrorCode.EVS_SERIALIZATION_ERROR,
        throwable);
  }
}
