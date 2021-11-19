package socialNetwork.domain.models;

import java.util.Objects;

public class HistoryConversationDTO {

    private String firstName;
    private String lastName;
    private String text;

    public HistoryConversationDTO(String firstName, String lastName, String text) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.text = text;
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
        return "HistoryConversationDTO{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", text='" + text + '\'' +
                '}';
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
