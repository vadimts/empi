package eu.tsvetkov.empi.error;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class FileException extends Exception {
    public FileException(String message, Throwable cause) {
        super(message, cause);
    }
}
