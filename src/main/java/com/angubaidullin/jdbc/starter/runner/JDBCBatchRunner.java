package com.angubaidullin.jdbc.starter.runner;

import com.angubaidullin.jdbc.util.ConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCBatchRunner {
    public static void main(String[] args) throws SQLException {

        deleteFlight(7);

    }

    private static void deleteFlight(int flightId) throws SQLException {
        String sqlDeleteFlight = """
                DELETE FROM flight WHERE id =
                """ + flightId;

        String sqlDeleteTicket = """
                DELETE FROM simpleTicket WHERE flight_id = 
                """ + flightId;
        Connection connection = null;
        Statement statement = null;

        try {
            connection = ConnectionManager.open();

            connection.setAutoCommit(false);

            statement = connection.createStatement();
            statement.addBatch(sqlDeleteTicket);
            statement.addBatch(sqlDeleteFlight);

            int[] ints = statement.executeBatch();

            connection.commit();

        } catch (Exception e) {
            if (connection != null) {
                connection.rollback();
            }
            throw e;
        } finally {
            if (connection != null) {
                connection.close();
            }
            if (statement != null) {
                statement.close();
            }
        }
    }
}
