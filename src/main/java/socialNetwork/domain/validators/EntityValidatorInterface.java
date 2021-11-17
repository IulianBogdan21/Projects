package socialNetwork.domain.validators;

import socialNetwork.domain.models.Entity;
import socialNetwork.exceptions.InvalidEntityException;

/**
 * interface that defines a validator for a model
 * @param <ID> - type of the identifier of the model
 * @param <E> - type of the model that is a subclass of Entity and has the identifier ID
 */
public interface EntityValidatorInterface<ID, E extends Entity<ID>> {
    /**
     * validates the given entity
     * @param entity - entity to be validated
     * throws {@link IllegalArgumentException} - entity is null
     * throws {@link InvalidEntityException} - invalid entity
     */
    void validate(E entity);
}
