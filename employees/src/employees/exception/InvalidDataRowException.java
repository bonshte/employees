package employees.exception;

public class InvalidDataRowException extends Exception {
    public InvalidDataRowException(String msg) {
        super(msg);
    }

    public InvalidDataRowException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
