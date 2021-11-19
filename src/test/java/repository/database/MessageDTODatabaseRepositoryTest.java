package repository.database;

import config.ApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import socialNetwork.domain.models.Message;
import socialNetwork.domain.models.MessageDTO;
import socialNetwork.domain.models.ReplyMessage;
import socialNetwork.domain.models.User;
import socialNetwork.repository.RepositoryInterface;
import socialNetwork.repository.database.MessageDTODatabaseRepository;
import socialNetwork.repository.database.UserDatabaseRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MessageDTODatabaseRepositoryTest {
    String url = ApplicationContext.getProperty("network.database.url");
    String user = ApplicationContext.getProperty("network.database.user");
    String password = ApplicationContext.getProperty("network.database.password");
    MessageDTODatabaseRepository testRepository;

    public RepositoryInterface<Long, MessageDTO> getRepository() {
        if(testRepository == null)
            testRepository = new MessageDTODatabaseRepository(url, user, password);
        return testRepository;
    }

    public List<User> getUserTestData(){
        return Arrays.asList(
          new User(1L,"Gigi","Gigi"),
          new User(2L,"Maria","Maria"),
          new User(3L,"Bob","Bob")
        );
    }

    public List<Message> getMessageTestData(){
        return Arrays.asList(
          new Message(new User(1L,"Gigi","Gigi"),
                  Arrays.asList(new User(2L,"Maria","Maria"),
                          new User(3L,"Bob","Bob")),
                  "Buna"
                  ),
                new Message(new User(2L,"Maria","Maria"),
                        Arrays.asList(new User(1L,"Gigi","Gigi"),
                                new User(3L,"Bob","Bob")),
                        "Salut")
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
    void saveDtoMessagesTest(){
        List<Message> listOfMessages = getMessageTestData();
        Message message1 = listOfMessages.get(0);
        Message message2 = listOfMessages.get(1);
        MessageDTO messageDTONotReply = new MessageDTO(message1);
        getRepository().save(messageDTONotReply);
        MessageDTO messageDTOReply = new MessageDTO(message2);
        List<MessageDTO> listOfMessagesDTO = getRepository().getAll();
        MessageDTO messageWeWantToRespond = listOfMessagesDTO.get(0);
        messageDTOReply.setMessageToRespondTo(messageWeWantToRespond.getMainMessage());
        getRepository().save(messageDTOReply);

        List<MessageDTO> testGetAllMessageDTO = getRepository().getAll();
        MessageDTO messageDTOAtZeroIndex = testGetAllMessageDTO.get(0);
        Assertions.assertNull(messageDTOAtZeroIndex.getMessageToRespondTo());
        //System.out.println(messageDTOAtZeroIndex.getMainMessage());

        MessageDTO messageDtoAtOneIndex = testGetAllMessageDTO.get(1);
        Assertions.assertNotNull(messageDtoAtOneIndex.getMessageToRespondTo());

        //System.out.println(messageDtoAtOneIndex.getMainMessage());
        //System.out.println(messageDtoAtOneIndex.getMessageToRespondTo());
    }
}
