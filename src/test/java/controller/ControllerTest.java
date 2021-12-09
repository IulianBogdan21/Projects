package controller;

import config.ApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import repository.database.AuthentificationDatabaseRepositoryTest;
import socialNetwork.controllers.NetworkController;
import socialNetwork.domain.models.Autentification;
import socialNetwork.domain.models.Friendship;
import socialNetwork.domain.models.User;
import socialNetwork.domain.validators.AuthentificationValidator;
import socialNetwork.domain.validators.EntityValidatorInterface;
import socialNetwork.domain.validators.FriendshipValidator;
import socialNetwork.domain.validators.UserValidator;
import socialNetwork.exceptions.LogInException;
import socialNetwork.repository.database.AutentificationDatabaseRepository;
import socialNetwork.exceptions.DatabaseException;
import socialNetwork.repository.database.FriendshipDatabaseRepository;
import socialNetwork.repository.database.MessageDTODatabaseRepository;
import socialNetwork.repository.database.UserDatabaseRepository;
import socialNetwork.service.AuthentificationService;
import socialNetwork.service.MessageService;
import socialNetwork.service.NetworkService;
import socialNetwork.service.UserService;
import socialNetwork.utilitaries.UnorderedPair;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ControllerTest {

    String url = ApplicationContext.getProperty("network.database.url");
    String user = ApplicationContext.getProperty("network.database.user");
    String password = ApplicationContext.getProperty("network.database.password");
    EntityValidatorInterface<Long,User> testUserValidator = new UserValidator();
    EntityValidatorInterface<UnorderedPair<Long,Long>, Friendship> testFriendshipValidator;
    EntityValidatorInterface<String, Autentification> testAuthentificationValidator;
    AutentificationDatabaseRepository testAutentificationRepository = new AutentificationDatabaseRepository(url,user,password);
    MessageDTODatabaseRepository testMessageRepository;
    UserDatabaseRepository testUserRepository;
    FriendshipDatabaseRepository testFriendshipRepository;
    UserService testUserService;
    NetworkService testNetworkService;
    MessageService testMessageService;
    AuthentificationService testAuthentificationService;
    NetworkController testNetworkController = null;

    public NetworkController getNetworkController() {
        if(testNetworkController == null) {
            testUserRepository = new UserDatabaseRepository(url,user,password);
            testFriendshipRepository = new FriendshipDatabaseRepository(url,user,password);
            testMessageRepository = new MessageDTODatabaseRepository(url,user,password);
            testFriendshipValidator = new FriendshipValidator(testUserRepository);
            testAuthentificationValidator = new AuthentificationValidator();
            testUserService = new UserService(testUserRepository,testFriendshipRepository,testUserValidator);
            testNetworkService = new NetworkService(testFriendshipRepository,testUserRepository,testFriendshipValidator);
            testMessageService = new MessageService(testUserRepository,testMessageRepository);
            testAuthentificationService = new AuthentificationService(testAutentificationRepository,testAuthentificationValidator);
            testNetworkController = new NetworkController(testUserService,testNetworkService
                    ,testMessageService,testAuthentificationService);
        }
        return testNetworkController;
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

    private List<User> getUserTestData(){
        return Arrays.asList(
                new User("Gigi","Gigi","a1"),
                new User("Maria","Maria","a2"),
                new User("Bob","Bob","a3"),
                new User("Johnny","Test","a4"),
                new User("Paul","Paul","a5"),
                new User("Andrei","Andrei","a6")
        );
    }

    private List<Autentification> getAutentificationTestData(){
        return Arrays.asList(
                new Autentification("a1","pa1"),
                new Autentification("a2","pa2"),
                new Autentification("a3","pa3"),
                new Autentification("a4","pa4"),
                new Autentification("a5","pa5"),
                new Autentification("a6","pa6")
        );
    }

    public void tearDown(){
        try(Connection connection = DriverManager.getConnection(url, user, password)) {
            var deleteUsersStatement =
                    connection.prepareStatement("DELETE FROM users");
            deleteUsersStatement.executeUpdate();
            var deleteChatMessagesStatement =
                    connection.prepareStatement("DELETE FROM messages_id_correlation");
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

            String insertStatementString = "INSERT INTO users( first_name, last_name, username) VALUES (?,?,?)";
            PreparedStatement insertStatement = connection.prepareStatement(insertStatementString);

            for(User user : getUserTestData()){
                insertStatement.setString(1, user.getFirstName());
                insertStatement.setString(2, user.getLastName());
                insertStatement.setString(3,user.getUsername());
                insertStatement.executeUpdate();
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        getAutentificationTestData().forEach( x -> testAutentificationRepository.save(x));
    }

    @Test
    void removeUser(){
        getNetworkController().sendMessages(getMinimumId(),Arrays.asList(getMinimumId() + 2,getMinimumId() + 3),"Buna");
        getNetworkController().sendMessages(getMaximumId() - 2,Arrays.asList(getMaximumId() - 1,getMaximumId()),"Vulpe");
        getNetworkController().sendMessages(getMinimumId(),Arrays.asList(getMaximumId()),"Buna");

        List<Long> listOfAllId = testMessageRepository.getAll()
                .stream()
                .map( messageDTO -> {
                    return messageDTO.getMainMessage().getId();
                } )
                .toList();

        getNetworkController().respondMessage(getMinimumId() + 2,listOfAllId.get(0),"Noapte buna pa si pusi!");
        getNetworkController().respondMessage(getMaximumId() - 1,listOfAllId.get(1),"Gaina");

        List<Long> listOfID = testMessageService.allMessagesUserAppearsIn(getMinimumId())
                .stream()
                .map(message -> message.getId())
                .toList();


        getNetworkController().removeUser(getMinimumId());
        listOfID.forEach( idMessage -> Assertions.assertEquals(Optional.empty(),
                testMessageRepository.find(idMessage)));
    }

    @Test
    void signUpTest(){
        Assertions.assertTrue(getNetworkController().
                signUp("Naruto","Uzumaky","naruzu","casa"));
        Assertions.assertFalse(getNetworkController()
                .signUp("Yahiko","Akatsuzy","naruzu","copac"));
    }

    @Test
    void logInTest(){
        Assertions.assertTrue(getNetworkController().
                signUp("Naruto","Uzumaky","naruzu","casa"));
        Assertions.assertEquals(new User("Naruto","Uzumaky","naruzu"),
                getNetworkController().logIn("naruzu","casa").get());
        Assertions.assertThrows(LogInException.class,()->
                getNetworkController().logIn("naruzu","casA"));
        Assertions.assertThrows(LogInException.class,()->
                getNetworkController().logIn("Naruzu","casa"));
    }
}
