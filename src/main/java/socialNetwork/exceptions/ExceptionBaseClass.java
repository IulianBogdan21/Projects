package socialNetwork.exceptions;

/**
 * exception base class that extends RuntimeException
 */
public class ExceptionBaseClass extends RuntimeException {
    /**
     * constructor
     * @param message - String
     */
    public ExceptionBaseClass(String message) {
        super(message);
    }
}
