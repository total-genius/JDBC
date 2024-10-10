# JDBC Advanced

## FetchSize
Java программы, с которого мы делаем запросы в БД - это одно приложение. База данных, куда поступают запросы
с нашего прилоежения - это другое приложение. Эти два приложения как правило крутятся на разных серверах. Чтобы 
настроить связь между нашей программой и БД мы устанавливаем между ними соединение.

Когда мы делаем запрос для выборки данных из базы результатом может оказать огромное количесво строк, которое может
не поместиться в памяти приложения. Поэтому будет разумным получать данные из базы не целым скопом сразу, а небольшими
кусками. 

```java

package com.angubaidullin.jdbc.starter;

import com.angubaidullin.jdbc.starter.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JDBCRunner {
    public static void main(String[] args) throws SQLException {

        List<String> studentLastnameBetweenAge = getStudentLastnameBetweenAge(23, 28);
        studentLastnameBetweenAge.forEach(System.out::println);

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
}

```

- `statement.setFetchSize(3);` - устанавливаем количесвто строк, которое будет загружаться в память приложения 
за один раз при извлечении данных из базы. Вместо того чтобы загружать все результаты запроса сразу, можно ограничить 
количество строк, которые сервер базы данных передаёт за один раз. В данном случае, если запрос вернёт много строк, 
Java-клиент будет получать по 3 строки за раз. Это может быть полезно, если данные большие и нужно экономить память.
- `statement.setQueryTimeout(30);` - устанавливает максимальное время ожидания (в секундах) для выполнения SQL-запроса.
Если запрос к базе данных выполняется слишком долго (например, база данных перегружена), программа не будет ждать 
бесконечно. В данном случае, если запрос не выполнится за 30 секунд, произойдёт ошибка.
- `statement.setMaxRows(100);` - ограничивает максимальное количество строк, которые будут возвращены запросом. Если 
запрос вернёт слишком много строк, можно ограничить количество получаемых строк, чтобы не загружать слишком много данных 
в память. В данном случае программа не получит больше 100 строк, даже если в базе данных есть больше записей, 
соответствующих запросу.

---

## MetaData
С помощью JDBC мы также можем получить всю интересующую нас метаинформацию о базе данных:
```java

package com.angubaidullin.jdbc.starter;

import com.angubaidullin.jdbc.starter.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JDBCRunner {
    public static void main(String[] args) throws SQLException {

        checkMetaData();

    }

    private static void checkMetaData() {
        try(Connection connection = ConnectionManager.open()) {

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
        
}

```

---

## Транзакции и блокировки
Ранее все наши запросы выполнялись в `autocommit` моде. Каждый запрос выполнялся в отдельной транзакции.
Однако часто бывает так, что в рамках одной транзакции нужно выполнить несколько sql операций. Например, у нас имеется
таблица полетов и таблица билетов. При удалении какого-то из полетов, так же нужно и удалить все билеты, который были 
связаны с этим полетом. Более того, если на какую-то из нашей таблицы ссылается другая строка из другой таблицы, то при
попытке удаления строки из первой таблицы без удаления ссылающейся на нее строки из другой таблицы мы получим исключение.

```java

package com.angubaidullin.jdbc.starter;

import com.angubaidullin.jdbc.starter.util.ConnectionManager;

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
                DELETE FROM ticket WHERE flight_id = ?
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

```


