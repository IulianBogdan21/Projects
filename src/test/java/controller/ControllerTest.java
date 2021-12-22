package controller;

import config.ApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import socialNetwork.controllers.NetworkController;
import socialNetwork.domain.models.*;
import socialNetwork.domain.validators.*;
import socialNetwork.exceptions.LogInException;
import socialNetwork.repository.database.*;
import socialNetwork.exceptions.DatabaseException;
import socialNetwork.service.*;
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
    EntityValidatorInterface<UnorderedPair<Long,Long>, FriendRequest> testFriendRequestValidator;
    EntityValidatorInterface<String, Autentification> testAuthentificationValidator;
    AutentificationDatabaseRepository testAutentificationRepository = new AutentificationDatabaseRepository(url,user,password);
    MessageDTODatabaseRepository testMessageRepository;
    UserDatabaseRepository testUserRepository = new UserDatabaseRepository(url,user,password);;
    FriendshipDatabaseRepository testFriendshipRepository;
    FriendRequestDatabaseRepository testFriendRequestRepository;
    UserService testUserService;
    NetworkService testNetworkService;
    MessageService testMessageService;
    AuthentificationService testAuthentificationService;
    FriendRequestService testFriendRequestService;
    NetworkController testNetworkController = null;

    public NetworkController getNetworkController() {
        if(testNetworkController == null) {
            testFriendshipRepository = new FriendshipDatabaseRepository(url,user,password);
            testFriendRequestRepository = new FriendRequestDatabaseRepository(url,user,password);
            testMessageRepository = new MessageDTODatabaseRepository(url,user,password);
            testFriendshipValidator = new FriendshipValidator(testUserRepository);
            testFriendRequestValidator = new FriendRequestValidator(testUserRepository);
            testAuthentificationValidator = new AuthentificationValidator();
            testUserService = new UserService(testUserRepository,testFriendshipRepository,testFriendRequestRepository,testUserValidator);
            testNetworkService = new NetworkService(testFriendshipRepository,testFriendRequestRepository,
                    testUserRepository,testFriendshipValidator);
            testMessageService = new MessageService(testUserRepository,testMessageRepository);
            testAuthentificationService = new AuthentificationService(testAutentificationRepository,testAuthentificationValidator);
            testFriendRequestService = new FriendRequestService(testFriendRequestRepository,testFriendshipRepository,
                    testFriendRequestValidator);

            testNetworkController = new NetworkController(testUserService,testNetworkService
                    ,testMessageService,testAuthentificationService,testFriendRequestService);
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

    public Long getMinimumMessageId(){
        try(Connection connection = DriverManager.getConnection(url, user, password)){
            String findMinimumString = "select min(id) from messages";
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
            var deleteMessagesStatement =
                    connection.prepareStatement("DELETE FROM messages");
            deleteMessagesStatement.executeUpdate();
            var deleteFriendshipsStatement =
                    connection.prepareStatement("DELETE FROM friendships");
            deleteFriendshipsStatement.executeUpdate();
            var deleteChatMessagesStatement =
                    connection.prepareStatement("DELETE FROM messages_id_correlation");
            deleteChatMessagesStatement.executeUpdate();
            var deleteReplyMessagesStatement =
                    connection.prepareStatement("DELETE FROM replymessages");
            deleteReplyMessagesStatement.executeUpdate();
            var deleteFriendRequestStatement =
                    connection.prepareStatement("DELETE FROM friendrequests");
            deleteFriendRequestStatement.executeUpdate();
            var deleteAuthentificationStatement =
                    connection.prepareStatement("DELETE FROM autentifications");
            deleteAuthentificationStatement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @BeforeEach
    public void setUp(){
        tearDown();
        getUserTestData().forEach(x->testUserRepository.save(x));
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
                getNetworkController().logIn("naruzu","casa").getRoot());
        Assertions.assertThrows(LogInException.class,()->
                getNetworkController().logIn("naruzu","casA"));
        Assertions.assertThrows(LogInException.class,()->
                getNetworkController().logIn("Naruzu","casa"));
    }

    private boolean friendshipInTheList(List<User> friendshipList,Long id){
        boolean find = false;
        for(User user : friendshipList)
            if(user.getId().equals(id)){
                find = true;
                break;
            }
        return find;
    }

    @Test
    void testPageObjectWithoutChat(){
        Page page = getNetworkController().logIn("a1","pa1");

        //send 3 pending invitations
        getNetworkController().sendInvitationForFriendships(getMinimumId(),getMinimumId()+1);
        getNetworkController().sendInvitationForFriendships(getMinimumId(),getMinimumId()+2);
        getNetworkController().sendInvitationForFriendships(getMinimumId(),getMinimumId()+3);
        List<FriendRequest> friendRequestList = page.getFriendRequestList();
        Assertions.assertEquals(friendRequestList.size(),3);

        //accept last 2 invitations
        getNetworkController().updateApprovedFriendship(getMinimumId() + 2,getMinimumId());
        getNetworkController().updateApprovedFriendship(getMinimumId() + 3,getMinimumId());
        List<User> friendshipList = page.getFriendList();
        Assertions.assertEquals(friendshipList.size(),2);
        Assertions.assertTrue(friendshipInTheList(friendshipList,getMinimumId()+2));
        Assertions.assertTrue(friendshipInTheList(friendshipList,getMinimumId()+3));

        //reject last approve invitation by the receiver
        getNetworkController().updateRejectedFriendship(getMinimumId()+3,getMinimumId());
        friendshipList = page.getFriendList();
        Assertions.assertEquals(friendshipList.size(),1);
        Assertions.assertTrue(friendshipInTheList(friendshipList,getMinimumId()+2));

        //withdraw the pending invitation
        getNetworkController().withdrawFriendRequest(getMinimumId(),getMinimumId()+1);
        friendRequestList = page.getFriendRequestList();
        Assertions.assertEquals(friendRequestList.size(),2);

        //remove a friend .There it will be just the rejecte invitation
        getNetworkController().removeFriendship(getMinimumId(),getMinimumId()+2);
        friendRequestList = page.getFriendRequestList();
        Assertions.assertEquals(friendRequestList.size(),1);
        Assertions.assertEquals(friendRequestList.get(0).getInvitationStage(),InvitationStage.REJECTED);
        friendshipList = page.getFriendList();
        Assertions.assertEquals(friendshipList.size(),0);

        //resubmit the rejected invitation
        getNetworkController().updateRejectedToPendingFriendship(getMinimumId()+3,getMinimumId());
        getNetworkController().updateApprovedFriendship(getMinimumId(),getMinimumId() + 3);
        friendRequestList = page.getFriendRequestList();
        Assertions.assertEquals(friendRequestList.size(),1);
        Assertions.assertEquals(friendRequestList.get(0).getInvitationStage(),InvitationStage.APPROVED);
        friendshipList = page.getFriendList();
        Assertions.assertEquals(friendshipList.size(),1);

        page.unsubscribePage();
    }

    @Test
    void testPageObjectChat(){
        Page page = getNetworkController().logIn("a6","pa6");
        List<Chat> chatList = page.getChatList();
        Assertions.assertEquals(chatList.size(),0);

        //send 2 message
        getNetworkController().sendMessages(getMaximumId(),Arrays.asList(
                getMinimumId()+1,getMinimumId()+2),"Cel fara de nume");
        getNetworkController().sendMessages(getMaximumId(),Arrays.asList(
                getMinimumId()+2,getMinimumId()+1),"Se va ridica din nou");
        chatList = page.getChatList();
        Assertions.assertEquals(chatList.get(0).getMessageList().size(),2);

        getNetworkController().respondMessage(getMinimumId()+1,getMinimumMessageId(),"Cel fara de neam");
        chatList = page.getChatList();
        Assertions.assertEquals(chatList.size(),1);
        Assertions.assertEquals(chatList.get(0).getReplyMessageList().size(),1);

        page.unsubscribePage();
    }
}
