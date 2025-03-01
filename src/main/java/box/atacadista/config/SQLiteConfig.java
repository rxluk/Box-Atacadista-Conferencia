package box.atacadista.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteConfig {

    private static final String DATABASE_URL = "jdbc:sqlite:src/main/resources/db/users.db";

    public static Connection connect() {
        try {
            return DriverManager.getConnection(DATABASE_URL);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar com SQLite: " + e.getMessage());
        }
    }
}
