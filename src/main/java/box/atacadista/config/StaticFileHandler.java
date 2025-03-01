package box.atacadista.config;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;

public class StaticFileHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestURI = exchange.getRequestURI().getPath();
        System.out.println("📄 Requisição de arquivo: " + requestURI);

        // Remove o prefixo "/static" para acessar corretamente os arquivos em resources/static
        String resourcePath = "static" + requestURI.replaceFirst("/static", "");
        System.out.println("📂 Buscando recurso: " + resourcePath);

        // Lê o arquivo a partir do classpath
        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(resourcePath);

        if (resourceStream == null) {
            // Arquivo não encontrado
            String response = "❌ File not found: " + resourcePath;
            exchange.sendResponseHeaders(404, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            System.out.println(response);
            return;
        }

        // Define o tipo de conteúdo (Content-Type)
        String contentType = URLConnection.guessContentTypeFromName(resourcePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(200, 0);

        // Transfere o conteúdo do arquivo para a resposta
        try (OutputStream os = exchange.getResponseBody()) {
            resourceStream.transferTo(os);
        }

        System.out.println("✅ Arquivo servido com sucesso: " + resourcePath);
    }
}