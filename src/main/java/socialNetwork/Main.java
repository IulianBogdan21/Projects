package socialNetwork;

import socialNetwork.config.ApplicationContext;
import socialNetwork.controllers.NetworkController;
import socialNetwork.domain.models.Autentification;
import socialNetwork.domain.models.Friendship;
import socialNetwork.domain.models.MessageDTO;
import socialNetwork.domain.models.User;
import socialNetwork.domain.validators.*;
import socialNetwork.exceptions.CorruptedDataException;
import socialNetwork.repository.RepositoryInterface;
import socialNetwork.repository.database.AutentificationDatabaseRepository;
import socialNetwork.repository.database.FriendshipDatabaseRepository;
import socialNetwork.repository.database.MessageDTODatabaseRepository;
import socialNetwork.repository.database.UserDatabaseRepository;
import socialNetwork.service.AuthentificationService;
import socialNetwork.service.MessageService;
import socialNetwork.service.NetworkService;
import socialNetwork.service.UserService;
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

        RepositoryInterface<Long, MessageDTO> messagesRepository =
                new MessageDTODatabaseRepository(url, user, password);

        RepositoryInterface<String, Autentification> autentificationRepository =
                new AutentificationDatabaseRepository(url,user,password);
        EntityValidatorInterface<String,Autentification> autentificationValidator =
                new AuthentificationValidator();

        UserService userService = new UserService(userRepository, friendshipRepository, userValidator);
        NetworkService networkService = new NetworkService(friendshipRepository, userRepository,
                friendshipValidator);
        MessageService messageService = new MessageService(userRepository, messagesRepository);
        AuthentificationService authentificationService = new AuthentificationService(
                autentificationRepository,autentificationValidator);
        NetworkController networkController =
                new NetworkController(userService, networkService, messageService,authentificationService);
        ConsoleInterface ui = new ConsoleInterface(networkController);
        ui.run();
    }
}
