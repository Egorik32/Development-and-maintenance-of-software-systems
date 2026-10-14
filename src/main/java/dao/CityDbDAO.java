package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import domain.City;
import domain.Park;

public class CityDbDAO {
    private final ConnectionBuilder builder = new PostgreSQLConnectionBuilder();

    public List<City> findAll() throws DAOException {
        String sql = """
                SELECT c.id, c.name, c.status, c.population,
                       p.id AS park_id, p.name AS park_name, p.type, p.area, p.has_water, p.build_date
                FROM cities c
                JOIN parks p ON p.id = c.park_id
                ORDER BY c.id
                """;
        List<City> cities = new ArrayList<>();
        try (Connection connection = builder.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                cities.add(mapCity(resultSet));
            }
            return cities;
        } catch (SQLException ex) {
            throw new DAOException("Cannot load cities", ex);
        }
    }

    public City findById(Long id) throws DAOException {
        String sql = """
                SELECT c.id, c.name, c.status, c.population,
                       p.id AS park_id, p.name AS park_name, p.type, p.area, p.has_water, p.build_date
                FROM cities c
                JOIN parks p ON p.id = c.park_id
                WHERE c.id = ?
                """;
        try (Connection connection = builder.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapCity(resultSet) : null;
            }
        } catch (SQLException ex) {
            throw new DAOException("Cannot load city by id", ex);
        }
    }

    private static City mapCity(ResultSet resultSet) throws SQLException {
        Park park = new Park(
                resultSet.getLong("park_id"),
                resultSet.getString("park_name"),
                resultSet.getString("type"),
                resultSet.getBigDecimal("area"),
                resultSet.getBoolean("has_water"),
                resultSet.getDate("build_date").toLocalDate());
        return new City(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("status"),
                resultSet.getInt("population"),
                park);
    }

}
