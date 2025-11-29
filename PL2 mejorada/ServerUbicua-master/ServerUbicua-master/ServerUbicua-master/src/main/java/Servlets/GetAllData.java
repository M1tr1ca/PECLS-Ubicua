package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.InformationDisplayMeasurement;
import logic.Log;
import logic.Logic;
import logic.Measurement;
import logic.TrafficCounterMeasurement;
import logic.TrafficLightMeasurement;

/**
 * Servlet que devuelve datos de todos los tipos de sensores
 */
@WebServlet("/GetAllData")
public class GetAllData extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public GetAllData() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Log.log.info("--Get all sensor data from DB--");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try 
        {
            Gson gson = new Gson();
            JsonObject result = new JsonObject();
            
            // Obtener datos de sensores meteorológicos
            ArrayList<Measurement> weatherData = Logic.getDataFromDB();
            result.add("weather", gson.toJsonTree(weatherData));
            
            // Obtener datos de contadores de tráfico
            ArrayList<TrafficCounterMeasurement> trafficCounterData = Logic.getTrafficCounterDataFromDB();
            result.add("trafficCounter", gson.toJsonTree(trafficCounterData));
            
            // Obtener datos de semáforos
            ArrayList<TrafficLightMeasurement> trafficLightData = Logic.getTrafficLightDataFromDB();
            result.add("trafficLight", gson.toJsonTree(trafficLightData));
            
            // Obtener datos de pantallas de información
            ArrayList<InformationDisplayMeasurement> displayData = Logic.getInformationDisplayDataFromDB();
            result.add("informationDisplay", gson.toJsonTree(displayData));

            //Obtener datos de otros
            ArrayList<String> rawList = Logic.getOtherFromDB();
            JsonArray cleanArray = new JsonArray();

            for (String linea : rawList) {
                // Buscamos dónde empieza la llave '{' y cogemos todo desde ahí
                int inicio = linea.indexOf("{");
                if (inicio != -1) {
                    // Cortamos el texto sucio del principio
                    String jsonPuro = linea.substring(inicio);
                    // Convertimos el texto a objeto JSON real
                    cleanArray.add(JsonParser.parseString(jsonPuro));
                }
            }
            // Añadimos la lista limpia al resultado
            result.add("Other", cleanArray);
        
            
            Log.log.info("All data retrieved successfully");
            out.println(result.toString());
            
        } catch (Exception e) 
        {
            out.println("{\"error\": \"" + e.getMessage() + "\"}");
            Log.log.error("Exception: " + e);
        } finally 
        {
            out.close();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
