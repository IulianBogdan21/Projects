package service;

import config.ApplicationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import socialNetwork.domain.models.*;
import socialNetwork.repository.database.MessageDTODatabaseRepository;
import socialNetwork.repository.database.UserDatabaseRepository;
import socialNetwork.service.MessageService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MessageServiceTest {
    String url = ApplicationContext.getProperty("network.database.url");
    String user = ApplicationContext.getProperty("network.database.user");
    String password = ApplicationContext.getProperty("network.database.password");
    MessageDTODatabaseRepository testMessageRepository;
    UserDatabaseRepository testUserRepository;
    MessageService testService;

    public MessageService getService() {
        if(testService == null) {
            testMessageRepository = new MessageDTODatabaseRepository(url, user, password);
            testUserRepository = new UserDatabaseRepository(url, user, password);
            testService = new MessageService(testUserRepository, testMessageRepository);
        }
        return testService;
    }

    public List<User> getUserTestData(){
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
    void sendMessagesTest(){
        getService().sendMessagesService(1L,
                Arrays.asList(2L, 3L), "Buna");
        getService().sendMessagesService(1L,
                Arrays.asList(6L), "Noapte buna");
        getService().sendMessagesService(2L,
                Arrays.asList(1L,3L), "hELLO");
    }

    @Test
    void replyMessagesTest(){
        getService().sendMessagesService(1L,
                Arrays.asList(2L, 3L), "Buna");
        getService().sendMessagesService(1L,
                Arrays.asList(6L), "Noapte buna");
        getService().sendMessagesService(2L,
                Arrays.asList(1L,3L), "hELLO");
        List<MessagesToRespondDTO> messagesToRespondDTOList =
                getService().getAllMessagesToRespondForUserService(2L);
        Long id = messagesToRespondDTOList.get(0).getId();
        getService().respondMessageService(2L, id, "Buna si tie");
    }

    @Test
    void conversationMessagesTest(){
        getService().sendMessagesService(1L,
                Arrays.asList(2L, 3L), "Buna");
        getService().sendMessagesService(1L,
                Arrays.asList(6L), "Noapte buna");
        getService().sendMessagesService(2L,
                Arrays.asList(1L,3L), "hELLO");
        List<MessagesToRespondDTO> messagesToRespondDTOList =
                getService().getAllMessagesToRespondForUserService(2L);
        Long id = messagesToRespondDTOList.get(0).getId();
        getService().respondMessageService(2L, id, "Buna si tie");
        List<HistoryConversationDTO> historyConversationDTO =
                getService().historyConversationService(1L,2L);
        historyConversationDTO.forEach(System.out::println);
    }
}
