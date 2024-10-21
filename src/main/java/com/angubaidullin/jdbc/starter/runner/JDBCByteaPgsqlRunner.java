package com.angubaidullin.jdbc.starter.runner;

import com.angubaidullin.jdbc.util.ConnectionManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCByteaPgsqlRunner {
    public static void main(String[] args) throws SQLException {
        saveImage(3, "src/main/resources/boeing.jpeg");

        getImage(3, "src/main/resources/get-result/get-boeing.jpg");
    }

    private static void saveImage(int id, String imagePath) throws SQLException {

        String sqlInsert = """
                UPDATE airplane
                SET image = ?
                WHERE id = ?
                """;

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = ConnectionManager.open();
            connection.setAutoCommit(false);

            preparedStatement = connection.prepareStatement(sqlInsert);

            preparedStatement.setBytes(1, Files.readAllBytes(Paths.get(imagePath)));
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();

            connection.commit();

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }

    }

    private static void getImage(int id, String locationPath) throws SQLException {
        String sqlGet = """
                SELECT image
                FROM airplane
                WHERE id = ?
                """;
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = ConnectionManager.open();
            connection.setAutoCommit(false);

            preparedStatement = connection.prepareStatement(sqlGet);

            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                byte[] imageBytes = resultSet.getBytes("image");
                Files.write(Paths.get(locationPath), imageBytes, StandardOpenOption.CREATE);
            }

            connection.commit();

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }
}
