package repository.database;

import config.ApplicationContext;
import socialNetwork.repository.database.AutentificationDatabaseRepository;

public class AuthentificationDatabaseRepositoryTest {
    String url = ApplicationContext.getProperty("network.database.url");
    String user = ApplicationContext.getProperty("network.database.user");
    String password = ApplicationContext.getProperty("network.database.password");
    AutentificationDatabaseRepository testRepository;

    private AutentificationDatabaseRepository getRepository(){
        if(testRepository == null)
            testRepository = new AutentificationDatabaseRepository(url,user,password);
        return testRepository;
    }
}
