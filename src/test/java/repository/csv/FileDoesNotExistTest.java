package repository.csv;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import socialNetwork.exceptions.IOFileException;
import socialNetwork.repository.csv.FriendshipCsvFileRepository;
import socialNetwork.repository.csv.UserCsvFileRepository;

public class FileDoesNotExistTest {
    @Test
    void usersFileDoesntExist(){
        Assertions.assertThrows(IOFileException.class, () -> {
            var repository = new UserCsvFileRepository("ads/asd/asd/asd.csv");
        });
    }

    @Test
    void friendshipsFileDoesntExist(){
        Assertions.assertThrows(IOFileException.class, () -> {
            var repository = new FriendshipCsvFileRepository("ads/asd/asd/asd.csv");
        });
    }
}
