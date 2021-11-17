package repository.csv;

import config.ApplicationContext;
import org.junit.jupiter.api.BeforeEach;
import repository.UserRepositorySetterTest;
import socialNetwork.domain.models.User;
import socialNetwork.repository.RepositoryInterface;
import socialNetwork.repository.csv.UserCsvFileRepository;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class UserCsvFileRepositoryTest extends UserRepositorySetterTest {
    UserCsvFileRepository testRepository;
    String TEST_FILE_PATH = ApplicationContext.getProperty("repository.csv.users.test");
    @Override
    public RepositoryInterface<Long, User> getRepository() {
        if(testRepository == null)
            testRepository = new UserCsvFileRepository(TEST_FILE_PATH);
        return testRepository;
    }

    void tearDown(){
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(TEST_FILE_PATH))) {
            writer.write("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    void setUp(){
        tearDown();

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(TEST_FILE_PATH))) {
            for(User user : getTestData()){
                String line = "" + user.getId() + "," + user.getFirstName() + "," + user.getLastName();
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
