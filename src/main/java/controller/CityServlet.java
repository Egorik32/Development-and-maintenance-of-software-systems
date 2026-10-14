package controller;

import java.io.IOException;

import dao.CityDbDAO;
import dao.DAOException;
import dao.ParkDbDAO;
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
    private final CityDbDAO cityDAO = new CityDbDAO();
    private final ParkDbDAO parkDAO = new ParkDbDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("cities", cityDAO.findAll());
            request.setAttribute("parks", parkDAO.findAll());
            request.getRequestDispatcher("/city.jsp").forward(request, response);
        } catch (DAOException ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        City city = readCity(request);
        try {
            cityDAO.insert(city);
            response.sendRedirect(request.getContextPath() + "/city");
        } catch (DAOException ex) {
            throw new ServletException(ex);
        }
    }

    static City readCity(HttpServletRequest request) {
        Park park = new Park();
        park.setId(Long.valueOf(request.getParameter("parkId")));
        return new City(
                request.getParameter("name"),
                request.getParameter("status"),
                Integer.valueOf(request.getParameter("population")),
                park);
    }
}
