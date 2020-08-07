package ragna.wf.orc.common.exceptions;

public class OrcInvalidArgument extends OrcException {
  public OrcInvalidArgument(String message) {
    super(message);
  }

  public OrcInvalidArgument(String message, ErrorCode code) {
    super(message, code);
  }
}
