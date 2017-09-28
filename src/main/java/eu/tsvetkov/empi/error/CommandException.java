package eu.tsvetkov.empi.error;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class CommandException extends Exception {
    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandException(String message) {
        super(message);
    }

    public CommandException(Throwable cause) {
        super(cause);
    }
}
