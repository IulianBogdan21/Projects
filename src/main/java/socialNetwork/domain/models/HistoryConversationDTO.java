package socialNetwork.domain.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class HistoryConversationDTO {

    private String firstName;
    private String lastName;
    private String text;
    private LocalDateTime date;

    public HistoryConversationDTO(String firstName, String lastName, String text, LocalDateTime date) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.text = text;
        this.date = date;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName + ": " + text + "  | " + date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoryConversationDTO that = (HistoryConversationDTO) o;
        return Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, text);
    }
}
