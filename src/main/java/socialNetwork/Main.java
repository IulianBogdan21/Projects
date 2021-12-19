package socialNetwork;

import socialNetwork.config.ApplicationContext;
import socialNetwork.controllers.NetworkController;
import socialNetwork.domain.models.*;
import socialNetwork.domain.validators.*;
import socialNetwork.exceptions.CorruptedDataException;
import socialNetwork.repository.RepositoryInterface;
import socialNetwork.repository.database.*;
import socialNetwork.service.*;
import socialNetwork.ui.ConsoleInterface;
import socialNetwork.utilitaries.UnorderedPair;

public class Main {

    public static void main(String[] args){
        String url = ApplicationContext.getProperty("socialnetwork.database.url");
        String user = ApplicationContext.getProperty("socialnetwork.database.user");
        String password = ApplicationContext.getProperty("socialnetwork.database.password");

        RepositoryInterface<Long, User> userRepository = new UserDatabaseRepository(url, user, password);
        EntityValidatorInterface<Long, User> userValidator = new UserValidator();

        RepositoryInterface<UnorderedPair<Long, Long>, Friendship> friendshipRepository =
                new FriendshipDatabaseRepository(url, user, password);
        EntityValidatorInterface<UnorderedPair<Long, Long>, Friendship> friendshipValidator =
                new FriendshipValidator(userRepository);

        RepositoryInterface<UnorderedPair<Long,Long>, FriendRequest> friendRequestRepository =
                new FriendRequestDatabaseRepository(url,user,password);
        EntityValidatorInterface<UnorderedPair<Long, Long>, FriendRequest> friendRequesttValidator =
                new FriendRequestValidator(userRepository);

        RepositoryInterface<Long, MessageDTO> messagesRepository =
                new MessageDTODatabaseRepository(url, user, password);

        RepositoryInterface<String, Autentification> autentificationRepository =
                new AutentificationDatabaseRepository(url,user,password);
        EntityValidatorInterface<String,Autentification> autentificationValidator =
                new AuthentificationValidator();

        UserService userService = new UserService(userRepository, friendshipRepository
                ,friendRequestRepository,userValidator);
        NetworkService networkService = new NetworkService(friendshipRepository, friendRequestRepository,
                userRepository,friendshipValidator);
        MessageService messageService = new MessageService(userRepository, messagesRepository);
        AuthentificationService authentificationService = new AuthentificationService(
                autentificationRepository,autentificationValidator);
        FriendRequestService friendRequestService = new FriendRequestService(friendRequestRepository,
                friendshipRepository,friendRequesttValidator);
        NetworkController networkController =
                new NetworkController(userService, networkService, messageService,
                        authentificationService,friendRequestService);
        ConsoleInterface ui = new ConsoleInterface(networkController);
        ui.run();
    }
}
