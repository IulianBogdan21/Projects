package socialNetwork.repository.csv;

import socialNetwork.domain.models.Friendship;
import socialNetwork.exceptions.CorruptedDataException;
import socialNetwork.utilitaries.UnorderedPair;

import java.time.LocalDateTime;

/**
 * implementation of AbstractCsvFileRepository
 */
public class FriendshipCsvFileRepository extends
        AbstractCsvFileRepository<UnorderedPair<Long, Long>, Friendship> {

    /**
     * constructor
     * @param filePath - String - absolute path
     */
    public FriendshipCsvFileRepository(String filePath) {
        super(filePath);
    }

    @Override
    public Friendship stringToEntity(String entityAsString) {
        String[] friendshipAttributes = entityAsString.split(",");
        if(friendshipAttributes.length != 3)
            throw new CorruptedDataException("friendship csv file is corrupted");
        Long firstUserId;
        Long secondUserId;
        LocalDateTime date;
        try {
            firstUserId = Long.parseLong(friendshipAttributes[0].stripTrailing().stripLeading());
            secondUserId = Long.parseLong(friendshipAttributes[1].stripTrailing().stripLeading());
            date = LocalDateTime.parse(friendshipAttributes[2].stripTrailing().stripLeading());
        } catch (NumberFormatException exception){
            throw new CorruptedDataException("friendship csv file is corrupted");
        }
        return new Friendship(firstUserId, secondUserId, date);
    }

    @Override
    public String entityToString(Friendship entity) {
        return "" +
                entity.getId().left + "," +
                entity.getId().right + "," +
                entity.getDate().toString();
    }
}
