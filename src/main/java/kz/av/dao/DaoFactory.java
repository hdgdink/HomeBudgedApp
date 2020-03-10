package kz.av.dao;

import kz.av.config.ConnectionPool;
import kz.av.config.ConnectionPoolException;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@Repository
@ComponentScan("kz.av")
public class DaoFactory {
    private final ConnectionPool pool = ConnectionPool.getInstance();
    private static final Logger logger = Logger.getRootLogger();

    public DaoFactory(){
    }

    public Connection getConnection(){
        Connection connection = null;

        try {
            connection = pool.getConnection();
            logger.debug("Connection has been taken.");
        } catch (ConnectionPoolException e) {
            logger.warn("Connection can not be taken.", e);
        }

        return connection;
    }

    public void closeResultSet(ResultSet resultSet, Connection connection) {
        if (resultSet != null) {

            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
                logger.error("Error during ResultSet closing ");
            }
        }

        returnConnection(connection);
    }

    public void returnConnection(Connection connection) {
        if (connection != null) {
            pool.returnConnection(connection);
        }
    }
}