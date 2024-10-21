package com.angubaidullin.jdbc.util;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConnectionPoolManager {
    private static final String URL_KEY = "db.url";
    private static final String USERNAME_KEY = "db.username";
    private static final String PASSWORD_KEY = "db.password";
    private static final String POOL_SIZE_KEY = "db.pool.size";
    private static final Integer DEFAULT_POOL_SIZE = 10;
    private static BlockingQueue<Connection> proxyPool;
    private static List<Connection> sourceConnections;


    static {
        loadDriver();
        initConnectionPool();
    }

    private ConnectionPoolManager() {
    }


    private static void loadDriver() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        try {
            return proxyPool.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initConnectionPool() {
        String poolSizeFromProps = PropertiesUtil.get(POOL_SIZE_KEY);
        int poolSize = poolSizeFromProps == null ? DEFAULT_POOL_SIZE : Integer.parseInt(poolSizeFromProps);
        proxyPool = new ArrayBlockingQueue<Connection>(Integer.valueOf(poolSize));
        sourceConnections = new ArrayList<Connection>(Integer.valueOf(poolSize));

        for (int i = 0; i < poolSize; i++) {
            Connection connection = open();
            Connection proxyConnection = (Connection) Proxy.newProxyInstance(ConnectionPoolManager.class.getClassLoader(),
                    new Class[]{Connection.class},
                    (proxy, method, args) ->
                            method.getName().equals("close") ? proxyPool.add((Connection) proxy) : method.invoke(connection, args));
            proxyPool.add(proxyConnection);
            sourceConnections.add(connection);
        }
    }


    private static Connection open() {
        try {
            return DriverManager.getConnection(
                    PropertiesUtil.get(URL_KEY),
                    PropertiesUtil.get(USERNAME_KEY),
                    PropertiesUtil.get(PASSWORD_KEY)
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void closePool() {
        System.out.println("Closing connection pool");
        for (Connection connection : sourceConnections) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
