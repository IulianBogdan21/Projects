package repository.memory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import repository.UserRepositorySetterTest;
import socialNetwork.domain.models.User;
import socialNetwork.repository.RepositoryInterface;
import socialNetwork.repository.memory.InMemoryRepository;

public class InMemoryUserRepositoryTest extends UserRepositorySetterTest {
    InMemoryRepository<Long, User> testRepository;

    @Override
    public RepositoryInterface<Long, User> getRepository(){
        if(testRepository == null)
            testRepository = new InMemoryRepository<>();
        return testRepository;
    }

    @AfterEach
    void tearDown(){
        for(User user : getRepository().getAll())
            getRepository().remove(user.getId());
    }

    @BeforeEach
    void setUp(){
        for(User user : getTestData())
            getRepository().save(user);
    }
}
