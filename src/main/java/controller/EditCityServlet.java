package controller;

import java.io.IOException;

import dao.CityDbDAO;
import dao.DAOException;
import dao.ParkDbDAO;
import domain.City;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/editcity")
public class EditCityServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final CityDbDAO cityDAO = new CityDbDAO();
    private final ParkDbDAO parkDAO = new ParkDbDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Long id = Long.valueOf(request.getParameter("id"));
            request.setAttribute("cities", cityDAO.findAll());
            request.setAttribute("parks", parkDAO.findAll());
            request.setAttribute("cityEdit", cityDAO.findById(id));
            request.getRequestDispatcher("/editcity.jsp").forward(request, response);
        } catch (DAOException ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        City city = CityServlet.readCity(request);
        city.setId(Long.valueOf(request.getParameter("id")));
        try {
            cityDAO.update(city);
            response.sendRedirect(request.getContextPath() + "/city");
        } catch (DAOException ex) {
            throw new ServletException(ex);
        }
    }
}
