package socialNetwork.service;

import socialNetwork.domain.models.Friendship;
import socialNetwork.domain.models.InvitationStage;
import socialNetwork.domain.models.User;
import socialNetwork.domain.validators.EntityValidatorInterface;
import socialNetwork.exceptions.EntityMissingValidationException;
import socialNetwork.exceptions.InvitationStatusException;
import socialNetwork.repository.RepositoryInterface;
import socialNetwork.utilitaries.UndirectedGraph;
import socialNetwork.utilitaries.UnorderedPair;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * business layer for Friendship model
 */
public class NetworkService {
    private final RepositoryInterface<UnorderedPair<Long, Long>, Friendship> friendshipRepository;
    private final RepositoryInterface<Long, User> userRepository;
    private final EntityValidatorInterface<UnorderedPair<Long, Long>, Friendship> friendshipValidator;


    public NetworkService(RepositoryInterface<UnorderedPair<Long, Long>, Friendship> friendshipRepository,
                          RepositoryInterface<Long, User> userRepository,
                          EntityValidatorInterface<UnorderedPair<Long, Long>, Friendship> friendshipValidator) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.friendshipValidator = friendshipValidator;
    }

    /**
     * adds a new friendship
     * @param firstUserId - Long - id of first user
     * @param secondUserId - Long - id of second user
     * @return friendship Optional if the relationship was added, empty Optional if the relationship exists already
     * @throws socialNetwork.exceptions.InvalidEntityException one of users is not found
     */
    public Optional<Friendship> addFriendshipService(Long firstUserId, Long secondUserId, LocalDateTime date){
        UnorderedPair<Long, Long> idNewFriendship = new UnorderedPair<>(firstUserId, secondUserId);
        Optional<Friendship> existingFriendshipOptional = friendshipRepository.find(idNewFriendship);

        if(existingFriendshipOptional.isEmpty()){
            Friendship friendship = new Friendship(firstUserId, secondUserId, date);
            //friendship.setInvitationStage(InvitationStage.APPROVED);
            friendshipValidator.validate(friendship);
            friendshipRepository.save(friendship);
        }
        return existingFriendshipOptional;
    }

    public Optional<Friendship> updateApprovedFriendshipService(Long firstUserId,Long secondUserId){
        UnorderedPair<Long, Long> idNewFriendship = new UnorderedPair<>(firstUserId, secondUserId);
        Optional<Friendship> newFriendshipOptional = friendshipRepository.find(idNewFriendship);
        if(newFriendshipOptional.isEmpty())
            throw new EntityMissingValidationException("Friendship between users doesn't exist");

        Friendship newFriendship = newFriendshipOptional.get();
        if(newFriendship.getInvitationStage().equals(InvitationStage.REJECTED))
            throw new InvitationStatusException("Invitation has already been rejected");
        newFriendship.setInvitationStage(InvitationStage.APPROVED);
        Optional<Friendship> updateFriendship = friendshipRepository.update(newFriendship);
        return updateFriendship;
    }

    public Optional<Friendship> updateRejectedFriendshipService(Long firstUserId,Long secondUserId){
        UnorderedPair<Long, Long> idNewFriendship = new UnorderedPair<>(firstUserId, secondUserId);
        Optional<Friendship> newFriendshipOptional = friendshipRepository.find(idNewFriendship);
        if(newFriendshipOptional.isEmpty())
            throw new EntityMissingValidationException("Friendship between users doesn't exist");

        Friendship newFriendship = newFriendshipOptional.get();
        newFriendship.setInvitationStage(InvitationStage.REJECTED);
        Optional<Friendship> updateFriendship = friendshipRepository.update(newFriendship);
        return updateFriendship;
    }

    public Optional<Friendship> sendInvitationForFriendshipsService(Long firstUserId,Long secondUserId){
        UnorderedPair<Long, Long> idNewFriendship = new UnorderedPair<>(firstUserId, secondUserId);
        Optional<Friendship> newFriendshipOptional = friendshipRepository.find(idNewFriendship);
        if(newFriendshipOptional.isEmpty() ){
            addFriendshipService(firstUserId,secondUserId,LocalDateTime.now());
            Optional<Friendship> friendshipOptional = friendshipRepository.find(idNewFriendship);
            Friendship newFriendship = friendshipOptional.get();
            newFriendship.setInvitationStage(InvitationStage.PENDING);
            Optional<Friendship> updateFriendship = friendshipRepository.update(newFriendship);
            return updateFriendship;
        }

        Friendship findFriendship = newFriendshipOptional.get();;
        if( findFriendship.getInvitationStage().equals(InvitationStage.APPROVED) )
            throw new InvitationStatusException("The invitation is already accepted");
        if( findFriendship.getInvitationStage().equals(InvitationStage.PENDING) )
            throw new InvitationStatusException("The invitation is already pending");

        throw new InvitationStatusException("The invitation is rejected");
    }

    /**
     * removes the friendship between the users with the given id-s
     * @param firstUserId - Long - id of first user
     * @param secondUserId - Long - id of second user
     * @return Optional containing the removed relationship, empty Optional if the users are not friends
     */
    public Optional<Friendship> removeFriendshipService(Long firstUserId, Long secondUserId){
        UnorderedPair<Long, Long> friendshipId = new UnorderedPair<>(firstUserId, secondUserId);
        return friendshipRepository.remove(friendshipId);
    }

    /**
     * finds a friendship with the id-s of the 2 friends
     * @param firstUserId - Long
     * @param secondUserId - Long
     * @return Optional containing friendship if exists, empty optional otherwise
     */
    public Optional<Friendship> findFriendshipService(Long firstUserId, Long secondUserId){
        UnorderedPair<Long, Long> friendshipId = new UnorderedPair<>(firstUserId, secondUserId);
        return friendshipRepository.find(friendshipId);
    }

    /**
     * @return int - number of communities in the network
     */
    public int getNumberOfCommunitiesService(){
        UndirectedGraph<Long> graphOfUserNetwork = new UndirectedGraph<>();
        for(User user: userRepository.getAll())
            graphOfUserNetwork.addVertex(user.getId());
        for(Friendship friendship: friendshipRepository.getAll())
            if(friendship.getInvitationStage().equals(InvitationStage.APPROVED))
                graphOfUserNetwork.addEdge(friendship.getId().left, friendship.getId().right);
        return graphOfUserNetwork.findNumberOfConnectedComponents();
    }

    /**
     * @return a list with all the users from the most sociable community
     */
    public List<User> getMostSocialCommunity(){
        UndirectedGraph<User> graphOfUsers = new UndirectedGraph<>(userRepository.getAll());

        for(Friendship friendship: friendshipRepository.getAll())
            if(friendship.getInvitationStage().equals(InvitationStage.APPROVED)){
                User user1 = userRepository.find(friendship.getId().left).get();
                User user2 = userRepository.find(friendship.getId().right).get();
                graphOfUsers.addEdge(user1, user2);
            }

        return graphOfUsers.findConnectedComponentWithLongestPath().getVertices();
    }

    /**
     * @return - a list of all users and their friends
     */
    public List<User> getAllUsersAndTheirFriendsService(){
        List<Friendship> allFriendships = friendshipRepository.getAll();
        UndirectedGraph<User> userUndirectedGraph = new UndirectedGraph<>(userRepository.getAll());

        for(Friendship friendship: allFriendships)
            if(friendship.getInvitationStage().equals(InvitationStage.APPROVED)){
                User user1 = userRepository.find(friendship.getId().left).get();
                User user2 = userRepository.find(friendship.getId().right).get();
                userUndirectedGraph.addEdge(user1, user2);
            }
        List<User> allUsersAndFriends = new ArrayList<User>(userRepository.getAll());
        for(User currentUser: allUsersAndFriends){
            currentUser.setListOfFriends(userUndirectedGraph.getNeighboursOf(currentUser).stream().toList());
        }
        return allUsersAndFriends;
    }
}
