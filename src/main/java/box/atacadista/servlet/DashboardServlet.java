package box.atacadista.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Exibindo a página do dashboard
        request.getRequestDispatcher("/static/dashboard.html").forward(request, response);
    }
}