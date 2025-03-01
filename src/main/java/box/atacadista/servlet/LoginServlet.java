package box.atacadista.servlet;

import box.atacadista.controller.UserController;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class LoginServlet extends HttpServlet {

    private UserController userController = new UserController();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Pegando os parâmetros enviados pelo formulário
        String login = request.getParameter("username");
        String password = request.getParameter("password");

        // Chamando o método de autenticação
        boolean authenticated = userController.authenticate(login, password);

        if (authenticated) {
            // Caso o login seja bem-sucedido, redireciona para o dashboard
            response.sendRedirect("/static/dashboard.html");
        } else {
            // Caso o login falhe, redireciona de volta para a página de login com uma mensagem de erro
            request.setAttribute("errorMessage", "Usuário ou senha inválidos");
            request.getRequestDispatcher("/static/login.html").forward(request, response);
        }
    }
}