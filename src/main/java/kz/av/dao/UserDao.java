package kz.av.dao;

import kz.av.entity.User;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Component()
@Repository
@ComponentScan("kz.av")
@Qualifier("userDao")
public class UserDao extends AbstractDao<Integer, User> implements Dao<User> {
    private static final String STATEMENT_CREATE_ERROR = "Statement cannot be created.";
    private static final Logger logger = Logger.getRootLogger();

    @Autowired
    private DaoFactory daoFactory;

    private static final String CREATE_USER = "INSERT INTO users(FIRSTNAME, LASTNAME, USERNAME, EMAIL, PASSWORD " +
            ") VALUES(?, ?, ?, ?, ?, ?);";
    private static final String GET_USER_BY_USERNAME = "SELECT * FROM USERS WHERE USERNAME = ?;";
    private static final String GET_USER_BY_ID = "SELECT * FROM USERS WHERE ID = ?;";
    private static final String CHANGE_PASSWORD = "UPDATE USERS SET PASSWORD= ? WHERE ID = ?;";
    private static final String FIND_ALL_USERS = "SELECT * FROM users;";
    private static final String UPDATE_USER = "UPDATE USERS SET FIRSTNAME = ?, LASTNAME = ?, USERNAME = ?, EMAIL = ?," +
            " PASSWORD = ?  WHERE ID = ?;";
    private static final String GET_ALL_USERS_BY_USERNAME = "SELECT * FROM USERS WHERE USERNAME = ?;";

    public UserDao() {
    }

    public UserDao(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    public User getByUsername(String username) throws DaoException {
        User user = null;
        Connection connection = daoFactory.getConnection();
        ResultSet resultSet = null;

        try (PreparedStatement statement = connection.prepareStatement(GET_USER_BY_USERNAME)) {
            statement.setString(1, username);
            resultSet = statement.executeQuery();

            if (resultSet.first()) {
                user = parseResultSetInstance(resultSet);
            }
        } catch (Exception e) {
            logger.error(STATEMENT_CREATE_ERROR, e);
            throw new DaoException(e);
        } finally {
            daoFactory.closeResultSet(resultSet, connection);
        }

        if (user == null) {
            user = new User();
        }

        return user;
    }


    @Override
    public User getById(Integer id) {
        User user = null;
        Connection connection = daoFactory.getConnection();
        ResultSet resultSet = null;

        try (PreparedStatement statement = connection.prepareStatement(GET_USER_BY_ID)) {
            statement.setInt(1, id);
            resultSet = statement.executeQuery();

            if (resultSet.first()) {
                user = parseResultSetInstance(resultSet);
            }
        } catch (Exception e) {
            logger.error(STATEMENT_CREATE_ERROR, e);
        } finally {
            daoFactory.closeResultSet(resultSet, connection);
        }
        return user;
    }


    public void resetPassword(String newPassword, Integer id) throws DaoException {
        Connection connection = daoFactory.getConnection();

        try (PreparedStatement statement = connection.prepareStatement(CHANGE_PASSWORD)) {
            statement.setString(1, newPassword);
            statement.setInt(2, id);
            statement.executeUpdate();
        } catch (Exception e) {
            logger.error(STATEMENT_CREATE_ERROR, e);
        } finally {
            daoFactory.returnConnection(connection);
        }
    }

    public Integer getUsersListByUsername(String username) throws DaoException {
        List<User> users = new ArrayList<>();
        Connection connection = daoFactory.getConnection();
        ResultSet resultSet = null;

        try (PreparedStatement statement = connection.prepareStatement(GET_ALL_USERS_BY_USERNAME)) {
            statement.setString(1, username);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                User user = parseResultSetInstance(resultSet);
                users.add(user);
            }
        } catch (Exception e) {
            logger.error(STATEMENT_CREATE_ERROR, e);
            throw new DaoException(e);
        } finally {
            daoFactory.closeResultSet(resultSet, connection);
        }

        return users.size();
    }

    @Override
    public void statementForCreate(PreparedStatement statement, User entity) throws DaoException {
        try {
            statement.setString(1, entity.getFirstname());
            statement.setString(2, entity.getLastname());
            statement.setString(3, entity.getUsername());
            statement.setString(4, entity.getEmail());
            statement.setString(5, entity.getPassword());
           } catch (Exception e) {
            logger.error("Preparing statement for Create User error", e);
            throw new DaoException(e);
        }
    }

    @Override
    public void statementForUpdate(PreparedStatement statement, User entity) throws DaoException {
        try {
            statementForCreate(statement, entity);
            statement.setInt(6, entity.getId());
        } catch (Exception e) {
            logger.error("Preparing statement for Update User error", e);
            throw new DaoException(e);
        }
    }

    @Override
    public User parseResultSetInstance(ResultSet resultSet) throws DaoException {
        User user = new User();

        try {
            user.setId(resultSet.getInt("ID"));
            user.setFirstname(resultSet.getString("FIRSTNAME"));
            user.setLastname(resultSet.getString("LASTNAME"));
            user.setUsername(resultSet.getString("USERNAME"));
            user.setEmail(resultSet.getString("EMAIL"));
            user.setPassword(resultSet.getString("PASSWORD"));
        } catch (Exception e) {
            logger.error("Error of results parsing", e);
            throw new DaoException(e);
        }
        return user;
    }

    @Override
    public String getReadQuery() {
        return FIND_ALL_USERS;
    }

    @Override
    public String getCreateQuery() {
        return CREATE_USER;
    }

    @Override
    public String getUpdateQuery() {
        return UPDATE_USER;
    }
}