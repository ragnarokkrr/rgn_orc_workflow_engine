package ragna.wf.orc.common.exceptions;

public class OrcException extends RuntimeException {
    private final ErrorCode code;

    public OrcException() {
        super(String.format("%s: %s", ErrorCode.GENERAL_ERROR.getCode(), ErrorCode.GENERAL_ERROR.getMessage()));
        this.code = ErrorCode.GENERAL_ERROR;
    }

    public OrcException(String message) {
        super(String.format("%s: %s", ErrorCode.GENERAL_ERROR.getCode(), message));
        this.code = ErrorCode.GENERAL_ERROR;
    }

    public OrcException(String message, ErrorCode code) {
        super(String.format("%s: %s", code.getCode(), message));
        this.code = code;
    }

    public OrcException(String message, ErrorCode code, Throwable throwable) {
        super(String.format("%s: %s", code.getCode(), message), throwable);
        this.code = code;
    }

}
