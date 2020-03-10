package kz.av.dao;

import kz.av.config.ConnectionPool;
import kz.av.entity.Entity;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDao<K extends Integer, T extends Entity> {
    private Logger logger = Logger.getRootLogger();
    private static final String STATEMENT_CREATE_ERROR = "Statement cannot be created.";
    private DaoFactory daoFactory = new DaoFactory();
    private final ConnectionPool pool = ConnectionPool.getInstance();

    public List<T> getAll() throws DaoException {
        List<T> list = new ArrayList<>();
        Connection connection = daoFactory.getConnection();
        String query = getReadQuery();

        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                T object = parseResultSetInstance(resultSet);
                list.add(object);
            }

            resultSet.close();
        } catch (Exception e) {
            throw new DaoException(STATEMENT_CREATE_ERROR, e);
        } finally {
            pool.returnConnection(connection);
        }

        return list;
    }

    public T getById(K id) {
        T object = null;
        Connection connection = daoFactory.getConnection();
        String query = getReadQuery() + " WHERE ID = ?;";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                object = parseResultSetInstance(resultSet);
            }

            resultSet.close();

        } catch (Exception e) {
            logger.error(STATEMENT_CREATE_ERROR);
        } finally {
            pool.returnConnection(connection);
        }

        return object;
    }

    public int create(T entity) throws DaoException {
        Connection connection = daoFactory.getConnection();
        String query = getCreateQuery();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statementForCreate(statement, entity);
            return statement.executeUpdate();
        } catch (Exception e) {
            throw new DaoException(STATEMENT_CREATE_ERROR, e);
        } finally {
            pool.returnConnection(connection);
        }
    }

    public int update(T entity) throws DaoException {
        Connection connection = daoFactory.getConnection();
        String query = getUpdateQuery();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statementForUpdate(statement, entity);
            return statement.executeUpdate();
        } catch (Exception e) {
            throw new DaoException(e);
        } finally {
            pool.returnConnection(connection);
        }
    }

    public abstract void statementForCreate(PreparedStatement statement, T entity) throws DaoException;

    public abstract void statementForUpdate(PreparedStatement statement, T entity) throws DaoException;

    public abstract T parseResultSetInstance(ResultSet resultSet) throws DaoException;

    public abstract String getReadQuery();

    public abstract String getCreateQuery();

    public abstract String getUpdateQuery();
}