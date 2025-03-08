package box.atacadista.dao;

import box.atacadista.config.SQLiteConfig;
import box.atacadista.model.Categoria;
import box.atacadista.model.Conferente;
import box.atacadista.model.RegistroConferencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RegistroConferenciaDao {

    // Inserir um novo registro de conferência
    public void insertRegistroConferencia(RegistroConferencia registro) {
        String sql = "INSERT INTO registros_conferencia (transacao, nota_fiscal, conferente_id, tipo, data) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = SQLiteConfig.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, registro.getTransacao());
            stmt.setString(2, registro.getNotaFiscal());
            stmt.setLong(3, registro.getConferente().getId());
            stmt.setString(4, registro.getTipo().toString());
            stmt.setDate(5, java.sql.Date.valueOf(LocalDate.now())); // Usando a data atual diretamente
            stmt.executeUpdate();
            System.out.println("Registro de conferência criado");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Obter um registro de conferência por ID
    public RegistroConferencia getRegistroConferenciaById(Long id) {
        String sql = "SELECT * FROM registros_conferencia WHERE id = ?";

        try (Connection conn = SQLiteConfig.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Recuperando o conferente
                ConferenteDao conferenteDao = new ConferenteDao();
                Conferente conferente = conferenteDao.getConferenteById(rs.getLong("conferente_id"));
                Categoria tipo = Categoria.valueOf(rs.getString("tipo"));
                LocalDate data = rs.getDate("data").toLocalDate(); // Recuperando a data

                // Criando o objeto RegistroConferencia
                RegistroConferencia registro = new RegistroConferencia(
                        rs.getString("transacao"),
                        rs.getString("nota_fiscal"),
                        conferente,
                        tipo
                );
                registro.setData(data); // Setando a data

                return registro;  // Retornando o objeto corretamente
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Obter todos os registros de conferência
    public List<RegistroConferencia> getAllRegistrosConferencia() {
        List<RegistroConferencia> registros = new ArrayList<>();
        String sql = "SELECT * FROM registros_conferencia";

        try (Connection conn = SQLiteConfig.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Recuperando o conferente
                ConferenteDao conferenteDao = new ConferenteDao();
                Conferente conferente = conferenteDao.getConferenteById(rs.getLong("conferente_id"));
                Categoria tipo = Categoria.valueOf(rs.getString("tipo"));
                LocalDate data = rs.getDate("data").toLocalDate(); // Recuperando a data

                // Criando o objeto RegistroConferencia
                RegistroConferencia registro = new RegistroConferencia(
                        rs.getString("transacao"),
                        rs.getString("nota_fiscal"),
                        conferente,
                        tipo
                );
                registro.setData(data); // Setando a data

                // Adicionando à lista
                registros.add(registro);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return registros;
    }

    // Atualizar um registro de conferência
    public void updateRegistroConferencia(RegistroConferencia registro) {
        String sql = "UPDATE registros_conferencia SET transacao = ?, nota_fiscal = ?, conferente_id = ?, tipo = ?, data = ? WHERE id = ?";

        try (Connection conn = SQLiteConfig.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, registro.getTransacao());
            stmt.setString(2, registro.getNotaFiscal());
            stmt.setLong(3, registro.getConferente().getId());
            stmt.setString(4, registro.getTipo().toString());
            stmt.setDate(5, java.sql.Date.valueOf(registro.getData())); // Adicionando a data
            stmt.setLong(6, registro.getId());
            stmt.executeUpdate();
            System.out.println("Registro de conferência atualizado");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Deletar um registro de conferência
    public void deleteRegistroConferencia(Long id) {
        String sql = "DELETE FROM registros_conferencia WHERE id = ?";

        try (Connection conn = SQLiteConfig.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
            System.out.println("Registro de conferência deletado");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}