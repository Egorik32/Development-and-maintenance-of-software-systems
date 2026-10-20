package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgreSQLConnectionBuilder implements ConnectionBuilder {
    static {
        try {
            Class.forName(ConnectionProperty.getProperty("db.driver.class"));
        } catch (ClassNotFoundException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                ConnectionProperty.getProperty("db.url"),
                ConnectionProperty.getProperty("db.login"),
                ConnectionProperty.getProperty("db.password"));
    }
}
