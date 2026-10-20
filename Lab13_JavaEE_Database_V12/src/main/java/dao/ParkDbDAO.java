package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import domain.Park;

public class ParkDbDAO {
    private final ConnectionBuilder builder = new PostgreSQLConnectionBuilder();

    public List<Park> findAll() throws DAOException {
        String sql = "SELECT id, name, type, area, has_water, build_date FROM parks ORDER BY id";
        List<Park> parks = new ArrayList<>();
        try (Connection connection = builder.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                parks.add(mapPark(resultSet));
            }
            return parks;
        } catch (SQLException ex) {
            throw new DAOException("Cannot load parks", ex);
        }
    }

    public Park findById(Long id) throws DAOException {
        String sql = "SELECT id, name, type, area, has_water, build_date FROM parks WHERE id = ?";
        try (Connection connection = builder.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapPark(resultSet) : null;
            }
        } catch (SQLException ex) {
            throw new DAOException("Cannot load park by id", ex);
        }
    }

    static Park mapPark(ResultSet resultSet) throws SQLException {
        return new Park(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("type"),
                resultSet.getBigDecimal("area"),
                resultSet.getBoolean("has_water"),
                resultSet.getDate("build_date").toLocalDate());
    }

}
