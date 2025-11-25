package servlets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import Database.ConectionDDBB;
import logic.Log;

/**
 * Servlet para obtener la lista de calles disponibles
 * Endpoint: /GetStreets
 */
@WebServlet("/GetStreets")
public class GetStreets extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Gson gson = new Gson();
    
    public GetStreets() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        Log.log.info("--Obteniendo lista de calles--");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = response.getWriter();
        
        ConectionDDBB conector = new ConectionDDBB();
        Connection con = null;
        
        try {
            con = conector.obtainConnection(true);
            
            // Filtrar por distrito si se proporciona
            String district = request.getParameter("district");
            
            String sql;
            PreparedStatement ps;
            
            if (district != null && !district.isEmpty()) {
                sql = "SELECT * FROM streets WHERE district = ? ORDER BY street_name";
                ps = con.prepareStatement(sql);
                ps.setString(1, district);
            } else {
                sql = "SELECT * FROM streets ORDER BY street_name";
                ps = con.prepareStatement(sql);
            }
            
            ResultSet rs = ps.executeQuery();
            
            JsonArray streetsArray = new JsonArray();
            
            while (rs.next()) {
                JsonObject street = new JsonObject();
                street.addProperty("street_id", rs.getString("street_id"));
                street.addProperty("street_name", rs.getString("street_name"));
                street.addProperty("district", rs.getString("district"));
                street.addProperty("neighborhood", rs.getString("neighborhood"));
                
                JsonObject location = new JsonObject();
                location.addProperty("latitude_start", rs.getDouble("latitude_start"));
                location.addProperty("latitude_end", rs.getDouble("latitude_end"));
                location.addProperty("longitude_start", rs.getDouble("longitude_start"));
                location.addProperty("longitude_end", rs.getDouble("longitude_end"));
                street.add("location", location);
                
                streetsArray.add(street);
            }
            
            out.println(gson.toJson(streetsArray));
            Log.log.info("Devolviendo {} calles", streetsArray.size());
            
        } catch (Exception e) {
            out.println("{\"error\":\"Error obteniendo calles: " + e.getMessage() + "\"}");
            Log.log.error("Error obteniendo calles: " + e.getMessage());
        } finally {
            conector.closeConnection(con);
            out.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}

