package socialNetwork.domain.models;

import java.util.Objects;

/**
 * class Entity - base class for every model of the application
 * @param <ID> - generic type parameter
 */
public class Entity<ID> {
    private ID id;

    /**
     * constructor
     * @param id - generic type parameter
     */
    public Entity(ID id) {
        this.id = id;
    }

    /**
     * getter method for idEntity
     * @return - ID - generic type
     */
    public ID getId(){
        return this.id;
    }

    /**
     * setter for id private attribute
     * @param newId - a generic type
     */
    public void setIdEntity(ID newId){
        this.id = newId;
    }

    /**
     * overrides Object class method equals - comparing two instances of this class
     * @param o - Object instance
     * @return - true, if objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?> entity = (Entity<?>) o;
        return Objects.equals(id, entity.id);
    }

    /**
     * overrides Object class method hashCode - returns the hash code of an object
     * @return - int
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * overrides Object class method toString - specific printing of an Entity instance
     * @return String
     */
    @Override
    public String toString() {
        return "ID: " + id;
    }
}
