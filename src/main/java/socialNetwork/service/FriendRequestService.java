package socialNetwork.service;

import socialNetwork.domain.models.FriendRequest;
import socialNetwork.domain.models.Friendship;
import socialNetwork.domain.models.InvitationStage;
import socialNetwork.domain.validators.EntityValidatorInterface;
import socialNetwork.exceptions.EntityMissingValidationException;
import socialNetwork.exceptions.InvitationStatusException;
import socialNetwork.repository.RepositoryInterface;
import socialNetwork.utilitaries.UnorderedPair;

import java.time.LocalDateTime;
import java.util.Optional;

public class FriendRequestService {

    RepositoryInterface<UnorderedPair<Long,Long>, FriendRequest> friendRequestRepository;
    EntityValidatorInterface<UnorderedPair<Long, Long>, FriendRequest> friendRequestValidator;
    NetworkService networkService;

    public FriendRequestService(RepositoryInterface<UnorderedPair<Long, Long>, FriendRequest> friendRequestRepository,
                                EntityValidatorInterface<UnorderedPair<Long, Long>, FriendRequest> friendRequestValidator,
                                NetworkService networkService) {
        this.friendRequestRepository = friendRequestRepository;
        this.friendRequestValidator = friendRequestValidator;
        this.networkService = networkService;
    }

    public Optional<FriendRequest> find(Long firstUserId, Long secondUserId){
        UnorderedPair<Long,Long> idEntity = new UnorderedPair<>(firstUserId,secondUserId);
        return friendRequestRepository.find(idEntity);
    }

    /**
     * one user(firstUserID) sends an invitation to another(secondUserID)
     * exception thrown if invitation already exists
     */
    public Optional<FriendRequest> sendInvitationForFriendRequestService(Long firstUserId, Long secondUserId){
        Optional<FriendRequest> optionalFriendRequestBetweenUsers =
                searchForFriendRequestInRepository(firstUserId, secondUserId);
        if(optionalFriendRequestBetweenUsers.isEmpty())
            return addPendingInvitation(firstUserId, secondUserId);
        FriendRequest friendRequestBetweenUsers = optionalFriendRequestBetweenUsers.get();;
        throwExceptionValidateInvitationStatusWhenSendingInvitation(friendRequestBetweenUsers);
        return optionalFriendRequestBetweenUsers;
    }

    /**
     * sets invitation stage of a friendship to approved
     * exception thrown if there is no invitation from one user to another
     * exception thrown if invitation has already been refused
     */
    public Optional<Friendship> updateApprovedFriendRequestService(Long firstUserId, Long secondUserId){
        Optional<FriendRequest> optionalFriendRequestBetweenUsers = searchForFriendRequestInRepository(
                firstUserId, secondUserId);
        if(optionalFriendRequestBetweenUsers.isEmpty())
            throw new EntityMissingValidationException("There is no pending invitation between users");
        FriendRequest friendRequest = optionalFriendRequestBetweenUsers.get();
        throwExceptionIfInvitationIsRejected(friendRequest);
        throwExceptionIfInvitationIsApproved(friendRequest);
        return setInvitationStatusToApproved(friendRequest);
    }

    /**
     * sets invitation stage of a friendship to rejected
     * exception thrown if there is no invitation from one user to another
     */
    public Optional<Friendship> updateRejectedFriendRequestService(Long firstUserId,Long secondUserId){
        Optional<FriendRequest> optionalFriendRequestBetweenUsers = searchForFriendRequestInRepository(
                firstUserId, secondUserId);
        if(optionalFriendRequestBetweenUsers.isEmpty())
            throw new EntityMissingValidationException("Friendship between users doesn't exist");
        FriendRequest friendRequestBetweenUsers = optionalFriendRequestBetweenUsers.get();
        return setInvitationStatusToRejected(friendRequestBetweenUsers);
    }

    public Optional<FriendRequest> updateRejectedToPendingFriendRequestService(Long idUserThatSends,Long idUserThatReceive){

        Optional<FriendRequest> optionalFriendRequestBetweenUsers = searchForFriendRequestInRepository(
                idUserThatSends, idUserThatReceive);
        if(optionalFriendRequestBetweenUsers.isEmpty())
            throw new EntityMissingValidationException("Friendship between users doesn't exist");
        FriendRequest friendRequestBetweenUsers = optionalFriendRequestBetweenUsers.get();
        friendRequestBetweenUsers.setInvitationStage(InvitationStage.PENDING);
        return friendRequestRepository.update(friendRequestBetweenUsers);
    }

    private Optional<Friendship> setInvitationStatusToRejected(FriendRequest friendRequest){
        friendRequest.setInvitationStage(InvitationStage.REJECTED);
        friendRequestRepository.update(friendRequest);
        return networkService.removeFriendshipService(friendRequest.getFromUserID(),friendRequest.getToUserID());
    }

    private Optional<Friendship> setInvitationStatusToApproved(FriendRequest friendRequest){
        friendRequest.setInvitationStage(InvitationStage.APPROVED);
        friendRequestRepository.update(friendRequest);
        return networkService.addFriendshipService(friendRequest.getFromUserID(),
                friendRequest.getToUserID(),friendRequest.getDateRequest());
    }

    private Optional<FriendRequest> searchForFriendRequestInRepository(Long firstUserId,Long secondUserId){
        UnorderedPair<Long, Long> idNewFriendship = new UnorderedPair<>(firstUserId, secondUserId);
        return friendRequestRepository.find(idNewFriendship);
    }

    private Optional<FriendRequest> addPendingInvitation(Long firstUserId, Long secondUserId){
        FriendRequest pendingFriendRequest = new FriendRequest(
                firstUserId,secondUserId, InvitationStage.PENDING, LocalDateTime.now());
        friendRequestValidator.validate(pendingFriendRequest);
        friendRequestRepository.save(pendingFriendRequest);
        return Optional.of(pendingFriendRequest);
    }

    private void throwExceptionValidateInvitationStatusWhenSendingInvitation(FriendRequest friendRequest) {
        if( friendRequest.getInvitationStage().equals(InvitationStage.APPROVED) )
            throw new InvitationStatusException("The invitation is already accepted");
        if( friendRequest.getInvitationStage().equals(InvitationStage.PENDING) )
            throw new InvitationStatusException("The invitation is already pending");
        throw new InvitationStatusException("The invitation is rejected");
    }

    private void throwExceptionIfInvitationIsRejected(FriendRequest friendRequest) throws InvitationStatusException{
        if(friendRequest.getInvitationStage().equals(InvitationStage.REJECTED))
            throw new InvitationStatusException("Invitation has already been rejected");
    }

    private void throwExceptionIfInvitationIsApproved(FriendRequest friendRequest) throws InvitationStatusException{
        if(friendRequest.getInvitationStage().equals(InvitationStage.APPROVED))
            throw new InvitationStatusException("Invitation has already been approved");
    }

}
