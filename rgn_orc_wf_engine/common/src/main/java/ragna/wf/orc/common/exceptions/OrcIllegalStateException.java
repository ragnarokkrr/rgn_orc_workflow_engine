package ragna.wf.orc.common.exceptions;

public class OrcIllegalStateException extends OrcException {
  public OrcIllegalStateException() {}

  public OrcIllegalStateException(String message) {
    super(message);
  }

  public OrcIllegalStateException(String message, ErrorCode code) {
    super(message, code);
  }

  public OrcIllegalStateException(String message, ErrorCode code, Throwable throwable) {
    super(message, code, throwable);
  }
}
