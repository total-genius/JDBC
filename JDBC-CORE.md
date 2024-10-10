# JDBC Core

## Настройка и подключение

### Используем Докер для БД:
#### docker run
Запустим базу данных в Докер:
```
docker run 
--name some-postgres 
-e POSTGRES_PASSWORD=123 
-e POSTGRES_DB=mydatabase 
-e POSTGRES_USER=myuser 
-p 5432:5432 
-v путь_на_вашей_машине:/var/lib/postgresql/data 
-d postgres
```

Чтобы зайти внутрь контейнера:
`docker exec -it container_name_or_id bash`

Чтобы зайти в БД внутри контейнера:
`psql -U username -d DB_name`

#### docker-compose
Создадим файл `docker-compose.yml`:
```yaml

version: '3'
services:
  postgres:
    image: postgres:14
    container_name: postgres-db
    environment:
      POSTGRES_USER: myuser
      POSTGRES_PASSWORD: 123
      POSTGRES_DB: mydatabase
    ports:
      - "5433:5432"
    volumes:
      - C:\Users\user\Desktop\postgres-db-container\postgres-data:/var/lib/postgresql/data

  pgadmin:
    image: dpage/pgadmin4
    container_name: pgadmin
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@admin.com
      PGADMIN_DEFAULT_PASSWORD: 123
    ports:
      - "8080:80"
    depends_on:
      - postgres

volumes:
  postgres-data:

```

1. Теперь, находясь в директории с файлом `docker-compose.yml`, выполним команду:
`docker-compose up -d`
2. Остановка контейнеров: `docker-compose down`
3. Перезапуск контейнеров: `docker-compose restart`



### Добавление зависимостей:
Добавим зависимости для работы с базой данных (JDBC драйвер для определенной БД):
```xml

<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.3</version>
</dependency>


```
### Подключение к базе данных из Java приложения
Подключимся в БД из приложения Java:
```java

        String url = "jdbc:postgresql://localhost:5433/mydatabase";
        String username = "myuser";
        String password = "123";
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println(connection.getTransactionIsolation());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

```

Вынесем все это в утилитный класс:
```java

package com.angubaidullin.jdbc.starter.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionManager {

    private static final String URL = "jdbc:postgresql://localhost:5433/mydatabase";
    private static final String USERNAME = "myuser";
    private static final String PASSWORD = "123";

    static {
        loadDriver();
    }

    private static void loadDriver() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private ConnectionManager(){}

    public static Connection open() {
        try {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

```

Вынесем пароль, юзернейм и url в отдельный файл `application.properties`:
```
db.url=jdbc:postgresql://localhost:5433/mydatabase
db.username=myuser
db.password= 123

```

Создадим утилитный класс, который будет читать этот файл, возьмет оттуда все свойства и будет возвращать их по ключу:
```java

package com.angubaidullin.jdbc.starter.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PropertiesUtil {
    private PropertiesUtil() {}

    /*
    Для представляния свойств из файла есть специальный класс
     */
    private static final Properties PROPERTIES = new Properties();

    static {
        loadProps();
    }

    //Создадим публичный метод, который будет возвращать значение по ключу
    public static String get(String key) {
        return PROPERTIES.getProperty(key);
    }

    private static void loadProps() {
        try (InputStream resourceAsStream = PropertiesUtil.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            PROPERTIES.load(resourceAsStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

```

Внесем изменения в ConnectionManager:
```java

package com.angubaidullin.jdbc.starter.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionManager {

    private static final String URL_KEY = "db.url";
    private static final String USERNAME_KEY = "db.username";
    private static final String PASSWORD_KEY = "db.password";

    static {
        loadDriver();
    }

    private static void loadDriver() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private ConnectionManager() {
    }

    public static Connection open() {
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
}

```

Протестируем:
```java

package com.angubaidullin.jdbc.starter;

import com.angubaidullin.jdbc.starter.util.ConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;

public class JDBCRunner {
    public static void main(String[] args) throws SQLException {
        try (Connection connection = ConnectionManager.open()) {
            System.out.println(connection.getMetaData().getURL());
        }

    }
}

```

---
 
##  Statement

### DDL операции

Создадим таблицу БД c помощью `Statement`:
```java

package com.angubaidullin.jdbc.starter;

import com.angubaidullin.jdbc.starter.util.ConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCRunner {
    public static void main(String[] args) throws SQLException {

        String sql = """
                CREATE TABLE IF NOT EXISTS student (
                                 id SERIAL PRIMARY KEY,
                                 firstname VARCHAR(50) NOT NULL,
                                 lastname VARCHAR(50) NOT NULL,
                                 course VARCHAR(100),
                                 age INT
                             );
                """;

        try (Connection connection = ConnectionManager.open();
        Statement statement = connection.createStatement()) {

            System.out.println(connection.getMetaData().getURL());

            statement.execute(sql);

        }
    }
}


```

### DML операции
```java

package com.angubaidullin.jdbc.starter;

import com.angubaidullin.jdbc.starter.util.ConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCRunner {
    public static void main(String[] args) throws SQLException {

        String sql = """
                INSERT INTO student (firstname, lastname, course, age)
                VALUES 
                        ('Maria', 'Shwedova', 'Programming', 29),
                        ('Anton', 'Shastun', 'Phylosophy', 22)
                """;

        try (Connection connection = ConnectionManager.open();
        Statement statement = connection.createStatement()) {

            System.out.println(connection.getMetaData().getURL());

            statement.execute(sql);
            System.out.println(statement.getUpdateCount());

        }
    }
}

```

Можно отправить сразу несколько операций:

```java

String sql = """
                INSERT INTO student (firstname, lastname, course, age)
                VALUES 
                        ('Joe', 'Doria', 'Mathematic', 22),
                        ('Josef', 'Stalin', 'Politic', 20);
                INSERT INTO student (firstname, lastname, course, age)
                VALUES 
                        ('Michael', 'Shumacher', 'Racing', 23),
                        ('John', 'Sina', 'Wrestling', 22);
                """;

```

Чаще используют не `execute(String sql);`, а `executeUpdate(String sql);`

Обновление записи в бд:
```java

String sqlUpdate = """
                UPDATE student
                SET firstname = 'Maxim'
                WHERE id = 11
                """;

```

Удаление записи в бд:
```java

String sqlDelete = """
                DELETE FROM student
                WHERE id = 7
                """;

```

---

### ResultSet. Операция SELECT
```java
package com.angubaidullin.jdbc.starter;

import com.angubaidullin.jdbc.starter.util.ConnectionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCRunner {
    public static void main(String[] args) throws SQLException {

        String sqlSelect = """
                SELECT * FROM student
                """;

        try (Connection connection = ConnectionManager.open();
        Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(sqlSelect);
            while (resultSet.next()) {
                System.out.println(resultSet.getLong("id"));
                System.out.println(resultSet.getString("firstname"));
                System.out.println(resultSet.getString("lastname"));
            }

        }
    }
}

```

#### ResultSet. Generated keys
Чаще всего при работе с сущностями в качестве первичного ключа мы используем автогенерируемые значения.
Как можно получить автоматически сгененрированное значение сущности, которую мы только что вставили в таблицу,
без дополнительных запросов в базу данных?

```java

package com.angubaidullin.jdbc.starter;

import com.angubaidullin.jdbc.starter.util.ConnectionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCRunner {
    public static void main(String[] args) throws SQLException {

        String sqlInsert = """
                INSERT INTO student (firstname, lastname, course, age)
                VALUES 
                        ('Alex', 'Arnold', 'Sport', 23);
                """;
        
        try (Connection connection = ConnectionManager.open();
        Statement statement = connection.createStatement()) {
            
            int i = statement.executeUpdate(sqlInsert, Statement.RETURN_GENERATED_KEYS);
            ResultSet generatedKeys = statement.getGeneratedKeys();

            while (generatedKeys.next()) {
                int id = generatedKeys.getInt("id");
                System.out.println(id);
            }
        }
    }
}

```
Это можно сделать с помощью метода `ResultSet generatedKeys = statement.getGeneratedKeys();`, который возвращает 
множество автосгенерированных значений.
- Также необходимо в методе `statement.executeUpdate(sqlInsert);` установить второй параметр 
`statement.executeUpdate(sqlInsert, Statement.RETURN_GENERATED_KEYS);`.
- `Statement.RETURN_GENERATED_KEYS` - данный флаг, указывает, что после вставки значений в таблицу нужно вернуть их
автоматически сгененрированные значения. По умолчанию этот флаг имеет значение `Statement.NO_GENERATED_KEYS`, указывающий,
что сгененрированные значения возвращать не нужно.

---

### SQL injection
SQL инъекция - один из видов атак на приложение, где в запрос передается SQL выражание. Обычный Statement не защищен от
SQL инъекций.

**Пример кода, не защищенного от SQL инъекций:**

```java

package com.angubaidullin.jdbc.starter;

import com.angubaidullin.jdbc.starter.util.ConnectionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JDBCRunner {
    public static void main(String[] args) throws SQLException {

        String age = "23";
        List<String> studentsByAge = getStudentsByAge(age);
        studentsByAge.forEach(System.out::println);

    }

    //Получаем студентов по их возрасту
    private static List<String> getStudentsByAge(String age) {
        String sqlGetByAge = """
                SELECT firstname
                FROM student
                WHERE age = %s
                """.formatted(age);

        List<String> studentFirstnames = new ArrayList<>();

        try (Connection connection = ConnectionManager.open();
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(sqlGetByAge);
            while (resultSet.next()) {
                 studentFirstnames.add(resultSet.getString("firstname"));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return studentFirstnames;
    }
}

```

Вместо `String age = "23";` можно передать, например, `String age = "23 OR 1 = 1";`. Таким образом, мы получаем
все данные из таблицы:

```java

package com.angubaidullin.jdbc.starter;

import com.angubaidullin.jdbc.starter.util.ConnectionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JDBCRunner {
    public static void main(String[] args) throws SQLException {

        String age = "23 OR 1 = 1";
        List<String> studentsByAge = getStudentsByAge(age);
        studentsByAge.forEach(System.out::println);

    }

    //Получаем студентов по их возрасту
    private static List<String> getStudentsByAge(String age) {
        String sqlGetByAge = """
                SELECT firstname
                FROM student
                WHERE age = %s
                """.formatted(age);

        List<String> studentFirstnames = new ArrayList<>();

        try (Connection connection = ConnectionManager.open();
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(sqlGetByAge);
            while (resultSet.next()) {
                 studentFirstnames.add(resultSet.getString("firstname"));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return studentFirstnames;
    }
}

```

С помощью SQL инъекций можно нанести существенный вред базе данных. Например, если передать 
`String age = "23 OR 1=1; DROP TABLE student;` можно удать таблицу в базе данных.

Поэтому на практике стоит исользовать **PreparedStatement** для создания запросов в базу данных.

---

## PreparedStatement
PreparedStatement позволяет нам создавать параметризированные запросы. 

**Пример №1:**
```java

package com.angubaidullin.jdbc.starter;

import com.angubaidullin.jdbc.starter.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JDBCRunner {
    public static void main(String[] args) throws SQLException {

        String age = "23";
        List<String> studentsByAge = getStudentsByAge(age);
        studentsByAge.forEach(System.out::println);

    }

    //Получаем студентов по их возрасту
    private static List<String> getStudentsByAge(String age) {
        //Создаем запрос. На место куда должно поступить занчение из вне
        //(В нашем случае это возраст, по которому будет происходить выборка)
        //Мы ставим "?"
        String sqlGetByAge = """
                SELECT firstname
                FROM students
                WHERE age = ?
                """;

        List<String> studentFirstnames = new ArrayList<>();

        try (Connection connection = ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(sqlGetByAge)) {

            //Перед вызовом выполнения запроса мы вставляем в параметры запрос (вместо "?") нужное значение
            //Первый параметр порядок знака "?" (от 1 до n)
            //Второй параметр - значение, которое нужно подставить
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

```

В отличии от обычного Statement, в PreparedStatement нужно передать запрос сразу при его создании 
`PreparedStatement statement = connection.prepareStatement(String sql_statement)`


**Пример №2:**
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





