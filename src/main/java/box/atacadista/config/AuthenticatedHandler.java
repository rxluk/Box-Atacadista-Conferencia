package box.atacadista.config;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.util.List;
import java.util.Optional;

public class AuthenticatedHandler implements HttpHandler {

    private final String protectedResource;

    // Construtor: Recebe o caminho do recurso protegido (ex.: dashboard.html)
    public AuthenticatedHandler(String protectedResource) {
        this.protectedResource = protectedResource;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();

        // Verifica se a requisição é de logout
        if ("POST".equalsIgnoreCase(requestMethod) && "/logout".equals(exchange.getRequestURI().getPath())) {
            logout(exchange);
            return;
        }

        System.out.println("🔒 Acessando recurso protegido: " + protectedResource);

        // Valida a sessão (verifica se o cookie de sessionId está presente)
        Optional<String> sessionId = getSessionIdFromCookies(exchange);

        if (sessionId.isPresent()) {
            System.out.println("✅ Sessão válida: " + sessionId.get());

            // Serve o recurso protegido
            serveProtectedResource(exchange);
        } else {
            System.out.println("⛔ Sessão inválida! Redirecionando para login.");

            // Se não houver sessão, redireciona para a página de login
            redirectToLogin(exchange);
        }
    }

    // Lê e serve o recurso protegido
    private void serveProtectedResource(HttpExchange exchange) throws IOException {
        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream("static" + protectedResource);

        if (resourceStream == null) {
            // Retorna 404 se o recurso não for encontrado
            String response = "❌ Recurso não encontrado: " + protectedResource;
            exchange.sendResponseHeaders(404, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            return;
        }

        // Define o tipo de conteúdo (Content-Type)
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, 0);

        // Transfere o conteúdo para o cliente
        try (OutputStream os = exchange.getResponseBody()) {
            resourceStream.transferTo(os);
        }

        System.out.println("✅ Recurso servido com sucesso: " + protectedResource);
    }

    // Redireciona o usuário para a página de login
    private void redirectToLogin(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Location", "/static/login.html");
        exchange.sendResponseHeaders(302, -1); // 302: Found (redirect)
        exchange.close();
    }

    // Extrai o sessionId do cookie, se presente
    private Optional<String> getSessionIdFromCookies(HttpExchange exchange) {
        List<String> cookies = exchange.getRequestHeaders().getOrDefault("Cookie", List.of());

        for (String cookie : cookies) {
            for (String pair : cookie.split(";")) {
                String[] keyValue = pair.trim().split("=");
                if (keyValue.length == 2 && "sessionId".equalsIgnoreCase(keyValue[0])) {
                    return Optional.of(keyValue[1]);
                }
            }
        }
        return Optional.empty();
    }

    // Método de logout
    public void logout(HttpExchange exchange) throws IOException {
        // Limpar o cookie de sessão
        exchange.getResponseHeaders().set("Set-Cookie", "sessionId=; Path=/; Max-Age=0; HttpOnly");

        // Resposta de sucesso
        String response = "Logout bem-sucedido.";
        exchange.sendResponseHeaders(200, response.getBytes().length); // 200 OK
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

}