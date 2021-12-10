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
            friendshipValidator.validate(friendship);
            friendshipRepository.save(friendship);
        }
        return existingFriendshipOptional;
    }

    /**
     * sets invitation stage of a friendship to approved
     * exception thrown if there is no invitation from one user to another
     * exception thrown if invitation has already been refused
     */
    public Optional<Friendship> updateApprovedFriendshipService(Long firstUserId,Long secondUserId){
        Optional<Friendship> optionalFriendshipBetweenUsers = searchForFriendshipInRepository(
                firstUserId, secondUserId);
        if(optionalFriendshipBetweenUsers.isEmpty())
            throw new EntityMissingValidationException("Friendship between users doesn't exist");
        Friendship friendship = optionalFriendshipBetweenUsers.get();
        throwExceptionIfInvitationIsRejected(friendship);
        throwExceptionIfInvitationIsApproved(friendship);
        return setInvitationStatusToApproved(friendship);
    }

    /**
     * sets invitation stage of a friendship to rejected
     * exception thrown if there is no invitation from one user to another
     */
    public Optional<Friendship> updateRejectedFriendshipService(Long firstUserId,Long secondUserId){
        Optional<Friendship> optionalFriendshipBetweenUsers = searchForFriendshipInRepository(
                firstUserId, secondUserId);
        if(optionalFriendshipBetweenUsers.isEmpty())
            throw new EntityMissingValidationException("Friendship between users doesn't exist");
        Friendship friendshipBetweenUsers = optionalFriendshipBetweenUsers.get();
        return setInvitationStatusToRejected(friendshipBetweenUsers);
    }

    /**
     * one user sends an invitation to another
     * exception thrown if invitation already exists
     */
    public Optional<Friendship> sendInvitationForFriendshipsService(Long firstUserId, Long secondUserId){
        Optional<Friendship> optionalFriendshipBetweenUsers =
                searchForFriendshipInRepository(firstUserId, secondUserId);
        if(optionalFriendshipBetweenUsers.isEmpty())
            return addPendingInvitation(firstUserId, secondUserId);
        Friendship friendshipBetweenUsers = optionalFriendshipBetweenUsers.get();;
        throwExceptionValidateInvitationStatusWhenSendingInvitation(friendshipBetweenUsers);
        return optionalFriendshipBetweenUsers;
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

    /**
     * throws exception when sending an invitation if that invitation already exists in any
     * of the stages: approved, pending or rejected
     */
    private void throwExceptionValidateInvitationStatusWhenSendingInvitation(Friendship friendship) {
        if( friendship.getInvitationStage().equals(InvitationStage.APPROVED) )
            throw new InvitationStatusException("The invitation is already accepted");
        if( friendship.getInvitationStage().equals(InvitationStage.PENDING) )
            throw new InvitationStatusException("The invitation is already pending");
        throw new InvitationStatusException("The invitation is rejected");
    }

    private Optional<Friendship> searchForFriendshipInRepository(Long firstUserId,Long secondUserId){
        UnorderedPair<Long, Long> idNewFriendship = new UnorderedPair<>(firstUserId, secondUserId);
        return friendshipRepository.find(idNewFriendship);
    }

    /**
     * adds a default friendship in the repo - the invitation stage is set by default at
     * approved
     */
    private Friendship addDefaultFriendshipToRepo(Long firstUserId, Long secondUserId) {
        var idNewFriendship = new UnorderedPair<>(firstUserId, secondUserId);
        addFriendshipService(firstUserId, secondUserId,LocalDateTime.now());
        Optional<Friendship> friendshipOptional = friendshipRepository.find(idNewFriendship);
        return friendshipOptional.get();
    }

    private Optional<Friendship> setInvitationStatusToPending(Friendship newFriendship) {
        newFriendship.setInvitationStage(InvitationStage.PENDING);
        return friendshipRepository.update(newFriendship);
    }

    private Optional<Friendship> setInvitationStatusToApproved(Friendship newFriendship) {
        newFriendship.setInvitationStage(InvitationStage.APPROVED);
        return friendshipRepository.update(newFriendship);
    }

    private Optional<Friendship> setInvitationStatusToRejected(Friendship newFriendship) {
        newFriendship.setInvitationStage(InvitationStage.REJECTED);
        return friendshipRepository.update(newFriendship);
    }

    private Optional<Friendship> addPendingInvitation(Long firstUserId, Long secondUserId){
        Friendship defaultFriendship = addDefaultFriendshipToRepo(firstUserId, secondUserId);
        return setInvitationStatusToPending(defaultFriendship);
    }

    private void throwExceptionIfInvitationIsRejected(Friendship friendship) throws InvitationStatusException{
        if(friendship.getInvitationStage().equals(InvitationStage.REJECTED))
            throw new InvitationStatusException("Invitation has already been rejected");
    }

    private void throwExceptionIfInvitationIsApproved(Friendship friendship) throws InvitationStatusException{
        if(friendship.getInvitationStage().equals(InvitationStage.APPROVED))
            throw new InvitationStatusException("Invitation has already been approved");
    }
}
