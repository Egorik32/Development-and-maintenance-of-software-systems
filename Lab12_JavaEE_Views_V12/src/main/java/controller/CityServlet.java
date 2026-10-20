package controller;

import java.io.IOException;

import domain.City;
import domain.Park;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/city")
public class CityServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Park gorky = new Park(1L, "Парк имени Горького", "городской", new java.math.BigDecimal("19.50"), true, java.time.LocalDate.of(1928, 8, 12));
        Park revolution = new Park(2L, "Парк Революции", "культурный", new java.math.BigDecimal("17.00"), false, java.time.LocalDate.of(1926, 5, 1));
        Park olympic = new Park(3L, "Олимпийский парк", "спортивный", new java.math.BigDecimal("200.00"), true, java.time.LocalDate.of(2014, 2, 7));
        request.setAttribute("parks", java.util.Arrays.asList(gorky, revolution, olympic));
        request.setAttribute("cities", java.util.Arrays.asList(
            new domain.City(1L, "Ростов-на-Дону", "областной центр", 1142162, gorky),
            new domain.City(2L, "Шахты", "город областного значения", 226452, revolution),
            new domain.City(3L, "Сочи", "курортный город", 466078, olympic)));
        request.getRequestDispatcher("/city.jsp").forward(request, response);
    }
}
