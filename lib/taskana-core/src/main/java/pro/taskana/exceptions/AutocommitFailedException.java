package pro.taskana.exceptions;

/**
 * Thrown in ConnectionManagementMode AUTOCOMMIT when an attempt to commit fails.
 *
 */
@SuppressWarnings("serial")
public class AutocommitFailedException extends RuntimeException {
    public AutocommitFailedException(Throwable cause) {
        super("Autocommit failed", cause);
    }

}
