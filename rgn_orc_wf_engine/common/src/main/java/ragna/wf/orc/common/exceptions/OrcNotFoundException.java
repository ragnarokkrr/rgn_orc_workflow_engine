package ragna.wf.orc.common.exceptions;

public class OrcNotFoundException extends OrcException {
  public OrcNotFoundException(String message, ErrorCode code) {
    super(message, code);
  }
}
