package socialNetwork.service;

import socialNetwork.domain.models.Friendship;
import socialNetwork.domain.models.User;
import socialNetwork.domain.validators.EntityValidatorInterface;
import socialNetwork.repository.RepositoryInterface;
import socialNetwork.utilitaries.UnorderedPair;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * business layer for User model
 */
public class UserService {
    private RepositoryInterface<Long, User> userRepository;
    private RepositoryInterface<UnorderedPair<Long, Long>, Friendship> friendshipRepository;
    private EntityValidatorInterface<Long, User> userValidator;

    /**
     * constructor for user service
     * @param userRepository - repository of users
     * @param friendshipRepository - repository of friendships
     * @param userValidator - validator for User model
     */
    public UserService(RepositoryInterface<Long, User> userRepository,
                       RepositoryInterface<UnorderedPair<Long, Long>, Friendship> friendshipRepository,
                       EntityValidatorInterface<Long, User> userValidator) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.userValidator = userValidator;
    }

    /**
     * adds a user to the userRepository
     * @param firstName - String - first name of user
     * @param lastName - String - last name of user
     * @return empty Optional if the user was added, Optional containing the existing user with same id otherwise
     */
    public Optional<User> addUserService(String firstName, String lastName) {
        User user = new User(firstName, lastName);
        userValidator.validate(user);
        return userRepository.save(user);
    }

    /**
     * updates user from the user repository
     * @param id - identifier
     * @param firstName - String
     * @param lastName - String
     * @return optional with old value if user updated, empty optional otherwise
     */
    public Optional<User> updateUserService(Long id, String firstName, String lastName){
        User user = new User(id, firstName, lastName);
        userValidator.validate(user);
        return userRepository.update(user);
    }

    /**
     * finds a user in the user repository after its identifier
     * @param id - identifier of user that is searched for
     * @return - optional containing user with given id if exists, empty optional otherwise
     */
    public Optional<User> findUserService(Long id){
        return userRepository.find(id);
    }

    /**
     * @return - a list with all users
     * the list of friends for each user is empty
     */
    public List<User> getAllService(){
        return userRepository.getAll();
    }

    /**
     * removes the user with the given id and his friendships with other users
     * @param id - Long - identifier of the user
     * @return - Optional with the user that was removed, empty Optional if user did not exist
     * @throws IllegalArgumentException - id is null
     */
    public Optional<User> removeUserService(Long id){
        if(userRepository.find(id).isEmpty())
            return Optional.empty();
        List<Friendship> friendships = friendshipRepository.getAll();
        friendships.forEach(
                friendship -> {
                    if(friendship.hasUser(id))
                        friendshipRepository.remove(friendship.getId());
                }
        );
        return userRepository.remove(id);
    }

    /**
     * finds all friends for a certain user
     * @param idUser - Long - identifier for one user
     * @return - Map - key is Optional of user, value is LocalDateTime
     */
    public Map<Optional<User>, LocalDateTime> findAllFriendsForUserService(Long idUser){
        Map<Optional<User>, LocalDateTime> mapOfFriendships = new HashMap<>();
        List<Friendship> friendships = friendshipRepository.getAll();
        friendships.stream()
                .filter(friendship -> friendship.hasUser(idUser))
                .forEach(friendship -> {
                    Long idOfFriend;
                    if(friendship.getId().left == idUser)
                        idOfFriend = friendship.getId().right;
                    else
                        idOfFriend = friendship.getId().left;
                    mapOfFriendships.put(userRepository.find(idOfFriend), friendship.getDate());
                });
        return mapOfFriendships;
    }

    public Map<Optional<User>, LocalDateTime > findAllFriendsForUserMonthService(Long idUser,int month){
        Map<Optional<User>, LocalDateTime> friendshipsMonth = new HashMap<>();
        findAllFriendsForUserService(idUser).entrySet()
                .stream()
                .filter(entry -> entry.getValue().getMonth().getValue() == month )
                .forEach(entry -> {
                    friendshipsMonth.put(entry.getKey(),entry.getValue());
                });
        return friendshipsMonth;
    }
}
