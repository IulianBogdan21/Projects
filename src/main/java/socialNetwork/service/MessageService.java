package socialNetwork.service;

import socialNetwork.domain.models.*;
import socialNetwork.exceptions.EntityMissingValidationException;
import socialNetwork.repository.RepositoryInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MessageService {

    RepositoryInterface<Long, User> repoUser;
    RepositoryInterface<Long, MessageDTO> repoMessagesDTO;

    public MessageService(RepositoryInterface<Long, User> repoUser, RepositoryInterface<Long, MessageDTO> repoMessagesDTO) {
        this.repoUser = repoUser;
        this.repoMessagesDTO = repoMessagesDTO;
    }

    public Optional<Message> sendMessage(Long idUserFrom, List<Long> to,String text){
        User userFrom = getUserById(idUserFrom);
        List<User> usersTo = new ArrayList<>();
        to.forEach( idUserTo -> {
            User userTo = getUserById(idUserTo);
            usersTo.add(userTo);
        } );
        Message message = new Message(userFrom,usersTo,text);
        MessageDTO messageDTO = new MessageDTO(message);

        Optional<MessageDTO> saveMessageDTO = repoMessagesDTO.save(messageDTO);
        if(saveMessageDTO.isEmpty())
            return Optional.empty();
        return Optional.of(saveMessageDTO.get().getMainMessage());
    }

    public Optional<ReplyMessage> respondMessage(Long idUserFrom,Long idMessageAgregate,String text){
        User userFrom = getUserById(idUserFrom);

        Optional<MessageDTO> findMessageDTO = repoMessagesDTO.find(idMessageAgregate);
        if( findMessageDTO.isEmpty() )
            throw new EntityMissingValidationException("The message doesn't exist");
        MessageDTO messageDTO = findMessageDTO.get();
        Message messageWeWantToRespondTo = messageDTO.getMainMessage();

        List < User > to = new ArrayList<>();
        to.add(messageWeWantToRespondTo.getFrom());
        messageWeWantToRespondTo.getTo().forEach( userTo -> {
            if( !userTo.getId().equals(userFrom.getId()) )
                to.add(userTo);
        } );

        Message responseMessage = new Message(userFrom,to,text);
        MessageDTO messageDTOToSave = new MessageDTO(responseMessage);
        messageDTOToSave.setMessageToRespondTo(messageWeWantToRespondTo);
        Optional<MessageDTO> messageDTOAfterSave = repoMessagesDTO.save(messageDTOToSave);

        if( messageDTOAfterSave.isEmpty() )
            return Optional.empty();

        ReplyMessage replyMessage = new ReplyMessage(userFrom,to,text,messageWeWantToRespondTo);
        replyMessage.setDate(messageDTOAfterSave.get().getMainMessage().getDate());
        replyMessage.setIdEntity(messageDTOAfterSave.get().getMainMessage().getId());

        return Optional.of(replyMessage);
    }


    public List<HistoryConversationDTO> historyConversation(Long idFirstUser,Long idSecondUser){
        User firstUser = getUserById(idFirstUser);
        User secondUser = getUserById(idSecondUser);
        List < Message > messageList = new ArrayList<>();
        repoMessagesDTO.getAll().forEach(messageDTO -> {
            messageList.add(messageDTO.getMainMessage());
        });

        Predicate<Message> isThere = message -> {
            return message.getFrom().getId().equals(firstUser.getId()) && userInList(secondUser,message.getTo()) ||
                    message.getFrom().getId().equals(secondUser.getId()) && userInList(firstUser,message.getTo());
        };

        List<Message> filterMessage =  messageList.stream()
                .filter(isThere)
                .sorted((firstMessage,secondMessage)->{
                    if( firstMessage.getDate().isEqual(secondMessage.getDate()) )
                        return 0;
                    if( firstMessage.getDate().isBefore(secondMessage.getDate()) )
                        return -1;
                    return 1;
                })
                .toList();

        List< HistoryConversationDTO > listHistoryConversation = new ArrayList<>();
        for(Message message: filterMessage){
            if(message.getFrom().getId().equals(firstUser.getId()))
                listHistoryConversation.add(
                        new HistoryConversationDTO(firstUser.getFirstName(), firstUser.getLastName(), message.getText()
                        , message.getDate()));
            if(message.getFrom().getId().equals(secondUser.getId()))
                listHistoryConversation.add(
                        new HistoryConversationDTO(secondUser.getFirstName(), secondUser.getLastName(),
                                message.getText(), message.getDate()));
        }
        return listHistoryConversation;
    }

    public List<MessagesToRespondDTO> getAllMessagesToRespondForUser(Long idUser){
        User user = getUserById(idUser);
        return repoMessagesDTO.getAll()
                .stream()
                .filter(messageDTO -> messageDTO.getMessageToRespondTo() == null )
                .filter(messageDTO -> {
                    return userInList(user, messageDTO.getMainMessage().getTo());
                })
                .map( messageDTO -> {
                    return new MessagesToRespondDTO(
                            messageDTO.getMainMessage().getId(),messageDTO.getMainMessage().getText());
                } )
                .toList();
    }

    private User getUserById(Long id){
        Optional<User> findUserFrom = repoUser.find(id);
        if(  findUserFrom.isEmpty() )
            throw new EntityMissingValidationException("The user doesn't exist");
        User userFrom = findUserFrom.get();
        return userFrom;
    }

    private boolean userInList(User user,List<User> list){
        for(User userTo: list)
            if( user.getId().equals(userTo.getId()) )
                return true;
        return false;
    }


}
