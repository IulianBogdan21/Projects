package socialNetwork.exceptions;

public class EntityMissingValidationException extends InvalidEntityException{
    /**
     * constructor
     * @param message - String
     */
    public EntityMissingValidationException(String message) {
        super(message);
    }
}
