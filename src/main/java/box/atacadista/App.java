package box.atacadista;

import box.atacadista.config.HttpServerConfig;

import java.io.IOException;

public class App {
    public static void main(String[] args) {
        try {
            // Chamando o método startServer para inicializar o servidor HTTP
            HttpServerConfig.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}