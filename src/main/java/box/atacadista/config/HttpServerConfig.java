package box.atacadista.config;

import box.atacadista.controller.ConferenteController;
import box.atacadista.controller.RegistroConferenciaController;
import box.atacadista.controller.UserController;
import com.sun.net.httpserver.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpServerConfig {

    public static void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Rota de login (gera sessão)
        server.createContext("/login", new LoginHandler());

        // Rota de conferente
        server.createContext("/api/conferentes", new ConferenteController());

        //
        server.createContext("/api/registros_conferencia", new RegistroConferenciaController());

        // Rota do dashboard (protegida pela sessão)
        server.createContext("/static/dashboard.html", new AuthenticatedHandler("/dashboard.html"));

        // Rota de logout (chama o método de logout)
        server.createContext("/logout", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                new AuthenticatedHandler("/").logout(exchange);
            }
        }); // Aqui adicionamos a rota /logout

        // Outras rotas estáticas públicas
        server.createContext("/static", new StaticFileHandler());

        server.start();
        System.out.println("🚀 Servidor rodando na porta 8080...");
    }

    // Handler de Login: Valida credenciais e cria a sessão
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("🔍 Requisição recebida no /login: " + exchange.getRequestMethod());

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                InputStream requestBody = exchange.getRequestBody();
                String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);

                Map<String, String> params = parseFormData(body);
                String username = params.get("username");
                String password = params.get("password");

                System.out.println("🔑 Username: " + username + ", Password: " + password);

                UserController userController = new UserController();
                boolean authenticated = userController.authenticate(username, password);
                System.out.println("✅ Autenticado? " + authenticated);

                if (authenticated) {
                    // Cria a sessão e envia o cookie
                    String sessionId = UUID.randomUUID().toString();
                    exchange.getResponseHeaders().add("Set-Cookie", "sessionId=" + sessionId + "; HttpOnly; Path=/");

                    String response = "Login bem-sucedido!";
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }

                    System.out.println("✅ Sessão criada: " + sessionId);
                } else {
                    String response = "Usuário ou senha inválidos!";
                    exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                    exchange.sendResponseHeaders(400, response.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    System.out.println("❌ Falha na autenticação.");
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Método não permitido
            }
            exchange.close();
        }
    }

    // Utilitário: Parseia o corpo do formulário (application/x-www-form-urlencoded)
    private static Map<String, String> parseFormData(String body) {
        Map<String, String> params = new HashMap<>();
        for (String param : body.split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2) {
                try {
                    String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                    params.put(key, value);
                } catch (IllegalArgumentException e) {
                    System.err.println("Erro ao decodificar: " + e.getMessage());
                }
            }
        }
        return params;
    }
}