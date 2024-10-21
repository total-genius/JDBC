package com.angubaidullin.jdbc.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class HikariCPManager {
    private static HikariDataSource dataSource;
    private static final String URL_KEY = "db.url";
    private static final String USERNAME_KEY = "db.username";
    private static final String PASSWORD_KEY = "db.password";
    private static final String POOL_SIZE_KEY = "db.pool.size";
    private static final String CONNECTION_TIMEOUT_KEY = "db.connection.timeout";
    private static final String CONNECTION_IDLE_TIMEOUT_KEY = "db.connection.idle.timeout";
    private static final String CONNECTION_MAX_LIFETIME_KEY = "db.connection.max.lifetime";

    private static final Integer DEFAULT_POOL_SIZE = 10;

    private HikariCPManager() {}

    static {
        //Конфигурация пула соединений
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(PropertiesUtil.get(URL_KEY));
        config.setUsername(PropertiesUtil.get(USERNAME_KEY));
        config.setPassword(PropertiesUtil.get(PASSWORD_KEY));

        //Настройка пула
        //Максимальное количество соединений
        Integer maxPoolSize = PropertiesUtil.get(POOL_SIZE_KEY) == null ? DEFAULT_POOL_SIZE : Integer.valueOf(PropertiesUtil.get(POOL_SIZE_KEY));
        config.setMaximumPoolSize(maxPoolSize);
        //Минимальное количество соединений
        Integer minPoolSize = PropertiesUtil.get(POOL_SIZE_KEY) == null ? DEFAULT_POOL_SIZE/2 : Integer.valueOf(PropertiesUtil.get(POOL_SIZE_KEY)) / 2;
        config.setMinimumIdle(minPoolSize);
        //Максимальное время ожидания соединения в миллисекундах
        config.setConnectionTimeout(Integer.valueOf(PropertiesUtil.get(CONNECTION_TIMEOUT_KEY)));
        config.setValidationTimeout(5000);
        //Время простоя соединения перед его удалением из пула
        config.setIdleTimeout(Integer.valueOf(PropertiesUtil.get(CONNECTION_IDLE_TIMEOUT_KEY)));
        //Максимальный срок жизни соединения в пуле
        config.setMaxLifetime(Integer.valueOf(PropertiesUtil.get(CONNECTION_MAX_LIFETIME_KEY)));

        //Инициализация пула соединений
        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public static void reinitialize() {
        // Если пул закрыт или равен null, создаём новый пул
        if (dataSource == null || dataSource.isClosed()) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(PropertiesUtil.get(URL_KEY));
            config.setUsername(PropertiesUtil.get(USERNAME_KEY));
            config.setPassword(PropertiesUtil.get(PASSWORD_KEY));
            config.setMaximumPoolSize(DEFAULT_POOL_SIZE);

            // Создание нового пула
            dataSource = new HikariDataSource(config);
        }
    }
}
