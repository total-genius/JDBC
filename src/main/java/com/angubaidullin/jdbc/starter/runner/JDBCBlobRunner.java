package com.angubaidullin.jdbc.starter.runner;

import com.angubaidullin.jdbc.util.ConnectionManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JDBCBlobRunner {
    public static void main(String[] args) throws SQLException, IOException {
        /*
        BLOB - Binary Large Object. Тип данных, который
        можно использовать для объектов, которые можно
        представить в байтовом виде (все, что угодно, но
        как правило используется для изображений, видео и т.д.)

        CLOB - Character Large Object. Символьные объекты

        Не во всех СУБД присутствуют эти типы данных.
        Например, в Postgres нет BLOB и CLOB. Вместо BLOB используется
        bytea - массив байт (по сути то же самое)
        Вместо CLOB - TEXT
         */

        saveImage(3,"src/main/resources/boeing.jpeg" );


    }

    /*
    Ниже описан пример работы с BLOB и CLOB. Однако, поскольку postgres
    не поддерживает данные форматы с данной БД он работать не будет
     */

    private static void saveImage(int id, String imagePath) throws SQLException, IOException {

        String sqlInsert = """
                UPDATE airplane
                SET image = ?
                WHERE id = ?
                """;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            /*
            При работе с BLOB & CLOB
            рекомендуется использовать транзакции
             */
            connection = ConnectionManager.open();

            connection.setAutoCommit(false);

            preparedStatement = connection.prepareStatement(sqlInsert);

            Blob blob = connection.createBlob();
            blob.setBytes(1, Files.readAllBytes(Paths.get(imagePath)));

            preparedStatement.setBlob(1, blob);
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();

            connection.commit();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            connection.rollback();
            throw e;
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
