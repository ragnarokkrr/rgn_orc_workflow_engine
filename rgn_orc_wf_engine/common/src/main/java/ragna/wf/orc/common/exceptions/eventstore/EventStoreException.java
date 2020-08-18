package ragna.wf.orc.common.exceptions.eventstore;

import ragna.wf.orc.common.exceptions.ErrorCode;

public class EventStoreException extends RuntimeException {
  private final ErrorCode code;

  public EventStoreException() {
    super(
        String.format(
            "%s: %s", ErrorCode.GENERAL_ERROR.getCode(), ErrorCode.GENERAL_ERROR.getMessage()));
    this.code = ErrorCode.GENERAL_ERROR;
  }

  public EventStoreException(String message) {
    super(String.format("%s: %s", ErrorCode.GENERAL_ERROR.getCode(), message));
    this.code = ErrorCode.GENERAL_ERROR;
  }

  public EventStoreException(String message, ErrorCode code) {
    super(String.format("%s: %s", code.getCode(), message));
    this.code = code;
  }

  public EventStoreException(String message, ErrorCode code, Throwable throwable) {
    super(String.format("%s: %s", code.getCode(), message), throwable);
    this.code = code;
  }
}
