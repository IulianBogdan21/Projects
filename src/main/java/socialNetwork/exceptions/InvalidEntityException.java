package socialNetwork.exceptions;

/**
 * exception class that extends RuntimeException
 * exception used when validating an entity - user/friendship
 */
public class InvalidEntityException extends ExceptionBaseClass {

    /**
     * constructor with a message as parameter
     * @param message - String
     */
    public InvalidEntityException(String message) {
        super(message);
    }

}
