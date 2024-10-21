package com.angubaidullin.jdbc.starter.runner;

import com.angubaidullin.jdbc.util.HikariCPManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JDBCHikariCPRunner {
    public static void main(String[] args) {

        List<String> models = getAirplaneModels();
        models.forEach(System.out::println);

    }

    private static List<String> getAirplaneModels() {
        String sql = "select model from airplane";
        try (Connection connection = HikariCPManager.getConnection();
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(sql);
            List<String> models = new ArrayList<>();
            while (resultSet.next()) {
                models.add(resultSet.getString("model"));
            }
            return models;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            HikariCPManager.close();
        }
    }
}
