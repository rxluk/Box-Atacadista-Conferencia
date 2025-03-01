package box.atacadista.controller;

import box.atacadista.dao.ConferenteDao;
import box.atacadista.model.Conferente;
import box.atacadista.model.UserRole;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ConferenteController implements HttpHandler {

    private final ConferenteDao conferenteDao = new ConferenteDao();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if ("GET".equalsIgnoreCase(method) && path.matches("/api/conferentes/\\d+")) {
            getConferenteById(exchange);
        } else if ("GET".equalsIgnoreCase(method) && path.equals("/api/conferentes")) {
            getAllConferentes(exchange);
        } else if ("POST".equalsIgnoreCase(method) && path.equals("/api/conferentes")) {
            createConferente(exchange);
        } else if ("PUT".equalsIgnoreCase(method) && path.matches("/api/conferentes/\\d+")) {
            updateConferente(exchange);
        } else if ("DELETE".equalsIgnoreCase(method) && path.matches("/api/conferentes/\\d+")) {
            deleteConferente(exchange);
        } else {
            sendResponse(exchange, 404, "Endpoint não encontrado");
        }
    }

    private void getAllConferentes(HttpExchange exchange) throws IOException {
        List<Conferente> conferentes = conferenteDao.getAllConferentes();
        String response = new StringBuilder("[\n")
                .append(conferentes.stream()
                        .map(c -> String.format("{\"id\":%d, \"name\":\"%s\", \"role\":\"%s\"}",
                                c.getId(), c.getName(), c.getRole()))
                        .reduce((c1, c2) -> c1 + ",\n" + c2).orElse(""))
                .append("\n]").toString();
        sendResponse(exchange, 200, response);
    }

    private void getConferenteById(HttpExchange exchange) throws IOException {
        Long id = extractId(exchange);
        Conferente conferente = conferenteDao.getConferenteById(id);
        if (conferente != null) {
            String response = String.format("{\"id\":%d, \"name\":\"%s\", \"role\":\"%s\"}",
                    conferente.getId(), conferente.getName(), conferente.getRole());
            sendResponse(exchange, 200, response);
        } else {
            sendResponse(exchange, 404, "Conferente não encontrado");
        }
    }

    private void createConferente(HttpExchange exchange) throws IOException {
        Map<String, String> params = parseRequestBody(exchange);
        String name = params.get("name");
        String role = params.get("role");

        if (name == null || role == null) {
            sendResponse(exchange, 400, "Campos 'name' e 'role' são obrigatórios");
            return;
        }

        try {
            Conferente conferente = new Conferente(null, name, UserRole.valueOf(role));
            conferenteDao.insertConferente(conferente);
            sendResponse(exchange, 201, "Conferente criado com sucesso");
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "Role inválida");
        }
    }

    private void updateConferente(HttpExchange exchange) throws IOException {
        Long id = extractId(exchange);
        Conferente existing = conferenteDao.getConferenteById(id);

        if (existing == null) {
            sendResponse(exchange, 404, "Conferente não encontrado");
            return;
        }

        Map<String, String> params = parseRequestBody(exchange);
        String name = params.get("name");
        String role = params.get("role");

        if (name != null) existing.setName(name);
        if (role != null) existing.setRole(UserRole.valueOf(role));

        conferenteDao.updateConferente(existing);
        sendResponse(exchange, 200, "Conferente atualizado com sucesso");
    }

    private void deleteConferente(HttpExchange exchange) throws IOException {
        Long id = extractId(exchange);
        conferenteDao.deleteConferente(id);
        sendResponse(exchange, 200, "Conferente deletado com sucesso");
    }

    private Long extractId(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        return Long.parseLong(path.substring(path.lastIndexOf('/') + 1));
    }

    private Map<String, String> parseRequestBody(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            body.append(line);
        }

        Map<String, String> params = new HashMap<>();
        for (String param : body.toString().split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2) {
                params.put(pair[0], pair[1]);
            }
        }
        return params;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}