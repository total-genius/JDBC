package com.angubaidullin.jdbc.starter.runner;

import com.angubaidullin.jdbc.util.ConnectionPoolManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JDBCConnectionPoolRunner {
    public static void main(String[] args) {
        try {
            List<String> airplanesModel = getAirplanesModel();
            airplanesModel.forEach(System.out::println);
        } finally {
            ConnectionPoolManager.closePool();
        }


    }

    private static List<String> getAirplanesModel() {
        String sql = "select model from airplane";

        try (Connection connection = ConnectionPoolManager.getConnection();
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(sql);
            List<String> airplanesModel = new ArrayList<>();
            while (resultSet.next()) {
                airplanesModel.add(resultSet.getString("model"));
            }
            return airplanesModel;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
