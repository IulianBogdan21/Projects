package repository.csv;

import config.ApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import socialNetwork.exceptions.CorruptedDataException;
import socialNetwork.repository.csv.FriendshipCsvFileRepository;
import socialNetwork.repository.csv.UserCsvFileRepository;

public class CorruptedFilesTest {
    @Test
    void usersFileIsCorrupted(){
        Assertions.assertThrows(CorruptedDataException.class, () -> {
            var repository = new UserCsvFileRepository(ApplicationContext.getProperty("repository.csv.invalid_format"));
        });

        Assertions.assertThrows(CorruptedDataException.class, () -> {
            var repository = new UserCsvFileRepository(ApplicationContext.getProperty("repository.csv.users.corrupted_file1"));
        });

        Assertions.assertThrows(CorruptedDataException.class, () -> {
            var repository = new UserCsvFileRepository(ApplicationContext.getProperty("repository.csv.users.corrupted_file2"));
        });
    }

    @Test
    void friendshipsFileIsCorrupted(){
        Assertions.assertThrows(CorruptedDataException.class, () -> {
            var repository = new FriendshipCsvFileRepository(ApplicationContext.getProperty("repository.csv.invalid_format"));
        });

        Assertions.assertThrows(CorruptedDataException.class, () -> {
            var repository = new FriendshipCsvFileRepository(ApplicationContext.getProperty("repository.csv.friendships.corrupted_file1"));
        });

        Assertions.assertThrows(CorruptedDataException.class, () -> {
            var repository = new FriendshipCsvFileRepository(ApplicationContext.getProperty("repository.csv.friendships.corrupted_file2"));
        });
    }
}
