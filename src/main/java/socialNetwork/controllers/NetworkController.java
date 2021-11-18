package socialNetwork.controllers;

import socialNetwork.domain.models.Friendship;
import socialNetwork.domain.models.User;
import socialNetwork.service.NetworkService;
import socialNetwork.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * controller between ui and business layer
 */
public class NetworkController {
    private UserService userService;
    private NetworkService networkService;

    /**
     * constructor - creates a controller that accesses the given services
     * @param userService - service for users
     * @param networkService - service for friendships
     */
    public NetworkController(UserService userService, NetworkService networkService) {
        this.userService = userService;
        this.networkService = networkService;
    }

    /**
     * adds a new user
     * @param id - int
     * @param firstName - String
     * @param lastName - String
     * @return - empty Optional if the user was added, Optional containing the existing user otherwise
     */
    public Optional<User> addUser(Long id, String firstName, String lastName){
        return userService.addUserService(id, firstName, lastName);
    }

    /**
     * update user
     * @param id - Long
     * @param firstName - String
     * @param lastName - String
     * @return - old value Optional if the user was updated, empty Optional otherwise
     */
    public Optional<User> updateUser(Long id, String firstName, String lastName){
        return userService.updateUserService(id, firstName, lastName);
    }

    /**
     * removes the user with the given id and his friendships with other users
     * @param id - int
     * @return Optional with the user that was removed, empty optional if user did not exist
     */
    public Optional<User> removeUser(Long id){
        return userService.removeUserService(id);
    }

    /**
     * finds user with given id
     * @param id - identifier of user we search for
     * @return - Optional containing user that has given id if exists, empty optional otherwise
     */
    public Optional<User> findUser(Long id){
        return userService.findUserService(id);
    }

    /**
     * adds a friendship between 2 users
     * @param firstUserId - id first user
     * @param secondUserId - id second user
     * @return - empty Optional if the friendship was added, empty if friendship already exists
     * @throws socialNetwork.exceptions.InvalidEntityException - one of users not found
     */
    public Optional<Friendship> addFriendship(Long firstUserId, Long secondUserId, LocalDateTime date){
        return networkService.addFriendshipService(firstUserId, secondUserId, date);
    }

    /**
     * removes the friendship between 2 users
     * @param firstUserId - id first user
     * @param secondUserId - id second user
     * @return Optional containing removed friendship, empty Optional if users do not exist
     */
    public Optional<Friendship> removeFriendship(Long firstUserId, Long secondUserId){
        return networkService.removeFriendshipService(firstUserId, secondUserId);
    }

    /**
     * finds a friendships after id of 2 users
     * @param firstUserId - Long
     * @param secondUserId - Long
     * @return Optional containing friendship if exists, empty optional otherwise
     */
    public Optional<Friendship> findFriendship(Long firstUserId, Long secondUserId){
        return networkService.findFriendshipService(firstUserId, secondUserId);
    }

    /**
     * @return - a list of users and all their friends are set
     */
    public List<User> getAllUsersAndTheirFriends(){
        return networkService.getAllUsersAndTheirFriendsService();
    }

    /**
     * @return - number of communities in the network
     */
    public int getNumberOfCommunitiesInNetwork(){
        return networkService.getNumberOfCommunitiesService();
    }

    /**
     * @return - list of the users of the most social community
     */
    public List<User> getMostSocialCommunity(){
        return networkService.getMostSocialCommunity();
    }

    /**
     * @param idUser - Long - identifier for user
     * @return Map key is Optional of user, value is LocalDateTime
     */
    public Map<Optional<User>, LocalDateTime> findAllFriendshipsForUser(Long idUser){
        return userService.findAllFriendsForUserService(idUser);
    }

    public Map<Optional<User>, LocalDateTime> findAllFriendsForUserMonth(Long idUser,int month){
        return userService.findAllFriendsForUserMonthService(idUser,month);
    }
}
