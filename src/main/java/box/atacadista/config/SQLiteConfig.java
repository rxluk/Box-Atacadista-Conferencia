package box.atacadista.config;

import java.io.*;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteConfig {
    private static final String DB_NAME = "users.db";
    private static final String DB_PATH = "/db/" + DB_NAME; // Caminho dentro do JAR

    public static Connection connect() {
        try {
            // Caminho no sistema de arquivos (fora do JAR)
            String externalDbPath = System.getProperty("java.io.tmpdir") + File.separator + DB_NAME;

            // Se o banco não existir no sistema de arquivos, copia do JAR
            if (!Files.exists(Paths.get(externalDbPath))) {
                try (InputStream inputStream = SQLiteConfig.class.getResourceAsStream(DB_PATH);
                     OutputStream outputStream = new FileOutputStream(externalDbPath)) {
                    if (inputStream == null) {
                        throw new RuntimeException("Arquivo do banco de dados não encontrado no JAR!");
                    }
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                }
            }

            // Retorna a conexão usando o banco de dados copiado
            return DriverManager.getConnection("jdbc:sqlite:" + externalDbPath);

        } catch (IOException | SQLException e) {
            throw new RuntimeException("Erro ao conectar com SQLite: " + e.getMessage(), e);
        }
    }
}