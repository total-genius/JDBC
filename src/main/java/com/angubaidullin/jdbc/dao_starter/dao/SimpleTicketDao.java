package com.angubaidullin.jdbc.dao_starter.dao;

import com.angubaidullin.jdbc.dao_starter.dto.TicketFilter;
import com.angubaidullin.jdbc.dao_starter.entity.SimpleTicket;
import com.angubaidullin.jdbc.dao_starter.exception.DaoException;
import com.angubaidullin.jdbc.util.HikariCPManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SimpleTicketDao {

    private static final SimpleTicketDao INSTANCE = new SimpleTicketDao();
    //Запрос на удаление
    private static final String DELETE_SQL = """
            DELETE FROM ticket
            WHERE id = ?
            """;
    //Запрос на сохранение
    private static final String SAVE_SQL = """
            INSERT INTO ticket (passenger_name, price, flight_id)
            VALUES (?, ?, ?)
            """;
    //Запрос на обновление
    private static final String UPDATE_SQL = """
            UPDATE ticket
            SET passenger_name=?, 
                price=?, 
                flight_id=?
            WHERE id=?
            """;
    //Запрос на выборку
    private static final String FIND_BY_ID = """
            SELECT id, passenger_name, price, flight_id
            FROM ticket
            WHERE id = ?
            """;
    private static final String FIND_ALL = """
            SELECT id, passenger_name, price, flight_id
            FROM ticket
            """;

    private SimpleTicketDao() {
    }

    public static SimpleTicketDao getInstance() {
        return INSTANCE;
    }

    //Удаление сущности
    public boolean delete(Long id) {
        try (Connection connection = HikariCPManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {

            statement.setLong(1, id);
            int i = statement.executeUpdate();

            return i > 0;

        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    //Сохранение сущности
    public SimpleTicket save(SimpleTicket simpleTicket) {
        try (Connection connection = HikariCPManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, simpleTicket.getPassengerName());
            preparedStatement.setBigDecimal(2, simpleTicket.getPrice());
            preparedStatement.setLong(3, simpleTicket.getFlightId());

            preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();

            if (generatedKeys.next()) {
                simpleTicket.setId(generatedKeys.getLong("id"));
            }

            return simpleTicket;

        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    //Обновление
    public void update(SimpleTicket simpleTicket) {
        try (Connection connection = HikariCPManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_SQL)) {

            if (simpleTicket.getId() != null) {
                preparedStatement.setString(1, simpleTicket.getPassengerName());
                preparedStatement.setBigDecimal(2, simpleTicket.getPrice());
                preparedStatement.setLong(3, simpleTicket.getFlightId());
                preparedStatement.setLong(4, simpleTicket.getId());

                preparedStatement.executeUpdate();

            } else {
                save(simpleTicket);
            }


        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    //Выборка по id
    public Optional<SimpleTicket> findById(Long id) {
        SimpleTicket simpleTicket = null;
        try (Connection connection = HikariCPManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID)) {

            preparedStatement.setLong(1, id);
            System.out.println(preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                simpleTicket = buildTicket(resultSet);
            }

            return Optional.ofNullable(simpleTicket);

        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    //Получение всех билетов
    public List<SimpleTicket> findAll() {
        try (Connection connection = HikariCPManager.getConnection();
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(FIND_ALL);
            List<SimpleTicket> simpleTickets = new ArrayList<>();
            while (resultSet.next()) {
                simpleTickets.add(buildTicket(resultSet));
            }
            return simpleTickets;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    //Поиск с фильтрацией
    public List<SimpleTicket> findAll(TicketFilter filter) {

        List<Object> params = new ArrayList<>();
        String condition = null;
        List<String> whereParams = new ArrayList<>();
        if (filter.price() != null || filter.flightId() != null || filter.passengerName() != null) {
            if (filter.passengerName() != null) {
                params.add(filter.passengerName());
                whereParams.add("passenger_name=?");
            }
            if (filter.price() != null) {
                params.add(filter.price());
                whereParams.add("price=?");
            }
            if (filter.flightId() != null) {
                params.add(filter.flightId());
                whereParams.add("flight_id=?");
            }

            condition = whereParams.stream().collect(Collectors.joining(" AND ", " WHERE ", ""));
        } else {
            condition = "";
        }

        String sql = FIND_ALL + condition;


        try (Connection connection = HikariCPManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }

            List<SimpleTicket> simpleTickets = new ArrayList<>();
            System.out.println(preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                simpleTickets.add(buildTicket(resultSet));
            }
            return simpleTickets;

        } catch (SQLException e) {
            throw new DaoException(e);
        }

    }

    private static SimpleTicket buildTicket(ResultSet resultSet) throws SQLException {
        return new SimpleTicket(
                resultSet.getLong("id"),
                resultSet.getString("passenger_name"),
                resultSet.getBigDecimal("price"),
                resultSet.getLong("flight_id"));
    }


}
