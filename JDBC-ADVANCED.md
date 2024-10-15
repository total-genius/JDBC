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
---

## Batch запросы
При обычных выполнениях sql запросов через Java приложение, каждый запрос по очереди посылается на сервер
с базой данных выполняется там, затем от сервера базы данных возвращается ответ. И так с каждым из запросов.
Если запросов много, будет разуменее отправить их на выполенение на сервер одни скопом. Проделать это мы можем 
с помощью Batch запросов.

```java

package com.angubaidullin.jdbc.starter.util;

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
                DELETE FROM ticket WHERE flight_id = 
                """ + flightId;
        Connection connection = null;
        Statement statement = null;

        try {
            connection = ConnectionManager.open();

            connection.setAutoCommit(false);

            /*
              Batch запросы можно выполнять с помощью Statement.
              PreparedStatement для этого не подходит, потому что 
              при их создании в аргументы нужно сразу передавать 
              sql выражение.      
             */
            statement = connection.createStatement();
            /*
             Поочередно добавляем в Batch все запросы
             которые мы хотим отправить на сервер БД одной группой       
             */
            statement.addBatch(sqlDeleteTicket);
            statement.addBatch(sqlDeleteFlight);
            //Отправляем на сервер БД все запросы в Batch
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

```

---

## Blob & Clob
BLOB - Binary Large Object. Тип данных, который
можно использовать для объектов, которые можно
представить в байтовом виде (все, что угодно, но
как правило используется для изображений, видео и т.д.)
CLOB - Character Large Object. Символьные объекты

Не во всех СУБД присутствуют эти типы данных.
Например, в Postgres нет BLOB и CLOB. Вместо BLOB используется
bytea - массив байт (по сути то же самое)
Вместо CLOB - TEXT
```java

package com.angubaidullin.jdbc.starter;

import com.angubaidullin.jdbc.starter.util.ConnectionManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JDBCBlobRunner {
    public static void main(String[] args) throws SQLException, IOException {
        
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



```

**Пример, как работать с изображениями в Postgres:**
```java

package com.angubaidullin.jdbc.starter;

import com.angubaidullin.jdbc.starter.util.ConnectionManager;

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

```
На практике не принято хранить изображения и видео в базе данных. Для этого используются другие сторонние сервисы,
а в базе хранят лишь ссылку на изображения или видео на этих сервисах.

---

## Пул соединений (Connection Pool)

```java

package com.angubaidullin.jdbc.starter.util;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConnectionPoolManager {
    private static final String URL_KEY = "db.url";
    private static final String USERNAME_KEY = "db.username";
    private static final String PASSWORD_KEY = "db.password";
    private static final String POOL_SIZE_KEY = "db.pool.size";
    private static final Integer DEFAULT_POOL_SIZE = 10;
    private static BlockingQueue<Connection> proxyPool;
    private static List<Connection> sourceConnections;


    static {
        loadDriver();
        initConnectionPool();
    }

    private ConnectionPoolManager() {
    }


    private static void loadDriver() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        try {
            return proxyPool.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initConnectionPool() {
        String poolSizeFromProps = PropertiesUtil.get(POOL_SIZE_KEY);
        int poolSize = poolSizeFromProps == null ? DEFAULT_POOL_SIZE : Integer.parseInt(poolSizeFromProps);
        proxyPool = new ArrayBlockingQueue<Connection>(Integer.valueOf(poolSize));
        sourceConnections = new ArrayList<Connection>(Integer.valueOf(poolSize));

        for (int i = 0; i < poolSize; i++) {
            Connection connection = open();
            Connection proxyConnection = (Connection) Proxy.newProxyInstance(ConnectionPoolManager.class.getClassLoader(),
                    new Class[]{Connection.class},
                    (proxy, method, args) ->
                            method.getName().equals("close") ? proxyPool.add((Connection) proxy) : method.invoke(connection, args));
            proxyPool.add(proxyConnection);
            sourceConnections.add(connection);
        }
    }


    private static Connection open() {
        try {
            return DriverManager.getConnection(
                    PropertiesUtil.get(URL_KEY),
                    PropertiesUtil.get(USERNAME_KEY),
                    PropertiesUtil.get(PASSWORD_KEY)
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void closePool() {
        System.out.println("Closing connection pool");
        for (Connection connection : sourceConnections) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

```

```java

package com.angubaidullin.jdbc.starter.runner;

import com.angubaidullin.jdbc.starter.util.ConnectionPoolManager;

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

```
## Connection Pool. Готовые решения. HikariCP
HikariCP — это легковесный и быстрый пул соединений, который легко интегрируется в проект. Настройки HikariCP позволяют 
гибко управлять производительностью и поведением соединений с базой данных.

Для использования HikariCP в проекте, необходимо добавить соответствующую зависимость 
и правильно настроить пул соединений.

### Подключение зависимости
```xml

<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.0.1</version> 
</dependency>

```

### Настройка пула
```java

package com.angubaidullin.jdbc.starter.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class HikariCPManager {
    private static HikariDataSource dataSource;
    private static final String URL_KEY = "db.url";
    private static final String USERNAME_KEY = "db.username";
    private static final String PASSWORD_KEY = "db.password";
    private static final String POOL_SIZE_KEY = "db.pool.size";
    private static final String CONNECTION_TIMEOUT_KEY = "db.connection.timeout";
    private static final String CONNECTION_IDLE_TIMEOUT_KEY = "db.connection.idle.timeout";
    private static final String CONNECTION_MAX_LIFETIME_KEY = "db.connection.max.lifetime";

    private static final Integer DEFAULT_POOL_SIZE = 10;
    
    private HikariCPManager() {}

    static {
        //Конфигурация пула соединений
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(PropertiesUtil.get(URL_KEY));
        config.setUsername(PropertiesUtil.get(USERNAME_KEY));
        config.setPassword(PropertiesUtil.get(PASSWORD_KEY));

        //Настройка пула
        //Максимальное количество соединений
        Integer maxPoolSize = PropertiesUtil.get(POOL_SIZE_KEY) == null ? DEFAULT_POOL_SIZE : Integer.valueOf(PropertiesUtil.get(POOL_SIZE_KEY));
        config.setMaximumPoolSize(maxPoolSize);
        //Минимальное количество соединений
        Integer minPoolSize = PropertiesUtil.get(POOL_SIZE_KEY) == null ? DEFAULT_POOL_SIZE/2 : Integer.valueOf(PropertiesUtil.get(POOL_SIZE_KEY)) / 2;
        config.setMinimumIdle(minPoolSize);
        //Максимальное время ожидания соединения в миллисекундах
        config.setConnectionTimeout(Integer.valueOf(PropertiesUtil.get(CONNECTION_TIMEOUT_KEY)));
        //Время простоя соединения перед его удалением из пула
        config.setIdleTimeout(Integer.valueOf(PropertiesUtil.get(CONNECTION_IDLE_TIMEOUT_KEY)));
        //Максимальный срок жизни соединения в пуле
        config.setMaxLifetime(Integer.valueOf(PropertiesUtil.get(CONNECTION_MAX_LIFETIME_KEY)));
        
        //Инициализация пула соединений
        dataSource = new HikariDataSource(config);
    }
    
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}

```

### Дополнительные настройки
HikariCP поддерживает множество дополнительных параметров, которые можно настроить через объект HikariConfig. 
Вот некоторые из них:
- connectionTestQuery: Запрос для проверки соединения, если автоматическое тестирование соединений 
не поддерживается драйвером.
- poolName: Уникальное имя пула соединений (полезно для логирования и мониторинга).
- leakDetectionThreshold: Установка времени в миллисекундах, после которого пул соединений может регистрировать утечку 
соединений (полезно для отладки проблем с закрытием соединений).

```java

config.setLeakDetectionThreshold(2000); // Сообщать об утечках соединений, если они не закрыты в течение 2 секунд
config.setPoolName("MyHikariPool");      // Название пула соединений

```

### Мониторинг пула соединений
HikariCP также предоставляет возможности мониторинга и ведения логов:
```java

System.out.println("Maximum Pool Size: " + dataSource.getMaximumPoolSize());
System.out.println("Active Connections: " + dataSource.getHikariPoolMXBean().getActiveConnections());
System.out.println("Idle Connections: " + dataSource.getHikariPoolMXBean().getIdleConnections());
System.out.println("Total Connections: " + dataSource.getHikariPoolMXBean().getTotalConnections());

```

### Пример использования
```java

package com.angubaidullin.jdbc.starter.runner;

import com.angubaidullin.jdbc.starter.util.HikariCPManager;

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

```