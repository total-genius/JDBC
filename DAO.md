# DAO

---
## Entity Mapping
**Пример представления данных из таблицы в виде объекта Java:**
```java

package com.angubaidullin.jdbc.dao_starter.entity;

import java.math.BigDecimal;
import java.util.Objects;

public class Ticket {
    private Long id;
    private String passengerName;
    private BigDecimal price;
    private Long flightId;

    public Ticket() {
    }

    public Ticket(Long id, String passengerName, BigDecimal price, Long flightId) {
        this.id = id;
        this.passengerName = passengerName;
        this.price = price;
        this.flightId = flightId;
    }

    public Ticket(String passengerName, BigDecimal price, Long flightId) {
        this.passengerName = passengerName;
        this.price = price;
        this.flightId = flightId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getFlightId() {
        return flightId;
    }

    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket simpleTicket = (Ticket) o;
        return Objects.equals(id, simpleTicket.id) && Objects.equals(passengerName, simpleTicket.passengerName) && Objects.equals(price, simpleTicket.price) && Objects.equals(flightId, simpleTicket.flightId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, passengerName, price, flightId);
    }

    @Override
    public String toString() {
        return "SimpleTicket{" +
                "id=" + id +
                ", passengerName='" + passengerName + '\'' +
                ", price=" + price +
                ", flightId=" + flightId +
                '}';
    }
}

```
---

## DAO Delete, Insert
**Создание TicketDao:**

```java
package com.angubaidullin.jdbc.dao_starter.dao;

import com.angubaidullin.jdbc.dao_starter.entity.SimpleTicket;
import com.angubaidullin.jdbc.dao_starter.entity.Ticket;
import com.angubaidullin.jdbc.dao_starter.exception.DaoException;
import com.angubaidullin.jdbc.util.HikariCPManager;

import java.sql.*;

public class TicketDao {

    private static final TicketDao INSTANCE = new TicketDao();
    //Запрос на удаление
    private static final String DELETE_SQL = """
            DELETE FROM simpleTicket
            WHERE id = ?
            """;
    //Запрос на сохранение
    private static final String SAVE_SQL = """
            INSERT INTO simpleTicket (passenger_name, price, flight_id)
            VALUES (?, ?, ?)
            """;

    private TicketDao() {
    }

    public static TicketDao getInstance() {
        return INSTANCE;
    }

    //Удаление сущности
    public boolean delete(Long id) {
        try (Connection connection = HikariCPManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {

            statement.setLong(1, id);
            int i = statement.executeUpdate();

            return i > 0;

        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    //Сохранение сущности
    public SimpleTicket save(SimpleTicket simpleTicket) {
        try (Connection connection = HikariCPManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, simpleTicket.getPassengerName());
            preparedStatement.setBigDecimal(2, simpleTicket.getPrice());
            preparedStatement.setLong(3, simpleTicket.getFlightId());

            preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();

            if (generatedKeys.next()) {
                simpleTicket.setId(generatedKeys.getLong("id"));
            }

            return simpleTicket;

        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

}

```
---

## DAO Update, Select
**Дополняем TicketDao новыми методами:**
```java

    //Запрос на обновление
    private static final String UPDATE_SQL = """
            UPDATE simpleTicket
            SET passenger_name=?, 
                price=?, 
                flight_id=?
            WHERE id=?
            """;
    //Запрос на выборку
    private static final String FIND_BY_ID = """
            SELECT id, passenger_name, price, flight_id
            FROM simpleTicket
            WHERE id = ?
            """;
    private static final String FIND_ALL = """
            SELECT id, passenger_name, price, flight_id
            FROM simpleTicket
            """;

    //Обновление
    public void update(Ticket simpleTicket) {
        try (Connection connection = HikariCPManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_SQL)) {

            if (simpleTicket.getId() != null) {
                preparedStatement.setString(1, simpleTicket.getPassengerName());
                preparedStatement.setBigDecimal(2, simpleTicket.getPrice());
                preparedStatement.setLong(3, simpleTicket.getFlightId());
                preparedStatement.setLong(4, simpleTicket.getId());

                preparedStatement.executeUpdate();

            } else {
                save(simpleTicket);
            }


        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    //Выборка по id
    public Optional<Ticket> findById(Long id) {
        Ticket simpleTicket = null;
        try (Connection connection = HikariCPManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID)){

            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                simpleTicket = buildTicket(resultSet);
            }

            return Optional.ofNullable(simpleTicket);

        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    //Получение всех билетов
    public List<Ticket> findAll() {
        try (Connection connection = HikariCPManager.getConnection();
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(FIND_ALL);
            List<Ticket> simpleTickets = new ArrayList<>();
            while (resultSet.next()) {
               simpleTickets.add(buildTicket(resultSet));
            }
            return simpleTickets;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    private static Ticket buildTicket(ResultSet resultSet) throws SQLException {
        return new Ticket(
                resultSet.getLong("id"),
                resultSet.getString("passenger_name"),
                resultSet.getBigDecimal("price"),
                resultSet.getLong("flight_id"));
    }

```
---

## DAO Select с фильтрацией

```java

package com.angubaidullin.jdbc.dao_starter.dto;

import java.math.BigDecimal;

public record TicketFilter(
        String passengerName,
        BigDecimal price,
        Long flightId) {

}
```

```java

//Поиск с фильтрацией
    public List<Ticket> findAll(TicketFilter filter) {

        List<Object> params = new ArrayList<>();
        String condition = null;
        List<String> whereParams = new ArrayList<>();
        if (filter.price() != null || filter.flightId() != null || filter.passengerName() != null) {
            if (filter.passengerName() != null) {
                params.add(filter.passengerName());
                whereParams.add("passenger_name=?");
            }
            if (filter.price() != null) {
                params.add(filter.price());
                whereParams.add("price=?");
            }
            if (filter.flightId() != null) {
                params.add(filter.flightId());
                whereParams.add("flight_id=?");
            }

            condition = whereParams.stream().collect(Collectors.joining(" AND ", " WHERE ", ""));
        } else {
            condition = "";
        }

        String sql = FIND_ALL + condition;


        try (Connection connection = HikariCPManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }

            List<Ticket> simpleTickets = new ArrayList<>();
            System.out.println(preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                simpleTickets.add(buildTicket(resultSet));
            }
            return simpleTickets;

        } catch (SQLException e) {
            throw new DaoException(e);
        }

    }

    private static Ticket buildTicket(ResultSet resultSet) throws SQLException {
        return new Ticket(
                resultSet.getLong("id"),
                resultSet.getString("passenger_name"),
                resultSet.getBigDecimal("price"),
                resultSet.getLong("flight_id"));
    }

```
