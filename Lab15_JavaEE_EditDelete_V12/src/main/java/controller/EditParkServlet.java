package controller;

import java.io.IOException;

import dao.DAOException;
import dao.ParkDbDAO;
import domain.Park;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/editpark")
public class EditParkServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ParkDbDAO parkDAO = new ParkDbDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Long id = Long.valueOf(request.getParameter("id"));
            request.setAttribute("parks", parkDAO.findAll());
            request.setAttribute("parkEdit", parkDAO.findById(id));
            request.getRequestDispatcher("/editpark.jsp").forward(request, response);
        } catch (DAOException ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Park park = ParkServlet.readPark(request);
        park.setId(Long.valueOf(request.getParameter("id")));
        try {
            parkDAO.update(park);
            response.sendRedirect(request.getContextPath() + "/park");
        } catch (DAOException ex) {
            throw new ServletException(ex);
        }
    }
}
