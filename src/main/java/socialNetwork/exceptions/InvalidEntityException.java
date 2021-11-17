package socialNetwork.exceptions;

/**
 * exception class that extends RuntimeException
 * exception used when validating an entity - user/friendship
 */
public class InvalidEntityException extends RuntimeException {

    /**
     * constructor with no parameters
     */
    public InvalidEntityException() {}

    /**
     * constructor with a message as parameter
     * @param message - String
     */
    public InvalidEntityException(String message) {
        super(message);
    }

    /** constructor with a message and a cause as parameters
     * @param message - String
     * @param cause - Throwable
     */
    public InvalidEntityException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * constructor with a cause as parameter
     * @param cause - Throwable
     */
    public InvalidEntityException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message - String
     * @param cause - Throwable
     * @param enableSuppression - true if we want to suppress an exception
     * @param writableStackTrace - boolean
     */
    public InvalidEntityException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
