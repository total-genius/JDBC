package com.angubaidullin.jdbc.dao_starter.dao;

import com.angubaidullin.jdbc.dao_starter.entity.Airplane;
import com.angubaidullin.jdbc.dao_starter.exception.DaoException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AirplaneDao {

    private static final AirplaneDao INSTANCE = new AirplaneDao();
    //Запрос на удаление
    private static final String DELETE_SQL = """
            DELETE FROM airplane
            WHERE id = ?
            """;
    //Запрос на сохранение
    private static final String SAVE_SQL = """
            INSERT INTO airplane (model)
            VALUES (?)
            """;
    //Запрос на обновление
    private static final String UPDATE_SQL = """
            UPDATE airplane
            SET model=? 
            WHERE id=?
            """;
    //Запрос на выборку
    private static final String FIND_BY_ID = """
            SELECT id, model
            FROM airplane
            WHERE id = ?
            """;
    private static final String FIND_ALL = """
            SELECT id, model
            FROM airplane
            """;

    private AirplaneDao() {
    }

    public static AirplaneDao getInstance() {
        return INSTANCE;
    }

    //Deleting method
    public boolean delete(Long id, Connection connection) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_SQL)) {

            preparedStatement.setLong(1, id);
            int result = preparedStatement.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    //Save airplane
    public Airplane save(Airplane airplane, Connection connection) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, airplane.getModel());
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                airplane.setId(resultSet.getLong(1));
            }
            return airplane;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
    //update airplane
    public void update(Airplane airplane, Connection connection) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
            if (airplane.getId() != null) {
                preparedStatement.setLong(1, airplane.getId());
                preparedStatement.setString(2, airplane.getModel());
                preparedStatement.executeUpdate();
            } else {
                save(airplane, connection);
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
    //select by id
    public Optional<Airplane> findById(Long id, Connection connection) {
        Airplane airplane = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID)) {
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                airplane = buildAirplane(resultSet);
            }
            return Optional.ofNullable(airplane);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
    //Find all
    public List<Airplane> findAll(Connection connection) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Airplane> airplanes = new ArrayList<>();
            while (resultSet.next()) {
                airplanes.add(buildAirplane(resultSet));
            }
            return airplanes;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    private static Airplane buildAirplane(ResultSet resultSet) throws SQLException {
        return new Airplane(
                resultSet.getLong("id"),
                resultSet.getString("model")
        );
    }


}
