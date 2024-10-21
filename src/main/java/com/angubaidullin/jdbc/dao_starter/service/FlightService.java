package com.angubaidullin.jdbc.dao_starter.service;

import com.angubaidullin.jdbc.dao_starter.dao.FlightDao;
import com.angubaidullin.jdbc.dao_starter.entity.Flight;
import com.angubaidullin.jdbc.util.HikariCPManager;

import java.sql.Connection;
import java.sql.SQLException;

public class FlightService {
    private static final FlightDao FLIGHT_DAO = FlightDao.getInstance();
    private static final FlightService INSTANCE = new FlightService();
    private FlightService() {}
    public static FlightService getInstance() {
        return INSTANCE;
    }

    public Flight getFlightById(Long id) throws SQLException {
        Connection connection = null;
        try {
            connection = HikariCPManager.getConnection();
            connection.setAutoCommit(false);
            Flight flight = FLIGHT_DAO.findById(id, connection).get();
            connection.commit();
            return flight;
        } catch (SQLException e) {
            if (connection != null) {
                connection.rollback();
            }
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
            }
        }
    }
}
