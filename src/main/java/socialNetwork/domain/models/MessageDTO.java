package socialNetwork.domain.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MessageDTO extends Entity<Long> {

    private Message mainMessage;
    private Message messageToRespondTo = null ;

    public MessageDTO(Message mainMessage) {
        this.mainMessage = mainMessage;
        setIdEntity(mainMessage.getId());
    }

    public Message getMainMessage() {
        return mainMessage;
    }

    public void setMainMessage(Message mainMessage) {
        this.mainMessage = mainMessage;
    }

    public Message getMessageToRespondTo() {
        return messageToRespondTo;
    }

    public void setMessageToRespondTo(Message messageToRespondTo) {
        this.messageToRespondTo = messageToRespondTo;
    }

    @Override
    public String toString() {
        return "MessageDTO{" +
                "mainMessage=" + mainMessage +
                ", messageToRespondTo=" + messageToRespondTo +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageDTO that = (MessageDTO) o;
        return Objects.equals(mainMessage, that.mainMessage) && Objects.equals(messageToRespondTo, that.messageToRespondTo) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mainMessage, messageToRespondTo);
    }
}
