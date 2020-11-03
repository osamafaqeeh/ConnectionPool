package com.osama.pool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * Thread safe connection pool.
 *
 * @author Osama Alfaqeeh
 * @see LinkedList
 * @see Lock
 * @see ConnectionCreator
 */
public class ConnectionPool {
    private static final Logger LOGGER = Logger.getLogger(ConnectionPool.class.getName());

    private static Lock instanceLocker = new ReentrantLock();
    private static Lock poolLocker = new ReentrantLock();
    private static Condition poolCondition = poolLocker.newCondition();

    private static ConnectionPool connectionPool = null;
    private final LinkedList<Connection> pool;

    private static AtomicBoolean isInstanceAvailable = new AtomicBoolean(true);

    private ConnectionPool() {
        ConnectionCreator connectionCreator = new ConnectionCreator();
        pool = connectionCreator.creatPool();
    }

    /**
     * Get instance of connection pool class.
     *
     * @return instance.
     */
    public ConnectionPool instance() {
        if (isInstanceAvailable.get()) {
            instanceLocker.lock();

            try {
                boolean isInstanceAvailableNow = connectionPool == null;
                if (isInstanceAvailableNow) {
                    connectionPool = new ConnectionPool();
                    isInstanceAvailable.set(false);
                }
            } finally {
                instanceLocker.unlock();
            }
        }

        return connectionPool;
    }

    /**
     * Get and remove connection from pool.
     *
     * @return first connection from pool.
     */
    public Connection getConnection() {
        poolLocker.lock();

        try {
            if (pool.isEmpty()) {

                poolCondition.await();
            }
        } catch (InterruptedException exception) {
            throw new IllegalStateException("Can't get connection. ", exception);
        } finally {
            poolLocker.unlock();
        }
        return pool.poll();
    }

/**
 * Adds chosen connection back to pool.
 *
 * @param connection to database, that was get from pool.
 */
    public void returnConnection(Connection connection) {
        poolLocker.lock();

        try {
            pool.addLast(connection);
            poolCondition.signal();
        } finally {
            poolLocker.unlock();
        }
    }

    /**
     * Close all connections in pool.
     */
    public void closePool() {
        for (Connection connection : pool) {
            try {
                connection.close();
            } catch (SQLException exception) {
                LOGGER.warning("Exception was detected during pool closing." + exception.getMessage());
            }
        }

    }

}

