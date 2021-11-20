package socialNetwork.repository.database;

import socialNetwork.domain.models.Message;
import socialNetwork.domain.models.MessageDTO;
import socialNetwork.domain.models.ReplyMessage;
import socialNetwork.domain.models.User;
import socialNetwork.exceptions.DatabaseException;
import socialNetwork.repository.RepositoryInterface;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MessageDTODatabaseRepository implements RepositoryInterface<Long, MessageDTO> {

    private String url;
    private String user;
    private String password;

    public MessageDTODatabaseRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private User getUserById(Connection connection,Long id){
        try (PreparedStatement selectUserById = connection.prepareStatement(
                "select * from users where id = ?")) {
            selectUserById.setLong(1, id);
            ResultSet resultSetUsers = selectUserById.executeQuery();
            resultSetUsers.next();
            Long idUser = resultSetUsers.getLong("id");
            String firstName = resultSetUsers.getString("first_name");
            String lastname =resultSetUsers.getString("last_name");

            User user = new User(idUser,firstName,lastname);
            user.setIdEntity(idUser);

            return user;

        } catch (SQLException throwables) {
            throw new DatabaseException(throwables.getMessage());
        }
    }

    private Optional<Message> getNotReplyMessageById(Connection connection,Long idSearchedEntity) {
        try (PreparedStatement selectTextDataMessage =
                     connection.prepareStatement("select * from messages where id = ?")) {

            selectTextDataMessage.setLong(1, idSearchedEntity);
            ResultSet resultSetIdTextDateMessage = selectTextDataMessage.executeQuery();
            if (resultSetIdTextDateMessage.next() == false)
                return Optional.empty();
            Long idMessage = resultSetIdTextDateMessage.getLong("id");
            String text = resultSetIdTextDateMessage.getString("text");
            LocalDateTime date = resultSetIdTextDateMessage.getTimestamp("date").toLocalDateTime();

            PreparedStatement selectFromToUsers = connection.prepareStatement(
                    "select id_user_send,id_user_receive from chatmessages where id_message = ?");
            selectFromToUsers.setLong(1, idMessage);
            ResultSet resultSetSentReceiveUsers = selectFromToUsers.executeQuery();

            User from = null;
            List<User> to = new ArrayList<>();
            while (resultSetSentReceiveUsers.next()) {
                Long id_user_send = resultSetSentReceiveUsers.getLong("id_user_send");
                Long id_user_receive = resultSetSentReceiveUsers.getLong("id_user_receive");

                User userSend = getUserById(connection, id_user_send);
                User userTo = getUserById(connection, id_user_receive);
                to.add(userTo);
                from = userSend;
            }

            Message responseMessage = new Message(from, to, text);
            responseMessage.setDate(date);
            responseMessage.setIdEntity(idMessage);

            return Optional.of(responseMessage);

        } catch (SQLException throwables) {
            throw new DatabaseException(throwables.getMessage());
        }
    }

    @Override
    public Optional<MessageDTO> find(Long idSearchedEntity) {

        try( Connection connection = DriverManager.getConnection(url,user,password)) {

            Optional<Message> responseMessageOptional = getNotReplyMessageById(connection,idSearchedEntity);
            if(responseMessageOptional.isEmpty())
                return Optional.empty();
            Message responseMessage = responseMessageOptional.get();
            Long idMessage = responseMessage.getId();
            MessageDTO messageDTO = new MessageDTO(responseMessage);

            PreparedStatement isThereReplyMessage = connection.prepareStatement(
                    "select * from replymessages where id_reply = ?");
            isThereReplyMessage.setLong(1,idMessage);
            ResultSet resultSetFindReplyMessage = isThereReplyMessage.executeQuery();

            if( resultSetFindReplyMessage.next() ){ //id ul mesajului apara ca a raspuns la alt mesaj
                Long idTheMessageWeWantToRespondTo = resultSetFindReplyMessage.getLong("id_message");
                Optional<Message> theMessageWeWantToRespondTo =
                        getNotReplyMessageById(connection,idTheMessageWeWantToRespondTo);
                messageDTO.setMessageToRespondTo(theMessageWeWantToRespondTo.get());
            }

            return Optional.of(messageDTO);

        } catch (SQLException throwables) {
            throw new DatabaseException(throwables.getMessage());
        }
    }

    @Override
    public List<MessageDTO> getAll() {
        List <MessageDTO> messageDTOList = new ArrayList<>();
        try(Connection connection = DriverManager.getConnection(url,user,password);
            PreparedStatement selectAllIdFromMessages =
                connection.prepareStatement("select id from messages");
            ResultSet resultSetAllId = selectAllIdFromMessages.executeQuery()) {

            while(resultSetAllId.next()){
                Long idMessage = resultSetAllId.getLong("id");
                Optional<MessageDTO> messageDTO = find(idMessage);
                if(!messageDTO.isEmpty())
                    messageDTOList.add(messageDTO.get());
            }
            return messageDTOList;
        } catch (SQLException throwables) {
            throw new DatabaseException(throwables.getMessage());
        }
    }

    @Override
    public Optional<MessageDTO> save(MessageDTO entityToSave) {
        Message responseMessage = entityToSave.getMainMessage(); //<-RASPUNSUL

        try(Connection connection = DriverManager.getConnection(url,user,password);
            PreparedStatement insertMessage =
                    connection.prepareStatement("insert into messages(text,date) values (?,?)")) {
            insertMessage.setString(1,responseMessage.getText());
            insertMessage.setTimestamp(2, Timestamp.valueOf(responseMessage.getDate()));
            int rowCount = insertMessage.executeUpdate();

            if( rowCount == 0 )
                return Optional.of(entityToSave);


            String findIdLastRecord = "select id from messages where id = ( select MAX(id) from messages  ) ";
            PreparedStatement getIdFromLastRecord = connection.prepareStatement(findIdLastRecord);
            ResultSet resultSetMaxId = getIdFromLastRecord.executeQuery();
            resultSetMaxId.next();
            Long maxId = resultSetMaxId.getLong("id");
            responseMessage.setIdEntity(maxId);

            /*
            chatmessages isudserSent iduserReceive Id_mesjaulu
             */
            PreparedStatement insertConnectionToChat =
                    connection.prepareStatement(
                            "insert into chatmessages(id_user_send,id_user_receive,id_message) values (?,?,?)");
            responseMessage.getTo().forEach( userTo -> {
                try {
                    insertConnectionToChat.setLong(1,responseMessage.getFrom().getId());
                    insertConnectionToChat.setLong(2,userTo.getId());
                    insertConnectionToChat.setLong(3,maxId);
                    insertConnectionToChat.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } );

            Message theMessageWeWantToRespondTo = entityToSave.getMessageToRespondTo();

            //Message ,fie ReplyMessaje
            if( theMessageWeWantToRespondTo != null ){

                PreparedStatement insertToReplyMessages =
                        connection.prepareStatement("insert into replymessages(id_message,id_reply) values(?,?)");
                insertToReplyMessages.setLong(1,theMessageWeWantToRespondTo.getId());
                insertToReplyMessages.setLong(2,responseMessage.getId());
                insertToReplyMessages.executeUpdate();
            }
            return Optional.empty();

        } catch (SQLException throwables) {
            throw new DatabaseException(throwables.getMessage());
        }

    }

    @Override
    public Optional<MessageDTO> remove(Long idEntity) {
        return Optional.empty();
    }

    @Override
    public Optional<MessageDTO> update(MessageDTO entityToUpdate) {
        return Optional.empty();
    }
}
