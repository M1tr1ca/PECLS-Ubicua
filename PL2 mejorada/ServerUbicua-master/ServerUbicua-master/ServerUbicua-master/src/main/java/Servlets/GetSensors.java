package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import Database.ConectionDDBB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.Log;

/**
 * Servlet para obtener la lista de sensores con sus calles asociadas
 * Endpoint: /GetSensors
 * 
 * Devuelve: Lista de sensores con sensor_id, sensor_type, street_id y datos de la calle
 */
@WebServlet("/GetSensors")
public class GetSensors extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Gson gson = new Gson();
    
    public GetSensors() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        Log.log.info("--Obteniendo lista de sensores--");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = response.getWriter();
        
        ConectionDDBB conector = new ConectionDDBB();
        Connection con = null;
        
        try {
            con = conector.obtainConnection(true);
            
            // Filtrar por street_id si se proporciona
            String streetId = request.getParameter("street_id");
            
            String sql;
            PreparedStatement ps;
            
            if (streetId != null && !streetId.isEmpty()) {
                // Obtener sensores de una calle específica con datos de la calle
                sql = "SELECT s.sensor_id, s.sensor_type, s.street_id, " +
                      "st.street_name, st.district, st.neighborhood " +
                      "FROM sensors s " +
                      "JOIN streets st ON s.street_id = st.street_id " +
                      "WHERE s.street_id = ? " +
                      "ORDER BY s.sensor_id";
                ps = con.prepareStatement(sql);
                ps.setString(1, streetId);
            } else {
                // Obtener todos los sensores con datos de la calle
                sql = "SELECT s.sensor_id, s.sensor_type, s.street_id, " +
                      "st.street_name, st.district, st.neighborhood " +
                      "FROM sensors s " +
                      "JOIN streets st ON s.street_id = st.street_id " +
                      "ORDER BY s.street_id, s.sensor_id";
                ps = con.prepareStatement(sql);
            }
            
            ResultSet rs = ps.executeQuery();
            
            JsonArray sensorsArray = new JsonArray();
            
            while (rs.next()) {
                JsonObject sensor = new JsonObject();
                sensor.addProperty("sensor_id", rs.getString("sensor_id"));
                sensor.addProperty("sensor_type", rs.getString("sensor_type"));
                sensor.addProperty("street_id", rs.getString("street_id"));
                sensor.addProperty("street_name", rs.getString("street_name"));
                sensor.addProperty("district", rs.getString("district"));
                sensor.addProperty("neighborhood", rs.getString("neighborhood"));
                
                sensorsArray.add(sensor);
            }
            
            out.println(gson.toJson(sensorsArray));
            Log.log.info("Devolviendo {} sensores", sensorsArray.size());
            
        } catch (Exception e) {
            out.println("{\"error\":\"Error obteniendo sensores: " + e.getMessage() + "\"}");
            Log.log.error("Error obteniendo sensores: " + e.getMessage());
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
