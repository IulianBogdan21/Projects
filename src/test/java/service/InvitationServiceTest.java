package service;

import config.ApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import socialNetwork.domain.models.Friendship;
import socialNetwork.domain.models.InvitationStage;
import socialNetwork.domain.models.User;
import socialNetwork.domain.validators.EntityValidatorInterface;
import socialNetwork.domain.validators.FriendshipValidator;
import socialNetwork.exceptions.EntityMissingValidationException;
import socialNetwork.exceptions.InvalidEntityException;
import socialNetwork.exceptions.InvitationStatusException;
import socialNetwork.repository.database.FriendshipDatabaseRepository;
import socialNetwork.repository.database.UserDatabaseRepository;
import socialNetwork.service.NetworkService;
import socialNetwork.utilitaries.UnorderedPair;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InvitationServiceTest {
    private final String url = ApplicationContext.getProperty("network.database.url");
    private final String user = ApplicationContext.getProperty("network.database.user");
    private final String password = ApplicationContext.getProperty("network.database.password");

    UserDatabaseRepository testUserRepository;
    FriendshipDatabaseRepository testFriendshipsRepository;
    EntityValidatorInterface<UnorderedPair<Long, Long>, Friendship> friendshipValidator;
    NetworkService testService;

    private NetworkService getService() {
        if(testService == null) {
            testUserRepository = new UserDatabaseRepository(url, user, password);
            testFriendshipsRepository = new FriendshipDatabaseRepository(url, user, password);
            friendshipValidator = new FriendshipValidator(testUserRepository);
            testService = new NetworkService(testFriendshipsRepository,
                    testUserRepository, friendshipValidator);
        }
        return testService;
    }

    public List<User> getTestData() {
        return new ArrayList<>(Arrays.asList(
                new User(1L,"Baltazar","Baltazar","f1"),
                new User(2L, "Bradley","Bradley","f2"),
                new User(3L,"Frank","Frank","f3"),
                new User(4L,"Johnny","John","f4"),
                new User(5L, "Johnny","John","f5")
        ));
    }

    public void tearDown(){
        try(Connection connection = DriverManager.getConnection(url, user, password)) {
            var deleteUsersStatement =
                    connection.prepareStatement("DELETE FROM users");
            deleteUsersStatement.executeUpdate();
            var deleteFriendshipsStatement =
                    connection.prepareStatement("DELETE FROM friendships");
            deleteFriendshipsStatement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @BeforeEach
    public void setUp(){
        tearDown();

        try(Connection connection = DriverManager.getConnection(url, user, password)) {
            String insertStatementString = "INSERT INTO users(id, first_name, last_name,username) VALUES (?,?,?,?)";
            PreparedStatement insertStatement = connection.prepareStatement(insertStatementString);

            for(User user : getTestData()){
                insertStatement.setLong(1, user.getId());
                insertStatement.setString(2, user.getFirstName());
                insertStatement.setString(3, user.getLastName());
                insertStatement.setString(4, user.getUsername());
                insertStatement.executeUpdate();
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Test
    void sendInvitationTest(){
        getService().addFriendshipService(1L,2L, LocalDateTime.now());
        getService().sendInvitationForFriendshipsService(2L, 3L);
        Optional<Friendship> findFriendshipPendingStatusOptional =
                getService().findFriendshipService(2L, 3L);
        Assertions.assertFalse(findFriendshipPendingStatusOptional.isEmpty());
        Friendship findFriendshipPendingStatus = findFriendshipPendingStatusOptional.get();
        Assertions.assertEquals(findFriendshipPendingStatus.getInvitationStage(),
                InvitationStage.PENDING
        );
        Assertions.assertThrows(InvitationStatusException.class,
                () -> getService().sendInvitationForFriendshipsService(2L, 3L),
                "Invitation already pending when sending invitation");
        Assertions.assertThrows(EntityMissingValidationException.class,
                () -> getService().sendInvitationForFriendshipsService(1L, 10L),
                "First user not found when sending invitation");
        Assertions.assertThrows(EntityMissingValidationException.class,
                () -> getService().sendInvitationForFriendshipsService(10L, 1L),
                "Second user not found when sending invitation");
        Assertions.assertThrows(InvalidEntityException.class,
                () -> getService().sendInvitationForFriendshipsService(10L, 10L),
                "Both users not found when sending invitation");
        getService().updateApprovedFriendshipService(2L, 3L);
        Assertions.assertThrows(InvitationStatusException.class,
                ()-> getService().sendInvitationForFriendshipsService(2L, 3L),
                "Sending invitation that exists and approved");
        getService().updateRejectedFriendshipService(2L, 3L);
        Assertions.assertThrows(InvitationStatusException.class,
                ()-> getService().sendInvitationForFriendshipsService(2L, 3L),
                "Sending invitation that exists and rejected");
    }

    @Test
    void rejectInvitationTest(){
        Assertions.assertThrows(EntityMissingValidationException.class,
                ()-> getService().updateRejectedFriendshipService(1L, 2L),
                "Reject ");
        getService().sendInvitationForFriendshipsService(4L, 5L);
        var friendshipPendingOptional = getService().findFriendshipService(4L, 5L);
        Assertions.assertFalse(friendshipPendingOptional.isEmpty());
        var friendshipPending = friendshipPendingOptional.get();
        Assertions.assertEquals(friendshipPending.getInvitationStage(),
                InvitationStage.PENDING);
        getService().updateRejectedFriendshipService(4L, 5L);
        var friendshipRejectedOptional = getService().findFriendshipService(4L, 5L);
        Assertions.assertFalse(friendshipRejectedOptional.isEmpty());
        var friendshipRejected = friendshipRejectedOptional.get();
        Assertions.assertEquals(friendshipRejected.getInvitationStage(),
                InvitationStage.REJECTED);
        getService().updateRejectedFriendshipService(4L, 5L);
        var friendshipRejectedAgain = getService().findFriendshipService(4L, 5L).get();
        Assertions.assertEquals(friendshipRejectedAgain.getInvitationStage(),
                InvitationStage.REJECTED);
        getService().addFriendshipService(1L, 3L, LocalDateTime.now());
        var getNewOptionalFriendship = getService().findFriendshipService(1L, 3L);
        Assertions.assertFalse(getNewOptionalFriendship.isEmpty());
        var getNewFriendship = getNewOptionalFriendship.get();
        Assertions.assertEquals(getNewFriendship.getInvitationStage(),
                InvitationStage.APPROVED);
        getService().updateRejectedFriendshipService(1L, 3L);
        var checkIfFriendshipRejected = getService().findFriendshipService(1L, 3L).get();
        Assertions.assertEquals(checkIfFriendshipRejected.getInvitationStage(),
                InvitationStage.REJECTED);
    }

    @Test
    void approveInvitationTest(){
        getService().sendInvitationForFriendshipsService(2L, 4L);
        var optionalSentInvitation = getService().findFriendshipService(2L, 4L);
        Assertions.assertFalse(optionalSentInvitation.isEmpty());
        var sentInvitation = optionalSentInvitation.get();
        Assertions.assertEquals(sentInvitation.getInvitationStage(),
                InvitationStage.PENDING);
        getService().updateApprovedFriendshipService(2L, 4L);
        var checkIfInvitationApproved = getService().findFriendshipService(2L, 4L).get();
        Assertions.assertEquals(checkIfInvitationApproved.getInvitationStage(),
                InvitationStage.APPROVED);
        getService().updateRejectedFriendshipService(2L, 4L);
        Assertions.assertThrows(InvitationStatusException.class,
                ()-> getService().updateApprovedFriendshipService(2L, 4L),
                "Approving an invitation already rejected");
        getService().addFriendshipService(1L, 5L, LocalDateTime.now());
        getService().updateApprovedFriendshipService(1L, 5L);
        Assertions.assertEquals(getService().findFriendshipService(1L, 5L).get().getInvitationStage(),
                InvitationStage.APPROVED);
        Assertions.assertThrows(EntityMissingValidationException.class,
                ()-> getService().updateApprovedFriendshipService(1L, 2L),
                "Approving a not existing invitation");
    }

}
