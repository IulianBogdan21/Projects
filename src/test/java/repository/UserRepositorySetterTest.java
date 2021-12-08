package repository;

import config.ApplicationContext;
import socialNetwork.domain.models.User;
import socialNetwork.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class UserRepositorySetterTest extends RepositoryAbstractTest<Long, User> {

    String url = ApplicationContext.getProperty("network.database.url");
    String user = ApplicationContext.getProperty("network.database.user");
    String password = ApplicationContext.getProperty("network.database.password");

    public Long getMaximumId(){
        try(Connection connection = DriverManager.getConnection(url, user, password)){
            String findMaximumString = "select max(id) from users";
            PreparedStatement findSql = connection.prepareStatement(findMaximumString);
            ResultSet resultSet = findSql.executeQuery();
            return resultSet.getLong("");
        } catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    @Override
    public Long getExistingId() {
        return getMaximumId();
    }

    @Override
    public Long createNotExistingId() {
        return (getMaximumId() + 2);
    }

    @Override
    public List<User> getTestData() {
        return new ArrayList<>(Arrays.asList(
                new User("Baltazar","Baltazar"),
                new User("Bradley","Bradley"),
                new User("Frank","Frank"),
                new User("Johnny","John"),
                new User("Johnny","John")
                ));
    }

    @Override
    public User createValidEntity() {
        return new User("Brutus","Brutus");
    }

}
