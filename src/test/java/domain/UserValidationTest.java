package domain;

import socialNetwork.domain.models.User;
import socialNetwork.domain.validators.UserValidator;
import socialNetwork.exceptions.InvalidEntityException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationTest {
    UserValidator userValidator = new UserValidator();
    String invalidFirstName = "First name cannot be empty!\n";
    String invalidLastName = "Last name cannot be empty!\n";
    String allFieldsInvalid = invalidFirstName.concat(invalidLastName);

    @Test
    void allFieldsAreValid(){
        User exampleUser = new User("Baltazar", "Baltazar");
        userValidator.validate(exampleUser);
    }

    @Test
    void invalidFirstNameTest(){
        User exampleUser = new User( "", "Baltazar");
        assertThrows(InvalidEntityException.class, ()-> userValidator.validate(exampleUser), invalidFirstName);
    }

    @Test
    void invalidLastNameTest(){
        User exampleUser = new User( "Baltazar", "");
        assertThrows(InvalidEntityException.class, ()-> userValidator.validate(exampleUser), invalidLastName);
    }

    @Test
    void allFieldsInvalidTest(){
        User exampleUser = new User( "","");
        assertThrows(InvalidEntityException.class, ()-> userValidator.validate(exampleUser), allFieldsInvalid);
    }
}