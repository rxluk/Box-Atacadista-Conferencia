package box.atacadista.controller;

import box.atacadista.dao.ConferenteDao;
import box.atacadista.dao.RegistroConferenciaDao;
import box.atacadista.model.Categoria;
import box.atacadista.model.Conferente;
import box.atacadista.model.RegistroConferencia;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class RegistroConferenciaController implements HttpHandler {

    private final RegistroConferenciaDao registroConferenciaDao = new RegistroConferenciaDao();
    private final ConferenteDao conferenteDao = new ConferenteDao();

    // Formato de data a ser enviado para o frontend
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if ("GET".equalsIgnoreCase(method) && path.matches("/api/registros_conferencia/\\d+")) {
            getRegistroConferenciaById(exchange);
        } else if ("GET".equalsIgnoreCase(method) && path.equals("/api/registros_conferencia")) {
            getAllRegistrosConferencia(exchange);
        } else if ("GET".equalsIgnoreCase(method) && path.matches("/api/registros_conferencia/transacao/.*")) {
            getRegistrosConferenciaByTransacao(exchange);
        } else if ("GET".equalsIgnoreCase(method) && path.matches("/api/registros_conferencia/notafiscal/.*")) {
            getRegistrosConferenciaByNotaFiscal(exchange);
        } else if ("POST".equalsIgnoreCase(method) && path.equals("/api/registros_conferencia")) {
            createRegistroConferencia(exchange);
        } else if ("PUT".equalsIgnoreCase(method) && path.matches("/api/registros_conferencia/\\d+")) {
            updateRegistroConferencia(exchange);
        } else if ("DELETE".equalsIgnoreCase(method) && path.matches("/api/registros_conferencia/\\d+")) {
            deleteRegistroConferencia(exchange);
        } else {
            sendResponse(exchange, 404, "Endpoint não encontrado");
        }
    }

    private void getRegistrosConferenciaByNotaFiscal(HttpExchange exchange) throws IOException {
        String notaFiscal = extractParametro(exchange, "notafiscal");

        if (notaFiscal.isEmpty()) {
            sendResponse(exchange, 400, "Nota fiscal não fornecida");
            return;
        }

        List<RegistroConferencia> registros = registroConferenciaDao.getRegistrosConferenciaByNotaFiscal(notaFiscal);

        if (!registros.isEmpty()) {
            String response = registros.stream()
                    .map(r -> {
                        Conferente conferente = conferenteDao.getConferenteById(r.getConferente().getId());
                        String nomeConferente = conferente != null ? conferente.getName() : "Desconhecido";
                        String dataFormatada = r.getData() != null ? r.getData().format(DATE_FORMATTER) : "";
                        return String.format("{\"id\":%d, \"transacao\":\"%s\", \"nota_fiscal\":\"%s\", \"conferente\":\"%s\", \"tipo\":\"%s\", \"data\":\"%s\"}",
                                r.getId(), r.getTransacao(), r.getNotaFiscal(), nomeConferente, r.getTipo(), dataFormatada);
                    })
                    .collect(Collectors.joining(",\n", "[\n", "\n]"));
            sendResponse(exchange, 200, response);
        } else {
            sendResponse(exchange, 404, "Nenhum registro encontrado para a nota fiscal especificada");
        }
    }

    private void getRegistrosConferenciaByTransacao(HttpExchange exchange) throws IOException {
        String transacao = extractParametro(exchange, "transacao");
        List<RegistroConferencia> registros = registroConferenciaDao.getRegistrosConferenciaByTransacao(transacao);

        if (!registros.isEmpty()) {
            String response = registros.stream()
                    .map(r -> {
                        Conferente conferente = conferenteDao.getConferenteById(r.getConferente().getId());
                        String nomeConferente = conferente != null ? conferente.getName() : "Desconhecido";
                        String dataFormatada = r.getData() != null ? r.getData().format(DATE_FORMATTER) : "";
                        return String.format("{\"id\":%d, \"transacao\":\"%s\", \"nota_fiscal\":\"%s\", \"conferente\":\"%s\", \"tipo\":\"%s\", \"data\":\"%s\"}",
                                r.getId(), r.getTransacao(), r.getNotaFiscal(), nomeConferente, r.getTipo(), dataFormatada);
                    })
                    .collect(Collectors.joining(",\n", "[\n", "\n]"));
            sendResponse(exchange, 200, response);
        } else {
            sendResponse(exchange, 404, "Nenhum registro encontrado para a transação especificada");
        }
    }

    // Método auxiliar para extrair parâmetros da URL
    private String extractParametro(HttpExchange exchange, String parametro) {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        // Assume que o parâmetro desejado está sempre na última posição da URL após o último "/"
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals(parametro)) {
                return parts[i + 1]; // Retorna o valor que vem após o nome do parâmetro
            }
        }
        return "";  // Retorna uma string vazia caso o parâmetro não seja encontrado
    }

    private void getAllRegistrosConferencia(HttpExchange exchange) throws IOException {
        List<RegistroConferencia> registros = registroConferenciaDao.getAllRegistrosConferencia();

        // Mapear os registros de conferência para incluir o nome do conferente e formatar as datas
        String response = registros.stream()
                .map(r -> {
                    // Buscar o conferente pelo ID
                    Conferente conferente = conferenteDao.getConferenteById(r.getConferente().getId());
                    String nomeConferente = conferente != null ? conferente.getName() : "Desconhecido";

                    // Convertendo a data
                    String dataFormatada = r.getData() != null ? r.getData().format(DATE_FORMATTER) : "";

                    // Retornar a string formatada com o nome do conferente e a data
                    return String.format("{\"id\":%d, \"transacao\":\"%s\", \"nota_fiscal\":\"%s\", \"conferente\":\"%s\", \"tipo\":\"%s\", \"data\":\"%s\"}",
                            r.getId(), r.getTransacao(), r.getNotaFiscal(), nomeConferente, r.getTipo(), dataFormatada);
                })
                .collect(Collectors.joining(",\n", "[\n", "\n]"));

        sendResponse(exchange, 200, response);
    }

    private void getRegistroConferenciaById(HttpExchange exchange) throws IOException {
        Long id = extractId(exchange);
        RegistroConferencia registro = registroConferenciaDao.getRegistroConferenciaById(id);
        if (registro != null) {
            // Convertendo a data
            String dataFormatada = registro.getData() != null ? registro.getData().format(DATE_FORMATTER) : "";

            // Criar resposta com todos os campos necessários
            String response = String.format("{\"id\":%d, \"transacao\":\"%s\", \"nota_fiscal\":\"%s\", \"conferente_id\":%d, \"conferente\":\"%s\", \"tipo\":\"%s\", \"data\":\"%s\"}",
                    registro.getId(), registro.getTransacao(), registro.getNotaFiscal(), registro.getConferente().getId(),
                    registro.getConferente().getName(), registro.getTipo(), dataFormatada);
            sendResponse(exchange, 200, response);
        } else {
            sendResponse(exchange, 404, "Registro de conferência não encontrado");
        }
    }

    private void createRegistroConferencia(HttpExchange exchange) throws IOException {
        Map<String, String> params = parseRequestBody(exchange);
        String transacao = params.get("transacao");
        String notaFiscal = params.get("nota_fiscal");
        String tipo = params.get("tipo");
        Long conferenteId = Long.parseLong(params.get("conferente_id"));

        if (transacao == null || notaFiscal == null || tipo == null || conferenteId == null) {
            sendResponse(exchange, 400, "Campos 'transacao', 'nota_fiscal', 'tipo' e 'conferente_id' são obrigatórios");
            return;
        }

        try {
            Categoria categoria = Categoria.valueOf(tipo);
            Conferente conferente = conferenteDao.getConferenteById(conferenteId);  // Buscar o Conferente
            if (conferente == null) {
                sendResponse(exchange, 400, "Conferente não encontrado");
                return;
            }
            RegistroConferencia registro = new RegistroConferencia(transacao, notaFiscal, conferente, categoria);
            registroConferenciaDao.insertRegistroConferencia(registro);
            sendResponse(exchange, 201, "Registro de conferência criado com sucesso");
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "Tipo inválido");
        }
    }

    private void updateRegistroConferencia(HttpExchange exchange) throws IOException {
        Long id = extractId(exchange);
        RegistroConferencia existing = registroConferenciaDao.getRegistroConferenciaById(id);

        if (existing == null) {
            sendResponse(exchange, 404, "Registro de conferência não encontrado");
            return;
        }

        Map<String, String> params = parseRequestBody(exchange);
        String transacao = params.get("transacao");
        String notaFiscal = params.get("nota_fiscal");
        String tipo = params.get("tipo");
        Long conferenteId = Long.parseLong(params.get("conferente_id"));

        if (transacao != null) existing.setTransacao(transacao);
        if (notaFiscal != null) existing.setNotaFiscal(notaFiscal);
        if (tipo != null) existing.setTipo(Categoria.valueOf(tipo));
        if (conferenteId != null) {
            Conferente conferente = conferenteDao.getConferenteById(conferenteId);
            existing.setConferente(conferente);
        }

        registroConferenciaDao.updateRegistroConferencia(existing);
        sendResponse(exchange, 200, "Registro de conferência atualizado com sucesso");
    }

    private void deleteRegistroConferencia(HttpExchange exchange) throws IOException {
        Long id = extractId(exchange);
        registroConferenciaDao.deleteRegistroConferencia(id);
        sendResponse(exchange, 200, "Registro de conferência deletado com sucesso");
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