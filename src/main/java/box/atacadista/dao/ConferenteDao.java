package box.atacadista.dao;

import box.atacadista.config.SQLiteConfig;
import box.atacadista.model.Conferente;
import box.atacadista.model.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConferenteDao {

    public void insertConferente(Conferente conferente) {
        String sql = "INSERT INTO conferentes (name, role) VALUES (?, ?)";

        try (Connection conn = SQLiteConfig.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, conferente.getName());
            stmt.setString(2, conferente.getRole().toString());
            stmt.executeUpdate();
            System.out.println("Conferente criado");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Conferente getConferenteById(Long id) {
        String sql = "SELECT * FROM conferentes WHERE id = ?";

        try (Connection conn = SQLiteConfig.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Conferente(
                        rs.getLong("id"),
                        rs.getString("name"),
                        UserRole.valueOf(rs.getString("role"))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Conferente> getAllConferentes() {
        List<Conferente> conferentes = new ArrayList<>();
        String sql = "SELECT * FROM conferentes";

        try (Connection conn = SQLiteConfig.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                conferentes.add(new Conferente(
                        rs.getLong("id"),
                        rs.getString("name"),
                        UserRole.valueOf(rs.getString("role"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conferentes;
    }

    public void updateConferente(Conferente conferente) {
        String sql = "UPDATE conferentes SET name = ?, role = ? WHERE id = ?";

        try (Connection conn = SQLiteConfig.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, conferente.getName());
            stmt.setString(2, conferente.getRole().toString());
            stmt.setLong(3, conferente.getId());
            stmt.executeUpdate();
            System.out.println("Conferente atualizado");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteConferente(Long id) {
        String sql = "DELETE FROM conferentes WHERE id = ?";

        try (Connection conn = SQLiteConfig.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
            System.out.println("Conferente deletado");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}