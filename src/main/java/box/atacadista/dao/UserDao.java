package box.atacadista.dao;

import box.atacadista.config.SQLiteConfig;
import box.atacadista.model.User;
import box.atacadista.model.UserRole;

import java.sql.*;

public class UserDao {

    public User authenticate(String login, String password) {

        String sql = "SELECT * FROM users WHERE login = ? AND password = ?";

        try(Connection conn = SQLiteConfig.connect();
            PreparedStatement ps = conn.prepareStatement(sql))
        {

            ps.setString(1, login);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return new User(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("login"),
                        rs.getString("password"),
                        UserRole.valueOf(rs.getString("role"))
                );
            }
        } catch(SQLException e) {
                e.printStackTrace();
        }
        return null;
    }


    public void insertUser(User user) {
        String sql = "INSERT INTO users (login, name, password, role) VALUES (?, ?, ?, ?)";

        try(Connection conn = SQLiteConfig.connect();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getName());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getRole().toString());
            stmt.executeUpdate();
            System.out.println("User created");
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
}
