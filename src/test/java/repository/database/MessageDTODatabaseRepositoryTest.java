package repository.database;

import config.ApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import socialNetwork.domain.models.*;
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
                        "Salut"),
                new Message(new User(3L,"Bob","Bob"),
                        Arrays.asList(new User(1L,"Gigi","Gigi"),
                                new User(2L,"Maria","Maria")),
                        "Iondaime Hokage Sama"),
                new Message(new User(3L,"Bob","Bob"),
                        Arrays.asList(new User(2L,"Maria","Maria")),
                        "Fire Ball Tense")
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

    private MessageDTO constructDTOMessage(Message message){
        MessageDTO messageDTO = new MessageDTO(message);
        return messageDTO;
    }

    private MessageDTO constructDTOReplyMessage(Message message,Message replyMessage){
        MessageDTO messageDTO = new MessageDTO(message);
        messageDTO.setMessageToRespondTo(replyMessage);
        return messageDTO;
    }

    @Test
    void saveDtoMessagesTest(){
        List<Message> messageList = getMessageTestData();
        Message messageAtIndex0 = messageList.get(0); //1->2,3
        Message messageAtIndex1 = messageList.get(1); //2->1,3
        Message messageAtIndex2 = messageList.get(2); //3->1,2
        Message messageAtIndex3 = messageList.get(3); //3->2

        MessageDTO messageDTOForIndex0 = constructDTOMessage(messageAtIndex0);
        MessageDTO messageDTOForIndex1 = constructDTOMessage(messageAtIndex1);
        MessageDTO replyMessageDTOForIndex2 = constructDTOReplyMessage(messageAtIndex2,messageAtIndex0);
        MessageDTO messageDTOForIndex3 = constructDTOMessage(messageAtIndex3);

        Assertions.assertEquals(Optional.empty(),getRepository().save(messageDTOForIndex0));
        Assertions.assertEquals(Optional.empty(),getRepository().save(messageDTOForIndex1));
        Assertions.assertEquals(Optional.empty(),getRepository().save(replyMessageDTOForIndex2));
        Assertions.assertEquals(Optional.empty(),getRepository().save(messageDTOForIndex3));
    }

    @Test
    void findDTOMessagesTest(){
        List<Message> messageList = getMessageTestData();
        Message messageAtIndex0 = messageList.get(0); //1->2,3
        Message messageAtIndex1 = messageList.get(1); //2->1,3
        Message messageAtIndex2 = messageList.get(2); //3->1,2
        Message messageAtIndex3 = messageList.get(3); //3->2

        MessageDTO messageDTOForIndex0 = constructDTOMessage(messageAtIndex0);
        MessageDTO messageDTOForIndex1 = constructDTOMessage(messageAtIndex1);
        MessageDTO replyMessageDTOForIndex2 = constructDTOReplyMessage(messageAtIndex2,messageAtIndex0);
        MessageDTO messageDTOForIndex3 = constructDTOMessage(messageAtIndex3);
        getRepository().save(messageDTOForIndex0);
        getRepository().save(messageDTOForIndex1);
        getRepository().save(replyMessageDTOForIndex2);
        getRepository().save(messageDTOForIndex3);

        List<Long> allIDOFMessages = getRepository().getAll()
                .stream()
                .map( messageDTO -> {
                    return messageDTO.getMainMessage().getId();
                } )
                .toList();

        Long idOfFirstSavedDTOMessage = allIDOFMessages.get(0);
        Assertions.assertEquals(Optional.of (messageDTOForIndex0 ),
                getRepository().find(idOfFirstSavedDTOMessage));
        Long idOfSecondSavedDTOMessage = allIDOFMessages.get(1);
        Assertions.assertEquals(Optional.of( messageDTOForIndex1 ) ,
                getRepository().find(idOfSecondSavedDTOMessage));
        Long idOfThirdSavedDTOMessage = allIDOFMessages.get(2);
        Assertions.assertEquals(Optional.of( replyMessageDTOForIndex2 ) ,
                getRepository().find(idOfThirdSavedDTOMessage));
        Long idOfFourthSavedDToMessage = allIDOFMessages.get(3);
        Assertions.assertEquals( Optional.of( messageDTOForIndex3 ),
                getRepository().find(idOfFourthSavedDToMessage));

        //test the find method for a message that doesn't exist
        Assertions.assertEquals(Optional.empty() , getRepository().find(idOfFirstSavedDTOMessage-1));
    }

    @Test
    void removeMessageTest(){
        List<Message> messageList = getMessageTestData();
        Message messageAtIndex0 = messageList.get(0); //1->2,3
        Message messageAtIndex1 = messageList.get(1); //2->1,3
        Message messageAtIndex2 = messageList.get(2); //3->1,2
        Message messageAtIndex3 = messageList.get(3); //3->2

        MessageDTO messageDTOForIndex0 = constructDTOMessage(messageAtIndex0);
        MessageDTO messageDTOForIndex1 = constructDTOMessage(messageAtIndex1);
        MessageDTO replyMessageDTOForIndex2 = constructDTOReplyMessage(messageAtIndex2,messageAtIndex0);
        MessageDTO messageDTOForIndex3 = constructDTOMessage(messageAtIndex3);
        getRepository().save(messageDTOForIndex0);
        getRepository().save(messageDTOForIndex1);
        getRepository().save(replyMessageDTOForIndex2);
        getRepository().save(messageDTOForIndex3);

        List<Long> allIDOFMessages = getRepository().getAll()
                .stream()
                .map( messageDTO -> {
                    return messageDTO.getMainMessage().getId();
                } )
                .toList();

        var idMessage = allIDOFMessages.get(2);
        Assertions.assertEquals(getRepository().find(idMessage).get().getMainMessage(), messageAtIndex2);
        getRepository().remove(idMessage);
        Assertions.assertEquals(getRepository().find(idMessage), Optional.empty());
    }

}
