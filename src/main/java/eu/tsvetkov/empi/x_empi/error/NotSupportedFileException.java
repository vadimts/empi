package eu.tsvetkov.empi.x_empi.error;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
public class NotSupportedFileException extends Mp3Exception {
    public NotSupportedFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotSupportedFileException(String message) {
        super(message);
    }
}
