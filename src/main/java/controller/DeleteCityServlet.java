package controller;

import java.io.IOException;

import dao.CityDbDAO;
import dao.DAOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/deletecity")
public class DeleteCityServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final CityDbDAO cityDAO = new CityDbDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Long id = Long.valueOf(request.getParameter("id"));
            cityDAO.delete(id);
            response.sendRedirect(request.getContextPath() + "/city");
        } catch (DAOException ex) {
            throw new ServletException(ex);
        }
    }
}
