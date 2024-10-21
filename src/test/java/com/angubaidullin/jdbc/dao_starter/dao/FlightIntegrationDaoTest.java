package com.angubaidullin.jdbc.dao_starter.dao;

import com.angubaidullin.jdbc.dao_starter.entity.Flight;
import com.angubaidullin.jdbc.util.HikariCPManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class FlightIntegrationDaoTest {
    private static PostgreSQLContainer<?> postgreSQLContainer;
    private static FlightDao flightDao;

    @BeforeAll
    static void setUp() {
        //Инициализация контейнера postgres
        postgreSQLContainer = new PostgreSQLContainer<>("postgres:15")
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass");
        //Запуск контейнера
        postgreSQLContainer.start();
        System.out.println(postgreSQLContainer.getLogs());
        try {
            Thread.sleep(5000);  // Задержка 5 секунд
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println(postgreSQLContainer.getLogs());


        //Установка новых свойств для подключения к тестовой базе данных
        System.setProperty("db.url", postgreSQLContainer.getJdbcUrl());
        System.setProperty("db.username", postgreSQLContainer.getUsername());
        System.setProperty("db.password", postgreSQLContainer.getPassword());

        //Повторная инициализация пула соединений
        HikariCPManager.close();
        HikariCPManager.reinitialize();
        flightDao = flightDao.getInstance();

        //Выполнение sql скрипта
        executeSqlScript("test_data.sql");
    }

    @AfterAll
    static void tearDown() {
        //Закрытие контейнера и пула соединений после выполнения тестов
        postgreSQLContainer.stop();
        HikariCPManager.close();
    }

    @Test
    void testFindById() throws SQLException {
        //Проверка метода findById
        try (Connection connection = HikariCPManager.getConnection()) {
            Optional<Flight> flight = flightDao.findById(1L, connection);

            assertAll(
                    () -> assertTrue(flight.isPresent()),
                    () -> assertEquals(1l, flight.get().getId()),
                    () -> assertEquals("Moscow", flight.get().getDepartureAirport().getCity())
            );
        }
    }

    //Метод для выполнения SQL скрипта
    private static void executeSqlScript(String scriptPath) {
        try (Connection connection = HikariCPManager.getConnection();
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(
                             FlightIntegrationDaoTest.class
                                     .getClassLoader()
                                     .getResourceAsStream(scriptPath)
                     )
             )) {

            String sql = reader.lines().collect(Collectors.joining("\n"));
            connection.createStatement().execute(sql);


        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}