package service;

import config.ApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.database.FriendshipDatabaseTableSetter;
import repository.database.UserDatabaseTableSetter;
import socialNetwork.domain.models.Friendship;
import socialNetwork.domain.models.User;
import socialNetwork.domain.validators.FriendshipValidator;
import socialNetwork.exceptions.InvalidEntityException;
import socialNetwork.repository.RepositoryInterface;
import socialNetwork.repository.csv.FriendshipCsvFileRepository;
import socialNetwork.repository.csv.UserCsvFileRepository;
import socialNetwork.repository.database.FriendshipDatabaseRepository;
import socialNetwork.repository.database.UserDatabaseRepository;
import socialNetwork.service.NetworkService;
import socialNetwork.utilitaries.UndirectedGraph;
import socialNetwork.utilitaries.UnorderedPair;


import java.time.LocalDateTime;
import java.util.*;

public class NetworkServiceCrudTest {
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

    NetworkService testService = new NetworkService(friendshipTestRepository,
            userTestRepository,
            new FriendshipValidator(userTestRepository));

    @BeforeEach
    void setUp(){
        FriendshipDatabaseTableSetter.tearDown();
        UserDatabaseTableSetter.tearDown();
    //    System.out.println(localFriendships.getAll());
        UserDatabaseTableSetter.setUp(localUsers.getAll());
        FriendshipDatabaseTableSetter.setUp(localFriendships.getAll());
    }

    @Test
    void addExistingFriendship(){
        Assertions.assertTrue(testService.addFriendshipService(2L, 1L, LocalDateTime.now()).isPresent());
    }

    @Test
    void addNewFriendship(){
        Assertions.assertTrue(testService.addFriendshipService(12L, 11L, LocalDateTime.now()).isEmpty());
    }

    @Test
    void addWithNonExistingUsers(){
        Assertions.assertThrows(InvalidEntityException.class,
                ()->testService.addFriendshipService(1000L, 2000L, LocalDateTime.now()));
    }

    @Test
    void removeWithNonExistingFriendship(){
        Assertions.assertTrue(testService.removeFriendshipService(12L, 11L).isEmpty());
    }

    @Test
    void removeWithExistingFriendship(){
        Assertions.assertTrue(testService.removeFriendshipService(1L, 2L).isPresent());
    }

    @Test
    void getUsersWithAllFriends(){
        UndirectedGraph<User> graph = new UndirectedGraph<>(localUsers.getAll());

        for(Friendship friendship : localFriendships.getAll()) {
            User user1 = localUsers.find(friendship.getId().left).get();
            User user2 = localUsers.find(friendship.getId().right).get();
            graph.addEdge(user1, user2);
        }

        for(User user : testService.getAllUsersAndTheirFriendsService()){
            Set<User> expectedFriends = graph.getNeighboursOf(user);
            Assertions.assertEquals(expectedFriends.size(), user.getListOfFriends().size());
            Assertions.assertTrue(expectedFriends.containsAll(user.getListOfFriends()));
        }
    }
}
