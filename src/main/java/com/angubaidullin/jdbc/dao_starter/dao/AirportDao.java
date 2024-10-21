package com.angubaidullin.jdbc.dao_starter.dao;

import com.angubaidullin.jdbc.dao_starter.entity.Airport;
import com.angubaidullin.jdbc.dao_starter.exception.DaoException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AirportDao {
    private static final AirportDao INSTANCE = new AirportDao();
    //Delete
    private static final String DELETE_SQL = """
            DELETE FROM airport
            WHERE id = ?
            """;
    //Save
    private static final String SAVE_SQL = """
            INSERT INTO airport (city, airport_name) 
            VALUES (?,?)         
            """;
    //Update
    private static final String UPDATE_SQL = """
            UPDATE airport 
            SET city = ?, airport_name = ? 
            WHERE id = ?
            """;
    //Select
    private static final String FIND_BY_ID = """
            SELECT id, city, airport_name 
            FROM airport
            WHERE id = ?
            """;
    private static final String FIND_ALL = """
            SELECT id, city, airport_name
            FROM airport
            """;
    private AirportDao() {}
    public static AirportDao getInstance() {
        return INSTANCE;
    }

    //Select by id
    public Optional<Airport> findById(Long id, Connection connection) {
        Airport airport = null;
        try(PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                airport = buildAirport(resultSet);
            }
            return Optional.ofNullable(airport);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
    //Find All
    public List<Airport> findAll(Connection connection) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL)){
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Airport> airports = new ArrayList<>();
            while (resultSet.next()) {
                airports.add(buildAirport(resultSet));
            }
            return airports;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    private static Airport buildAirport(ResultSet rs) throws SQLException {
        return new Airport(rs.getLong("id"), rs.getString("city"), rs.getString("airport_name"));
    }
}
