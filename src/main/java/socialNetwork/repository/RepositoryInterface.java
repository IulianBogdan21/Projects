package socialNetwork.repository;

import socialNetwork.domain.models.Entity;
import socialNetwork.exceptions.InvalidEntityException;

import java.util.List;
import java.util.Optional;

/**
 * repository interface class - for CRUD operations
 * @param <ID> - type E must have an attribute of type ID
 * @param <E> -  type of entities saved in repository
 */
public interface RepositoryInterface<ID, E extends Entity<ID>>{

    /**
     * searches an entity after its id
     * @param idSearchedEntity - id - must not be null
     * @return Optional containing the found entity, an empty Optional if entity doesn't exist
     */
    Optional<E> find(ID idSearchedEntity);

    /**
     * @return - all entities
     */
    List<E> getAll();

    /**
     * saves the given entity
     * @param entityToSave - subclass of Entity, must not be null
     * @return empty Optional if the entityToSave was saved, Optional containing the existing entity otherwise
     */
    Optional<E> save(E entityToSave);

    /**
     * removes the entity with the given id
     * @param idEntity - ID, must not be null
     * @return Optional containing the old entity, empty Optional if entity to remove doesn't exist
     */
    Optional<E> remove(ID idEntity);

    /**
     * updates the entity with the given id and sets it to entityToUpdate
     * @param entityToUpdate - subclass of Entity, must not be null
     * @return Optional containing the old entity, empty Optional if entityToUpdate doesn't exist
     */
    Optional<E> update(E entityToUpdate);
}
