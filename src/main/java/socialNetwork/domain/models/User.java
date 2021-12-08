package socialNetwork.domain.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * class User - user entities for our social network
 */
public class User extends Entity<Long> {
    private String firstName;
    private String lastName;
    private List<User> listOfFriends = new ArrayList<User>();

    /**
     * constructor for User class
     * @param firstName - String
     * @param lastName - String
     */
    public User(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * constructor that also sets the id of the user
     * @param id Long
     * @param firstName String
     * @param lastName String
     */
    public User(Long id, String firstName, String lastName){
        setIdEntity(id);
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public User(User copyUser){
        setIdEntity(copyUser.getId());
        this.firstName = copyUser.firstName;
        this.lastName = copyUser.lastName;
    }

    /**
     * getter method for the firstName of the user
     * @return - String
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * setter method for the first name of the user
     * @param firstName - String
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * getter method for the last name of the user
     * @return - String
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * setter method for the last name of the user
     * @param lastName - String
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * getter method for the list of friends that a user has
     * @return - List = list of friends
     */
    public List<User> getListOfFriends() {
        return listOfFriends;
    }

    /**
     * setter for the list of friends of a user
     * @param copyListOfFriends - list of users
     */
    public void setListOfFriends(List<User> copyListOfFriends){
        this.listOfFriends = copyListOfFriends;
    }

    /**
     * overrides toString method for a specific printing of a user
     * @return String
     */
    @Override
    public String toString() {
        String userFormat = ", First name: %s, Last name: %s";
        return super.toString().concat(String.format(userFormat, firstName, lastName));
    }

    /**
     * overrides equals method of Object class
     * @param o - instance of Object class
     * @return - true if 2 users are equal (same values for their fields)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(firstName, user.firstName) &&
                Objects.equals(lastName, user.lastName);
    }

    /**
     * overrides hashCode method of class Object
     * @return - int - hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName);
    }
}
