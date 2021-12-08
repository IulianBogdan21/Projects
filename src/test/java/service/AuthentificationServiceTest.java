package service;

import config.ApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import socialNetwork.domain.models.Autentification;
import socialNetwork.domain.models.User;
import socialNetwork.domain.validators.AuthentificationValidator;
import socialNetwork.repository.database.AutentificationDatabaseRepository;
import socialNetwork.repository.database.UserDatabaseRepository;
import socialNetwork.service.AuthentificationService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthentificationServiceTest {
    String url = ApplicationContext.getProperty("network.database.url");
    String user = ApplicationContext.getProperty("network.database.user");
    String password = ApplicationContext.getProperty("network.database.password");
    AutentificationDatabaseRepository autentificationDatabaseRepository;
    UserDatabaseRepository userDataBaseRepository = new UserDatabaseRepository(url,user,password);;
    AuthentificationValidator authentificationValidator;
    AuthentificationService authentificationService;

    private AuthentificationService getAuthentificationService(){
        if(authentificationService == null) {
            autentificationDatabaseRepository = new AutentificationDatabaseRepository(url, user, password);
            authentificationValidator = new AuthentificationValidator();
            authentificationService = new AuthentificationService(autentificationDatabaseRepository,authentificationValidator);
        }
        return authentificationService;
    }

    private UserDatabaseRepository getUserRepository(){
        return userDataBaseRepository;
    }


    private void tearDown(){
        try(Connection connection = DriverManager.getConnection(url,user,password);
            PreparedStatement deleteAllRecordsAuthentifications = connection
                    .prepareStatement("delete from autentifications");
            PreparedStatement deleteAllRecordsUsers = connection
                    .prepareStatement("delete from users")) {
            deleteAllRecordsAuthentifications.executeUpdate();
            deleteAllRecordsUsers.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @BeforeEach
    private void setUp(){
        tearDown();
        getUserRepository().save(new User("andrei78","balanici","andrei"));
        getUserRepository().save(new User("razvan34","bogdan","razvan"));
    }

    @Test
    void testSaveAuthentification() {
        Assertions.assertEquals(Optional.empty(),getAuthentificationService()
                .saveAuthentificationService("andrei","casa"));
        Assertions.assertEquals(Optional.empty(),getAuthentificationService()
                .saveAuthentificationService("razvan","upup"));

        Autentification authentification2 = new Autentification("andrei","copac");
        Assertions.assertEquals(Optional.of(authentification2),getAuthentificationService()
                .saveAuthentificationService("andrei","copac"));
    }

    @Test
    void testFindAuthentification() {
        Autentification authentification0 = new Autentification("andrei","casa");
        Autentification authentification1 = new Autentification("razvan","upup");
        getAuthentificationService().saveAuthentificationService("andrei","casa");
        getAuthentificationService().saveAuthentificationService("razvan","upup");

        Assertions.assertEquals(Optional.of(authentification0),
                getAuthentificationService().findAuthentificationService("andrei"));
        Assertions.assertEquals(Optional.of(authentification1),
                getAuthentificationService().findAuthentificationService("razvan"));
        Assertions.assertEquals(Optional.empty(),
                getAuthentificationService().findAuthentificationService("upupdown"));
    }

    @Test
    void testGetAllAuthentification() {
        Autentification authentification0 = new Autentification("andrei", "casa");
        Autentification authentification1 = new Autentification("razvan", "upup");
        getAuthentificationService().saveAuthentificationService("andrei","casa");
        getAuthentificationService().saveAuthentificationService("razvan","upup");

        List<Autentification> autentificationList = getAuthentificationService().getAllAuthentificationService();
        Assertions.assertEquals(Optional.of(authentification0).get(),autentificationList.get(0));
        Assertions.assertEquals(Optional.of(authentification1).get(),autentificationList.get(1));
        Assertions.assertEquals(2,autentificationList.size());
    }
}
