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
            throw new ServletException(ex);
        }
    }
}
