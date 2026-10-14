package controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

import dao.DAOException;
import dao.ParkDbDAO;
import domain.Park;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/park")
public class ParkServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ParkDbDAO parkDAO = new ParkDbDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("parks", parkDAO.findAll());
            request.getRequestDispatcher("/park.jsp").forward(request, response);
        } catch (DAOException ex) {
            throw new ServletException(ex);
        }
    }

}
