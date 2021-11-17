package repository.memory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import repository.FriendshipRepositorySetterTest;
import socialNetwork.domain.models.Friendship;
import socialNetwork.repository.RepositoryInterface;
import socialNetwork.repository.memory.InMemoryRepository;
import socialNetwork.utilitaries.UnorderedPair;

public class InMemoryFriendshipRepositoryTest extends FriendshipRepositorySetterTest {
    InMemoryRepository<UnorderedPair<Long, Long>, Friendship> testRepository;
    @Override
    public RepositoryInterface<UnorderedPair<Long, Long>, Friendship> getRepository() {
        if(testRepository == null)
            testRepository = new InMemoryRepository<>();
        return testRepository;
    }

    @AfterEach
    void tearDown() {
        for (Friendship friendship : getRepository().getAll())
            getRepository().remove(friendship.getId());
    }

    @BeforeEach
    void setUp(){
        for(Friendship friendship : getTestData())
            getRepository().save(friendship);
    }
}
