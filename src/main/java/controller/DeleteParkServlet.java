package controller;

import java.io.IOException;

import dao.DAOException;
import dao.ParkDbDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/deletepark")
public class DeleteParkServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ParkDbDAO parkDAO = new ParkDbDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Long id = Long.valueOf(request.getParameter("id"));
            parkDAO.delete(id);
            response.sendRedirect(request.getContextPath() + "/park");
        } catch (DAOException ex) {
            if (ex.getCause() instanceof java.sql.SQLException
                    && "23503".equals(((java.sql.SQLException) ex.getCause()).getSQLState())) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.setContentType("text/html; charset=UTF-8");
                response.getWriter().println("<p>Нельзя удалить парк, пока на него ссылается город. "
                        + "Сначала измените или удалите связанный город.</p><a href='"
                        + request.getContextPath() + "/park'>Вернуться к паркам</a>");
                return;
            }
            throw new ServletException(ex);
        } catch (NumberFormatException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Неверный код парка");
        }
    }
}
