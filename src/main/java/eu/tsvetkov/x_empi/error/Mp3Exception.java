package eu.tsvetkov.x_empi.error;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class Mp3Exception extends Exception {
    public Mp3Exception(String message, Throwable cause) {
        super(message, cause);
    }

    public Mp3Exception(String message) {
        super(message);
    }
}
