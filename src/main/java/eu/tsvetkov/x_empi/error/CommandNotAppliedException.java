package eu.tsvetkov.x_empi.error;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class CommandNotAppliedException extends CommandException {
    public CommandNotAppliedException(String message) {
        super(message);
    }

    public CommandNotAppliedException(Throwable cause) {
        super(cause);
    }
}
