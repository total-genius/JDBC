package com.angubaidullin.jdbc.starter.runner;

import com.angubaidullin.jdbc.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JDBCTransactionRunner {
    public static void main(String[] args) throws SQLException {

        deleteFlight(7);

    }

    private static void deleteFlight(int flightId) throws SQLException {
        String sqlDeleteFlight = """
                DELETE FROM flight WHERE id = ?
                """;

        String sqlDeleteTicket = """
                DELETE FROM simpleTicket WHERE flight_id = ?
                """;
        Connection connection = null;
        PreparedStatement deleteTicketStatement = null;
        PreparedStatement deleteFlightStatement = null;

        try {
            connection = ConnectionManager.open();
            deleteTicketStatement = connection.prepareStatement(sqlDeleteTicket);
            deleteFlightStatement = connection.prepareStatement(sqlDeleteFlight);

            /*
            По умолчанию у нас происходит автокомит. Каждый запрос
            выполняет в отдельной транзакции и происходит автоматический
            комит. Поэтому нужно отключить автокомит для ручного управления
            транзакцией.
             */

            connection.setAutoCommit(false);

            deleteTicketStatement.setLong(1, flightId);
            deleteFlightStatement.setInt(1, flightId);

            deleteTicketStatement.executeUpdate();
            deleteFlightStatement.executeUpdate();

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
            if (deleteTicketStatement != null) {
                deleteTicketStatement.close();
            }
            if (deleteFlightStatement != null) {
                deleteFlightStatement.close();
            }
        }
    }
}
