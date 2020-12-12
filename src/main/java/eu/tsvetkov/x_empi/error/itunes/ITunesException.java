package eu.tsvetkov.x_empi.error.itunes;

import eu.tsvetkov.x_empi.error.CommandException;

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
