package com.angubaidullin.jdbc.starter.runner;

import com.angubaidullin.jdbc.util.ConnectionManager;
import com.angubaidullin.jdbc.util.ConnectionPoolManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JDBCRunner {
    public static void main(String[] args) throws SQLException {

        checkMetaData();

    }

    private static void checkMetaData() {
        try(Connection connection = ConnectionPoolManager.getConnection()) {

            //Для получения метаданных БД мы оперируем соединением (Connection connection)
            DatabaseMetaData metaData = connection.getMetaData();
            //Получим каталоги (Базы данных)
            ResultSet catalogs = metaData.getCatalogs();
            System.out.println("catalogs: ");
            while (catalogs.next()) {
                System.out.println(catalogs.getString(1));

            }
            System.out.println();
            //Получим схемы
            ResultSet schemas = metaData.getSchemas();
            System.out.println("schemas: ");
            while (schemas.next()) {
                System.out.println(schemas.getString(1));
            }
            System.out.println();

            //Получим все таблицы:
            ResultSet tables = metaData.getTables("mydatabase", "public", "%s", null);
            System.out.println("All tables:");
            while (tables.next()) {
                System.out.println(tables.getString("TABLE_NAME"));
            }
            System.out.println();

            //Получим колонки нашей таблицы
            ResultSet columns = metaData.getColumns("mydatabase", "public", "%", null);
            System.out.println("All columns:");
            while (columns.next()) {
                System.out.println(columns.getString("COLUMN_NAME"));
            }
            System.out.println();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //Получаем список фамилий студентов чей возраст находится в указанном диапозоне
    private static List<String> getStudentLastnameBetweenAge(int ageFrom, int ageTo){
        String sqlGetBetweenAges = """
                SELECT lastname
                FROM students
                WHERE age BETWEEN ? AND ?
                """;

        List<String> result = new ArrayList<>();
        try (Connection connection = ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(sqlGetBetweenAges)) {

            statement.setFetchSize(3);
            statement.setQueryTimeout(30);
            statement.setMaxRows(100);

            statement.setInt(1, ageFrom);
            statement.setInt(2, ageTo);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                result.add(resultSet.getString(1));
            }
            return result;


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //Получаем студентов по их возрасту
    private static List<String> getStudentsByAge(String age) {
        String sqlGetByAge = """
                SELECT firstname
                FROM students
                WHERE age = ?
                """;

        List<String> studentFirstnames = new ArrayList<>();

        try (Connection connection = ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(sqlGetByAge)) {

            statement.setInt(1, Integer.parseInt(age));

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                studentFirstnames.add(resultSet.getString(1));
            }



        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return studentFirstnames;
    }
}
