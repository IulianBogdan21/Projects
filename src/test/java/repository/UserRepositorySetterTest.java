package repository;

import repository.RepositoryAbstractTest;
import socialNetwork.domain.models.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class UserRepositorySetterTest extends RepositoryAbstractTest<Long, User> {

    @Override
    public Long getExistingId() {
        return 1L;
    }

    @Override
    public Long createNotExistingId() {
        return 4L;
    }

    @Override
    public List<User> getTestData() {
        return new ArrayList<>(Arrays.asList(
                new User(1L,"Baltazar","Baltazar","d1"),
                new User(2L, "Bradley","Bradley","d2"),
                new User(3L,"Frank","Frank","d3"),
                new User(1234L,"Johnny","John","d4"),
                new User(5678L, "Johnny","John","d5")
                ));
    }

    @Override
    public User createValidEntity() {
        return new User(10L,"Brutus","Brutus","d6");
    }

}
