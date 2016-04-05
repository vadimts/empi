package eu.tsvetkov.empi.error;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class CommandException extends Exception {
    public CommandException(Throwable cause) {
        super(cause);
    }

    public CommandException(String message) {
        super(message);
    }
}
