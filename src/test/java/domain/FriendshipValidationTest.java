package domain;

import socialNetwork.domain.models.Friendship;
import socialNetwork.domain.models.User;
import socialNetwork.domain.validators.FriendshipValidator;
import socialNetwork.exceptions.EntityMissingValidationException;
import socialNetwork.repository.memory.InMemoryRepository;
import socialNetwork.repository.RepositoryInterface;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;


class FriendshipValidationTest {
    RepositoryInterface<Long, User> createUserRepository() {
        return new InMemoryRepository<>();
    }

    void setUpUserRepository(RepositoryInterface<Long, User> userRepository){
        List<User> userTestData = new ArrayList<>(Arrays.asList(
                new User(1L, "Michael", "Michael"),
                new User(2L, "John", "John"),
                new User(3L, "Marcel", "Marcel")));
        for(User user : userTestData)
            userRepository.save(user);
    }

    FriendshipValidator createStrategy(){
        var testRepo = createUserRepository();
        setUpUserRepository(testRepo);
        return new FriendshipValidator(testRepo);
    }

    @Test
    void usersDontExist(){
        var strategy = createStrategy();
        assertThrows(EntityMissingValidationException.class,
                () -> strategy.validate(new Friendship(1000L, 2000L)));
    }

    @Test
    void oneUserDoesntExist(){
        var strategy = createStrategy();
        assertThrows(EntityMissingValidationException.class,
                () -> strategy.validate(new Friendship(1L, 2000L)));
        assertThrows(EntityMissingValidationException.class,
                () -> strategy.validate(new Friendship(1000L, 2L)));
    }

    @Test
    void usersExist(){
        var strategy = createStrategy();
        strategy.validate(new Friendship(1L, 2L));
    }
}