package service;

import config.ApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import socialNetwork.domain.models.*;
import socialNetwork.exceptions.CorruptedDataException;
import socialNetwork.exceptions.EntityMissingValidationException;
import socialNetwork.repository.database.MessageDTODatabaseRepository;
import socialNetwork.repository.database.UserDatabaseRepository;
import socialNetwork.service.MessageService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
        getService().sendMessagesService(3L,
                Arrays.asList(2L), "Noapte buna");
        List<Long> listOfAllId = testMessageRepository.getAll()
                .stream()
                .map( messageDTO -> {
                    return messageDTO.getMainMessage().getId();
                } )
                .toList();

        Long idOfTheFirstMessageSend = listOfAllId.get(0);
        Assertions.assertEquals(Arrays.asList(testUserRepository.find(2L).get(),
                        testUserRepository.find(3L).get()),
                testMessageRepository.find(idOfTheFirstMessageSend).get().getMainMessage().getTo());
        Assertions.assertEquals(testUserRepository.find(1L).get(),
                testMessageRepository.find(idOfTheFirstMessageSend).get().getMainMessage().getFrom());
        Assertions.assertEquals("Buna",
                testMessageRepository.find(idOfTheFirstMessageSend).get().getMainMessage().getText());

        Long idOfTheSecondMessageSend = listOfAllId.get(1);
        Assertions.assertEquals(Arrays.asList( testUserRepository.find(2L).get() ),
                testMessageRepository.find(idOfTheSecondMessageSend).get().getMainMessage().getTo());
        Assertions.assertEquals(testUserRepository.find(3L).get(),
                testMessageRepository.find(idOfTheSecondMessageSend).get().getMainMessage().getFrom());
        Assertions.assertEquals("Noapte buna",
                testMessageRepository.find(idOfTheSecondMessageSend).get().getMainMessage().getText());

//throw exception if the user that send the message doesn't exist
        Assertions.assertThrows(EntityMissingValidationException.class,
                () -> getService().sendMessagesService(-2L,Arrays.asList(2L,1L),"gaga"));

//throw exception if one of the receivers doesn't exist
        Assertions.assertThrows(EntityMissingValidationException.class,
                () -> getService().sendMessagesService(1L,Arrays.asList(2L,3L,-1L),"mama"));
    }

    @Test
    void replyMessagesTest(){
        getService().sendMessagesService(1L,
                Arrays.asList(2L, 3L), "Buna");
        getService().sendMessagesService(3L,
                Arrays.asList(2L), "Noapte buna");
        List<Long> listOfAllIdOfAllSentMessages = testMessageRepository.getAll()
                .stream()
                .map( messageDTO -> {
                    return messageDTO.getMainMessage().getId();
                } )
                .toList();

        Long idOfTheReplyMessage = listOfAllIdOfAllSentMessages.get(0);
        getService().respondMessageService(3L,idOfTheReplyMessage,"Wake up to reality");
        Optional< MessageDTO > responseMessage = testMessageRepository.getAll()
                .stream()
                .max( (MessageDTO x,MessageDTO y) ->{
                    return x.getMainMessage().getId().compareTo(y.getMainMessage().getId());
                } );
        Assertions.assertEquals(testUserRepository.find(3L).get(),
                responseMessage.get().getMainMessage().getFrom());
        Assertions.assertEquals(Arrays.asList( testUserRepository.find(1L).get() ,
                testUserRepository.find(2L).get() ),
                responseMessage.get().getMainMessage().getTo());
        Assertions.assertEquals("Wake up to reality",
                responseMessage.get().getMainMessage().getText());
        Assertions.assertEquals("Buna",
                responseMessage.get().getMessageToRespondTo().getText());

        Long idOfSecondSentMessage = listOfAllIdOfAllSentMessages.get(1);
        getService().respondMessageService(2L, idOfSecondSentMessage,"Nothing ever goes as planned");
        Optional< MessageDTO > responseMessageToSecond = testMessageRepository.getAll()
                .stream()
                .max( (MessageDTO x,MessageDTO y) ->{
                    return x.getMainMessage().getId().compareTo(y.getMainMessage().getId());
                } );
        Assertions.assertEquals("Nothing ever goes as planned",
                responseMessageToSecond.get().getMainMessage().getText());
        Assertions.assertEquals("Noapte buna",
                responseMessageToSecond.get().getMessageToRespondTo().getText());

        //if user that reply doesn't exist
        Assertions.assertThrows(EntityMissingValidationException.class,
                () -> getService().respondMessageService(-1L,idOfTheReplyMessage,"kopac"));
        //if the message that I want to respond doesn't exist
        Assertions.assertThrows(CorruptedDataException.class,
                () -> getService().respondMessageService(1L,-1L,"casa"));
    }

    @Test
    void conversationMessagesTest(){
        getService().sendMessagesService(1L,
                Arrays.asList(2L, 3L), "Buna");
        getService().sendMessagesService(1L,
                Arrays.asList(6L), "Noapte buna");
        getService().sendMessagesService(2L,
                Arrays.asList(1L,3L), "HELLO");
        List<MessagesToRespondDTO> messagesToRespondDTOList =
                getService().getAllMessagesToRespondForUserService(2L);
        Assertions.assertEquals(messagesToRespondDTOList.get(0).getText(),"Buna");
        Assertions.assertEquals(messagesToRespondDTOList.size(),1);

        Long id = messagesToRespondDTOList.get(0).getId();
        getService().respondMessageService(2L, id, "Buna si tie");
        List<HistoryConversationDTO> historyConversationDTO =
                getService().historyConversationService(1L,2L);

        Assertions.assertEquals("Buna",historyConversationDTO.get(0).getText());
        Assertions.assertEquals("Gigi",historyConversationDTO.get(0).getFirstName());
        Assertions.assertEquals("HELLO",historyConversationDTO.get(1).getText());
        Assertions.assertEquals("Maria",historyConversationDTO.get(1).getFirstName());
        Assertions.assertEquals("Buna si tie",historyConversationDTO.get(2).getText());
        Assertions.assertEquals("Maria",historyConversationDTO.get(2).getFirstName());

        //if one of the users doesn't exist
        Assertions.assertThrows(EntityMissingValidationException.class,
                () -> getService().historyConversationService(-1L,2L));
    }
}
