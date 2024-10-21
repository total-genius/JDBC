package com.angubaidullin.jdbc.dao_starter.dao;

import com.angubaidullin.jdbc.dao_starter.entity.Flight;
import com.angubaidullin.jdbc.dao_starter.exception.DaoException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class FlightDao {
    private static final FlightDao INSTANCE = new FlightDao();
    private static final AirportDao airportDao = AirportDao.getInstance();
    private static final AirplaneDao airplaneDao = AirplaneDao.getInstance();
    //Select
    private static final String FIND_BY_ID = """
            SELECT id, departure_airport_id, arrival_airport_id, departure_time, arrival_time, airplane_id
            FROM flight
            WHERE id=?
            """;
    private static final String FIND_ALL = """
            SELECT id, departure_airport_id, arrival_airport_id, departure_time, arrival_time, airplane_id
            FROM flight
            """;

    private FlightDao() {
    }

    public static FlightDao getInstance() {
        return INSTANCE;
    }

    //Select
    public Optional<Flight> findById(Long id, Connection connection) {
        Flight flight = null;
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                flight = buildFlight(resultSet, connection);

            }
            return Optional.ofNullable(flight);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    private static Flight buildFlight(ResultSet resultSet, Connection connection) throws SQLException {
        Flight flight = new Flight();
        flight.setId(resultSet.getLong("id"));
        flight.setDepartureAirport(airportDao.findById(resultSet.getLong("departure_airport_id"), connection).get());
        flight.setArrivalAirport(airportDao.findById(resultSet.getLong("arrival_airport_id"), connection).get());
        flight.setDepartureDate(resultSet.getTimestamp("departure_time").toLocalDateTime().toLocalDate());
        flight.setArrivalDate(resultSet.getTimestamp("arrival_time").toLocalDateTime().toLocalDate());
        flight.setAirplane(airplaneDao.findById(resultSet.getLong("airplane_id"), connection).get());
        return flight;

    }
}
