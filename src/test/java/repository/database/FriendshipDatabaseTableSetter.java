package repository.database;

import config.ApplicationContext;
import socialNetwork.domain.models.Friendship;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class FriendshipDatabaseTableSetter {
    static String url = ApplicationContext.getProperty("network.database.url");
    static String user = ApplicationContext.getProperty("network.database.user");
    static String password = ApplicationContext.getProperty("network.database.password");

    public static void tearDown(){
        try(Connection connection = DriverManager.getConnection(url, user, password)) {
            var deleteStatement = connection.prepareStatement("DELETE FROM friendships");
            deleteStatement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public static void setUp(List<Friendship> testData){
        tearDown();

        try(Connection connection = DriverManager.getConnection(url, user, password)) {
            String insertStatementString = "INSERT INTO friendships(id_first_user, id_second_user, date) VALUES (?,?,?)";
            PreparedStatement insertStatement = connection.prepareStatement(insertStatementString);
            for(Friendship friendship : testData) {
                insertStatement.setLong(1, friendship.getId().left);
                insertStatement.setLong(2, friendship.getId().right);
                Timestamp time = Timestamp.valueOf(friendship.getDate());
                insertStatement.setTimestamp(3, time);
                insertStatement.executeUpdate();
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
