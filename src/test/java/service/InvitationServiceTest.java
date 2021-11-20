package service;

import config.ApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import socialNetwork.domain.models.Friendship;
import socialNetwork.domain.models.User;
import socialNetwork.domain.validators.EntityValidatorInterface;
import socialNetwork.domain.validators.FriendshipValidator;
import socialNetwork.exceptions.DatabaseException;
import socialNetwork.exceptions.ExceptionBaseClass;
import socialNetwork.exceptions.InvitationStatusException;
import socialNetwork.repository.database.FriendshipDatabaseRepository;
import socialNetwork.repository.database.MessageDTODatabaseRepository;
import socialNetwork.repository.database.UserDatabaseRepository;
import socialNetwork.service.MessageService;
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InvitationServiceTest {
    private String url = ApplicationContext.getProperty("network.database.url");
    private String user = ApplicationContext.getProperty("network.database.user");
    private String password = ApplicationContext.getProperty("network.database.password");

    UserDatabaseRepository testUserRepository;
    FriendshipDatabaseRepository testFriendshipsRepository;
    private EntityValidatorInterface<UnorderedPair<Long, Long>, Friendship> friendshipValidator;
    NetworkService testService;

    public NetworkService getService() {
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
                new User(1L,"Baltazar","Baltazar"),
                new User(2L, "Bradley","Bradley"),
                new User(3L,"Frank","Frank"),
                new User(4L,"Johnny","John"),
                new User(5L, "Johnny","John")
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
            String insertStatementString = "INSERT INTO users(id, first_name, last_name) VALUES (?,?,?)";
            PreparedStatement insertStatement = connection.prepareStatement(insertStatementString);

            for(User user : getTestData()){
                insertStatement.setLong(1, user.getId());
                insertStatement.setString(2, user.getFirstName());
                insertStatement.setString(3, user.getLastName());
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
        getService().updateApprovedFriendshipService(2L, 3L);
        Assertions.assertThrows(InvitationStatusException.class,
                () -> getService().sendInvitationForFriendshipsService(2L, 3L));
    }

    @Test
    void rejectInvitationTest(){
        getService().sendInvitationForFriendshipsService(4L, 5L);
        getService().updateRejectedFriendshipService(4L, 5L);
        var optionalFriendship = getService().
                addFriendshipService(4L, 5L, LocalDateTime.now());
        Assertions.assertEquals(4L, optionalFriendship.get().getId().left);
        Assertions.assertThrows(InvitationStatusException.class,
                ()-> getService().updateApprovedFriendshipService(4L, 5L)
            );
    }

}
