package socialNetwork.domain.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class ReplyMessage extends Message{

    private User from;
    private List< User > to;
    private String text;
    private LocalDateTime date;
    private Message message = null;

    public ReplyMessage(User from, List<User> to, String text,Message message) {
        super(from, to, text);
        date = LocalDateTime.now(); //Datele raspunsului <-Raspuns
        this.message = message; //mesajul la care raspundem
    }

    @Override
    public User getFrom() {
        return from;
    }

    @Override
    public void setFrom(User from) {
        this.from = from;
    }

    @Override
    public List<User> getTo() {
        return to;
    }

    @Override
    public void setTo(List<User> to) {
        this.to = to;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ReplyMessage{" +
                "id=" + getId() +
                ", from=" + from +
                ", to=" + to +
                ", text='" + text + '\'' +
                ", date=" + date +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ReplyMessage that = (ReplyMessage) o;
        return Objects.equals(from, that.from) && Objects.equals(to, that.to) && Objects.equals(text, that.text) && Objects.equals(date, that.date) && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), from, to, text, date, message);
    }
}
