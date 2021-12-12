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
import socialNetwork.exceptions.DatabaseException;
import socialNetwork.exceptions.EntityMissingValidationException;
import socialNetwork.exceptions.InvalidEntityException;
import socialNetwork.exceptions.InvitationStatusException;
import socialNetwork.repository.database.FriendshipDatabaseRepository;
import socialNetwork.repository.database.UserDatabaseRepository;
import socialNetwork.service.NetworkService;
import socialNetwork.utilitaries.UnorderedPair;

import java.sql.*;
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

    public Long getMaximumId(){
        try(Connection connection = DriverManager.getConnection(url, user, password)){
            String findMaximumString = "select max(id) from users";
            PreparedStatement findSql = connection.prepareStatement(findMaximumString);
            ResultSet resultSet = findSql.executeQuery();
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    public Long getMinimumId(){
        try(Connection connection = DriverManager.getConnection(url, user, password)){
            String findMinimumString = "select min(id) from users";
            PreparedStatement findSql = connection.prepareStatement(findMinimumString);
            ResultSet resultSet = findSql.executeQuery();
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    public List<User> getTestData() {
        return new ArrayList<>(Arrays.asList(
                new User("Baltazar","Baltazar","f1"),
                new User( "Bradley","Bradley","f2"),
                new User("Frank","Frank","f3"),
                new User("Johnny","John","f4"),
                new User( "Johnny","John","f5")
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
            String insertStatementString = "INSERT INTO users( first_name, last_name,username) VALUES (?,?,?)";
            PreparedStatement insertStatement = connection.prepareStatement(insertStatementString);

            for(User user : getTestData()){
                insertStatement.setString(1, user.getFirstName());
                insertStatement.setString(2, user.getLastName());
                insertStatement.setString(3, user.getUsername());
                insertStatement.executeUpdate();
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Test
    void sendInvitationTest(){
        getService().addFriendshipService(getMinimumId(),getMinimumId() + 1, LocalDateTime.now());
        getService().sendInvitationForFriendshipsService(getMinimumId() + 1, getMinimumId() + 2);
        Optional<Friendship> findFriendshipPendingStatusOptional =
                getService().findFriendshipService(getMinimumId() + 1, getMinimumId() + 2);
        Assertions.assertFalse(findFriendshipPendingStatusOptional.isEmpty());
        Friendship findFriendshipPendingStatus = findFriendshipPendingStatusOptional.get();
        Assertions.assertEquals(findFriendshipPendingStatus.getInvitationStage(),
                InvitationStage.PENDING
        );
        Assertions.assertThrows(InvitationStatusException.class,
                () -> getService().sendInvitationForFriendshipsService(getMinimumId() + 1, getMinimumId() + 2),
                "Invitation already pending when sending invitation");
        Assertions.assertThrows(EntityMissingValidationException.class,
                () -> getService().sendInvitationForFriendshipsService(getMinimumId(), getMaximumId() + 5),
                "First user not found when sending invitation");
        Assertions.assertThrows(EntityMissingValidationException.class,
                () -> getService().sendInvitationForFriendshipsService(getMaximumId() + 5, getMinimumId()),
                "Second user not found when sending invitation");
        Assertions.assertThrows(InvalidEntityException.class,
                () -> getService().sendInvitationForFriendshipsService(getMaximumId() + 5, getMaximumId() + 5),
                "Both users not found when sending invitation");
        getService().updateApprovedFriendshipService(getMinimumId() + 1, getMinimumId() + 2);
        Assertions.assertThrows(InvitationStatusException.class,
                ()-> getService().sendInvitationForFriendshipsService(getMinimumId() + 1, getMinimumId() + 2),
                "Sending invitation that exists and approved");
        getService().updateRejectedFriendshipService(getMinimumId() + 1, getMinimumId() + 2);
        Assertions.assertThrows(InvitationStatusException.class,
                ()-> getService().sendInvitationForFriendshipsService(getMinimumId() + 1, getMinimumId() + 2),
                "Sending invitation that exists and rejected");
    }

    @Test
    void rejectInvitationTest(){
        Assertions.assertThrows(EntityMissingValidationException.class,
                ()-> getService().updateRejectedFriendshipService(getMinimumId(), getMinimumId() + 1),
                "Reject ");
        getService().sendInvitationForFriendshipsService(getMaximumId() - 1, getMaximumId());
        var friendshipPendingOptional = getService().findFriendshipService(getMaximumId() - 1, getMaximumId());
        Assertions.assertFalse(friendshipPendingOptional.isEmpty());
        var friendshipPending = friendshipPendingOptional.get();
        Assertions.assertEquals(friendshipPending.getInvitationStage(),
                InvitationStage.PENDING);
        getService().updateRejectedFriendshipService(getMaximumId() - 1, getMaximumId());
        var friendshipRejectedOptional = getService().findFriendshipService(getMaximumId() - 1, getMaximumId());
        Assertions.assertFalse(friendshipRejectedOptional.isEmpty());
        var friendshipRejected = friendshipRejectedOptional.get();
        Assertions.assertEquals(friendshipRejected.getInvitationStage(),
                InvitationStage.REJECTED);
        getService().updateRejectedFriendshipService(getMaximumId() - 1, getMaximumId());
        var friendshipRejectedAgain = getService().findFriendshipService(getMaximumId() - 1, getMaximumId()).get();
        Assertions.assertEquals(friendshipRejectedAgain.getInvitationStage(),
                InvitationStage.REJECTED);
        getService().addFriendshipService(getMinimumId(), getMinimumId() + 2, LocalDateTime.now());
        var getNewOptionalFriendship = getService().findFriendshipService(getMinimumId(), getMinimumId() + 2);
        Assertions.assertFalse(getNewOptionalFriendship.isEmpty());
        var getNewFriendship = getNewOptionalFriendship.get();
        Assertions.assertEquals(getNewFriendship.getInvitationStage(),
                InvitationStage.APPROVED);
        getService().updateRejectedFriendshipService(getMinimumId(), getMinimumId() + 2);
        var checkIfFriendshipRejected = getService().findFriendshipService(getMinimumId(), getMinimumId() + 2).get();
        Assertions.assertEquals(checkIfFriendshipRejected.getInvitationStage(),
                InvitationStage.REJECTED);
    }

    @Test
    void approveInvitationTest(){
        getService().sendInvitationForFriendshipsService(getMinimumId() + 1, getMaximumId() - 1);
        var optionalSentInvitation = getService().findFriendshipService(getMinimumId() + 1, getMaximumId() - 1);
        Assertions.assertFalse(optionalSentInvitation.isEmpty());
        var sentInvitation = optionalSentInvitation.get();
        Assertions.assertEquals(sentInvitation.getInvitationStage(),
                InvitationStage.PENDING);
        getService().updateApprovedFriendshipService(getMinimumId() + 1, getMaximumId() - 1);
        var checkIfInvitationApproved = getService().findFriendshipService(getMinimumId() + 1, getMaximumId() - 1).get();
        Assertions.assertEquals(checkIfInvitationApproved.getInvitationStage(),
                InvitationStage.APPROVED);
        getService().updateRejectedFriendshipService(getMinimumId() + 1, getMaximumId() - 1);
        Assertions.assertThrows(InvitationStatusException.class,
                ()-> getService().updateApprovedFriendshipService(getMinimumId() + 1, getMaximumId() - 1),
                "Approving an invitation already rejected");
        getService().addFriendshipService(getMinimumId(), getMaximumId(), LocalDateTime.now());
        Assertions.assertThrows(InvitationStatusException.class,
                ()-> getService().updateApprovedFriendshipService(getMinimumId() , getMaximumId() ),
                "Approving an invitation already approved");
        Assertions.assertEquals(getService().findFriendshipService(getMinimumId(), getMaximumId()).get().getInvitationStage(),
                InvitationStage.APPROVED);
        Assertions.assertThrows(EntityMissingValidationException.class,
                ()-> getService().updateApprovedFriendshipService(getMinimumId(), getMinimumId() + 1),
                "Approving a not existing invitation");
    }

}
