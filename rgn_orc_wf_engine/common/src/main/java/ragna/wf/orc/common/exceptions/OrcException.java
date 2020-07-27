package ragna.wf.orc.common.exceptions;

import org.apache.commons.lang3.StringUtils;

public class OrcException extends RuntimeException {
    private final String code;

    public OrcException() {
        super("Orc Exception Occured");
        this.code = StringUtils.EMPTY;
    }

    public OrcException(String message) {
        super(message);
        this.code = StringUtils.EMPTY;
    }

    public OrcException(String message, String code) {
        super(message);
        this.code = code;
    }

    public OrcException(String message, String code, Throwable throwable) {
        super(message, throwable);
        this.code = code;
    }

}
