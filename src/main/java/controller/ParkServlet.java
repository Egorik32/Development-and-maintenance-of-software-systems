package controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

import domain.Park;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/park")
public class ParkServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Park gorky = new Park(1L, "Парк имени Горького", "городской", new java.math.BigDecimal("19.50"), true, java.time.LocalDate.of(1928, 8, 12));
        Park revolution = new Park(2L, "Парк Революции", "культурный", new java.math.BigDecimal("17.00"), false, java.time.LocalDate.of(1926, 5, 1));
        Park olympic = new Park(3L, "Олимпийский парк", "спортивный", new java.math.BigDecimal("200.00"), true, java.time.LocalDate.of(2014, 2, 7));
        request.setAttribute("parks", java.util.Arrays.asList(gorky, revolution, olympic));
        request.getRequestDispatcher("/park.jsp").forward(request, response);
    }
}
