package com.osama.pool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Util class that creates connection to database using file resources.
 *
 * @author Osama Alfaqeeh
 * @see Connection
 * @see Properties
 * @see ResourceBundle
 */
public class ConnectionCreator {

    private static final String RESOURCE_FILE_NAME = "database";
    private static final String POOL_SIZE = "db.poolSize";
    private static final String URL_PROPERTY_KEY = "db.url";
    private static final String USER_PROPERTY_KEY = "db.user";
    private static final String PASSWORD_PROPERTY_KEY = "db.password";
    private static final String USER_PROPERTY = "user";
    private static final String PASSWORD_PROPERTY = "password";

    private static final Logger LOGGER = Logger.getLogger(ConnectionCreator.class.getName());

    private ResourceBundle resourceBundle = ResourceBundle.getBundle(RESOURCE_FILE_NAME);

    /**
     * Create pool of connections to chosen database.
     *
     * @return LinkedList object.
     */
    public LinkedList<Connection> creatPool() {
        LinkedList<Connection> pool = new LinkedList<Connection>();

        int poolSize = Integer.getInteger(resourceBundle.getString(POOL_SIZE));
        for (int i = 0; i < poolSize; i++) {
            pool.addLast(creatConnection());
        }

        LOGGER.info("Pool was created successful.");
        return pool;
    }

    /**
     * Create connection to chosen database using properties.
     *
     * @return created connection.
     */
    private Connection creatConnection() {
        String url = resourceBundle.getString(URL_PROPERTY_KEY);
        String user = resourceBundle.getString(USER_PROPERTY_KEY);
        String password = resourceBundle.getString(PASSWORD_PROPERTY_KEY);

        Properties properties = new Properties();
        properties.put(USER_PROPERTY, user);
        properties.put(PASSWORD_PROPERTY, password);

        try {
            Connection connection = DriverManager.getConnection(url, properties);

            LOGGER.info("Connection was created successful.");

            return connection;
        } catch (SQLException e) {
            LOGGER.warning("Connection hasn't been created. ");
            throw new ExceptionInInitializerError("Connection hasn't been created. " + e.getMessage());
        }
    }
}
