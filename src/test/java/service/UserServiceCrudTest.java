package service;

import config.ApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.database.FriendshipDatabaseTableSetter;
import repository.database.UserDatabaseTableSetter;
import socialNetwork.domain.models.Friendship;
import socialNetwork.domain.models.User;
import socialNetwork.domain.validators.UserValidator;
import socialNetwork.exceptions.InvalidEntityException;
import socialNetwork.repository.RepositoryInterface;
import socialNetwork.repository.csv.FriendshipCsvFileRepository;
import socialNetwork.repository.csv.UserCsvFileRepository;
import socialNetwork.repository.database.FriendshipDatabaseRepository;
import socialNetwork.repository.database.UserDatabaseRepository;
import socialNetwork.service.UserService;
import socialNetwork.utilitaries.UnorderedPair;


public class UserServiceCrudTest {
    private String url = ApplicationContext.getProperty("network.database.url");
    private String user = ApplicationContext.getProperty("network.database.user");
    private String password = ApplicationContext.getProperty("network.database.password");
    RepositoryInterface<Long, User> userTestRepository = new UserDatabaseRepository(url, user, password);;
    RepositoryInterface<UnorderedPair<Long, Long>, Friendship> friendshipTestRepository
            = new FriendshipDatabaseRepository(url, user, password);;
    UserCsvFileRepository localUsers =
            new UserCsvFileRepository(ApplicationContext.getProperty("service.users.crud"));
    FriendshipCsvFileRepository localFriendships =
            new FriendshipCsvFileRepository(ApplicationContext.getProperty("service.friendships.crud"));
    UserService testService = new UserService(userTestRepository, friendshipTestRepository, new UserValidator());

    @BeforeEach
    void setUp(){
        FriendshipDatabaseTableSetter.tearDown();
        UserDatabaseTableSetter.tearDown();
        UserDatabaseTableSetter.setUp(localUsers.getAll());
        FriendshipDatabaseTableSetter.setUp(localFriendships.getAll());
    }

    @Test
    void addWithInvalidUser(){
        Assertions.assertThrows(InvalidEntityException.class,
                ()->testService.addUserService(-1000L, "", "",""));
    }

    @Test
    void addWithValidUser(){
        Assertions.assertTrue(testService.addUserService(1000L, "John", "Snow","t1").isEmpty());
    }

    @Test
    void addWithExistingUser(){
        Assertions.assertTrue(testService.addUserService(1L, "John", "Snow","ba1").isPresent());
    }

    @Test
    void removeNonExitingUser(){
        Assertions.assertTrue(testService.removeUserService(1000L).isEmpty());
    }

    @Test
    void removeExistingUserWithNoFriends(){
        int oldNumberOfFriendships = friendshipTestRepository.getAll().size();
        Assertions.assertTrue(testService.removeUserService(10L).isPresent());
        Assertions.assertEquals(oldNumberOfFriendships, friendshipTestRepository.getAll().size());
    }

    @Test
    void removeExistingUserWithFriends(){
        int oldNumberOfFriends = friendshipTestRepository.getAll().size();
        Assertions.assertTrue(testService.removeUserService(1L).isPresent());
        Assertions.assertEquals(oldNumberOfFriends - 3, friendshipTestRepository.getAll().size());
    }
}
