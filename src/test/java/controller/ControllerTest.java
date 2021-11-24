package controller;

import config.ApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import socialNetwork.controllers.NetworkController;
import socialNetwork.domain.models.Friendship;
import socialNetwork.domain.models.User;
import socialNetwork.domain.validators.EntityValidatorInterface;
import socialNetwork.domain.validators.FriendshipValidator;
import socialNetwork.domain.validators.UserValidator;
import socialNetwork.repository.database.FriendshipDatabaseRepository;
import socialNetwork.repository.database.MessageDTODatabaseRepository;
import socialNetwork.repository.database.UserDatabaseRepository;
import socialNetwork.service.MessageService;
import socialNetwork.service.NetworkService;
import socialNetwork.service.UserService;
import socialNetwork.utilitaries.UnorderedPair;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ControllerTest {

    String url = ApplicationContext.getProperty("network.database.url");
    String user = ApplicationContext.getProperty("network.database.user");
    String password = ApplicationContext.getProperty("network.database.password");
    EntityValidatorInterface<Long,User> testUserValidator = new UserValidator();
    EntityValidatorInterface<UnorderedPair<Long,Long>, Friendship> testFriendshipValidator;
    MessageDTODatabaseRepository testMessageRepository;
    UserDatabaseRepository testUserRepository;
    FriendshipDatabaseRepository testFriendshipRepository;
    UserService testUserService;
    NetworkService testNetworkService;
    MessageService testMessageService;
    NetworkController testNetworkController = null;

    public NetworkController getNetworkController() {
        if(testNetworkController == null) {
            testUserRepository = new UserDatabaseRepository(url,user,password);
            testFriendshipRepository = new FriendshipDatabaseRepository(url,user,password);
            testMessageRepository = new MessageDTODatabaseRepository(url,user,password);
            testFriendshipValidator = new FriendshipValidator(testUserRepository);
            testUserService = new UserService(testUserRepository,testFriendshipRepository,testUserValidator);
            testNetworkService = new NetworkService(testFriendshipRepository,testUserRepository,testFriendshipValidator);
            testMessageService = new MessageService(testUserRepository,testMessageRepository);
            testNetworkController = new NetworkController(testUserService,testNetworkService,testMessageService);
        }
        return testNetworkController;
    }

    private List<User> getUserTestData(){
        return Arrays.asList(
                new User(1L,"Gigi","Gigi"),
                new User(2L,"Maria","Maria"),
                new User(3L,"Bob","Bob"),
                new User(4L,"Johnny","Test"),
                new User(5L,"Paul","Paul"),
                new User(6L,"Andrei","Andrei")
        );
    }

    public void tearDown(){
        try(Connection connection = DriverManager.getConnection(url, user, password)) {
            var deleteUsersStatement =
                    connection.prepareStatement("DELETE FROM users");
            deleteUsersStatement.executeUpdate();
            var deleteChatMessagesStatement =
                    connection.prepareStatement("DELETE FROM chatmessages");
            deleteChatMessagesStatement.executeUpdate();
            var deleteMessagesStatement =
                    connection.prepareStatement("DELETE FROM messages");
            deleteMessagesStatement.executeUpdate();
            var deleteReplyMessagesStatement =
                    connection.prepareStatement("DELETE FROM replymessages");
            deleteReplyMessagesStatement.executeUpdate();
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

            for(User user : getUserTestData()){
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
    void removeUser(){
        getNetworkController().sendMessages(1L,Arrays.asList(3L,4L),"Buna");
        getNetworkController().sendMessages(4L,Arrays.asList(5L,6L),"Vulpe");
        getNetworkController().sendMessages(1L,Arrays.asList(6L),"Buna");

        List<Long> listOfAllId = testMessageRepository.getAll()
                .stream()
                .map( messageDTO -> {
                    return messageDTO.getMainMessage().getId();
                } )
                .toList();

        getNetworkController().respondMessage(3L,listOfAllId.get(0),"Noapte buna pa si pusi!");
        getNetworkController().respondMessage(5L,listOfAllId.get(1),"Gaina");

        List<Long> listOfID = testMessageService.allMessagesUserAppearsIn(1L)
                .stream()
                .map(message -> message.getId())
                .toList();


        getNetworkController().removeUser(1L);
        listOfID.forEach( idMessage -> Assertions.assertEquals(Optional.empty(),
                testMessageRepository.find(idMessage)));
    }
}
