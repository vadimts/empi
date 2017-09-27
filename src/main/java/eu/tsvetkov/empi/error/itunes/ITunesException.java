package eu.tsvetkov.empi.error;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class ITunesException extends CommandException {

    public ITunesException(String message, Throwable cause) {
        super(message, cause);
    }

    public ITunesException(String message) {
        super(message);
    }
}
