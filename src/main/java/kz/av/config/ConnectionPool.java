package kz.av.config;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConnectionPool {
    private static Logger logger = Logger.getRootLogger();
    private static ConnectionPool instance;
    private static BlockingQueue<Connection> pool = null;
    private static final ResourceBundle RB_DB = ResourceBundle.getBundle("jdbc");

    public static ConnectionPool getInstance() {
        ConnectionPool localInstance = instance;

        if (localInstance == null) {

            synchronized (ConnectionPool.class) {
                if (instance == null) {
                    instance = new ConnectionPool();

                    try {
                        createPool();
                    } catch (ConnectionPoolException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return instance;
    }

    private static void createPool() throws ConnectionPoolException {
        final String URL_RB = RB_DB.getString("db.url");
        final String USER_RB = RB_DB.getString("db.user");
        final String PASSWORD_RB = RB_DB.getString("db.password");
        final int POOL_SIZE_RB = Integer.parseInt(RB_DB.getString("db.pool_size"));

        pool = new ArrayBlockingQueue<>(POOL_SIZE_RB, true);

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < POOL_SIZE_RB; i++) {
            Connection connection;

            try {
                connection = DriverManager.getConnection(URL_RB, USER_RB, PASSWORD_RB);
            } catch (SQLException e) {
                throw new ConnectionPoolException("There are no connection to the database", e);
            }

            pool.add(connection);
        }
    }

    public synchronized Connection getConnection() throws ConnectionPoolException {
        Connection connection;

        try {
            connection = pool.take();
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting");
            throw new ConnectionPoolException();
        }

        return connection;
    }

    public void returnConnection(Connection connection) {
        if (connection != null) {
            try {
                pool.put(connection);
                logger.debug("Connection has been returned.");
            } catch (InterruptedException e) {
                logger.debug("Connection cannot be returned");
            }
        }
    }

    public void closeConnections() {
        for (Connection connection : pool) {
            try {
                connection.close();
                logger.debug("Connection has been closed.");
            } catch (SQLException e) {
                logger.error("Connection cannot be closed.");
            }
        }
    }
}