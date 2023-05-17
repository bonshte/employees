package employees.exception;

public class CorruptedFileContentException extends RuntimeException {
    public CorruptedFileContentException(String msg) {
        super(msg);
    }

    public CorruptedFileContentException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
