package servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import logic.Log;
import mqtt.MQTTBroker;
import mqtt.MQTTPublisher;

/**
 * Servlet para enviar alertas a los sensores desde aplicaciones Android
 * Endpoint: /SendAlert
 * 
 * Ejemplo de uso POST:
 * {
 *   "street_id": "ST_1617",
 *   "alert_level": 2
 * }
 */
@WebServlet("/SendAlert")
public class SendAlert extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Gson gson = new Gson();
    
    public SendAlert() {
        super();
    }

    /**
     * GET - Para pruebas rápidas desde navegador
     * Ejemplo: /SendAlert?street_id=ST_1617&alert_level=2
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = response.getWriter();
        
        try {
            String streetId = request.getParameter("street_id");
            String alertLevelStr = request.getParameter("alert_level");
            
            if (streetId == null || alertLevelStr == null) {
                out.println("{\"success\":false,\"error\":\"Parámetros requeridos: street_id, alert_level\"}");
                return;
            }
            
            int alertLevel = Integer.parseInt(alertLevelStr);
            
            if (alertLevel < 0 || alertLevel > 4) {
                out.println("{\"success\":false,\"error\":\"alert_level debe estar entre 0 y 4\"}");
                return;
            }
            
            // Enviar alerta por MQTT
            boolean success = sendAlert(streetId, alertLevel);
            
            JsonObject result = new JsonObject();
            result.addProperty("success", success);
            result.addProperty("street_id", streetId);
            result.addProperty("alert_level", alertLevel);
            result.addProperty("topic", "sensors/" + streetId + "/alerts");
            
            out.println(gson.toJson(result));
            
        } catch (NumberFormatException e) {
            out.println("{\"success\":false,\"error\":\"alert_level debe ser un número entero\"}");
            Log.log.error("Error parseando alert_level: " + e.getMessage());
        } catch (Exception e) {
            out.println("{\"success\":false,\"error\":\"Error interno del servidor\"}");
            Log.log.error("Error enviando alerta: " + e.getMessage());
        }
    }

    /**
     * POST - Para aplicaciones Android con JSON en el body
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = response.getWriter();
        
        try {
            // Leer JSON del body
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            
            String json = sb.toString();
            Log.log.info("Recibida solicitud de alerta: {}", json);
            
            JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();
            
            String streetId = jsonObj.has("street_id") ? jsonObj.get("street_id").getAsString() : null;
            Integer alertLevel = jsonObj.has("alert_level") ? jsonObj.get("alert_level").getAsInt() : null;
            
            // Validaciones
            if (streetId == null || streetId.isEmpty()) {
                out.println("{\"success\":false,\"error\":\"street_id es requerido\"}");
                return;
            }
            
            if (alertLevel == null) {
                out.println("{\"success\":false,\"error\":\"alert_level es requerido\"}");
                return;
            }
            
            if (alertLevel < 0 || alertLevel > 4) {
                out.println("{\"success\":false,\"error\":\"alert_level debe estar entre 0 y 4\"}");
                return;
            }
            
            // Enviar alerta por MQTT
            boolean success = sendAlert(streetId, alertLevel);
            
            JsonObject result = new JsonObject();
            result.addProperty("success", success);
            result.addProperty("street_id", streetId);
            result.addProperty("alert_level", alertLevel);
            result.addProperty("topic", "sensors/" + streetId + "/alerts");
            result.addProperty("message", success ? "Alerta enviada correctamente" : "Error al enviar alerta");
            
            out.println(gson.toJson(result));
            
            Log.log.info("Alerta enviada - street_id: {}, level: {}, success: {}", 
                        streetId, alertLevel, success);
            
        } catch (Exception e) {
            out.println("{\"success\":false,\"error\":\"Error procesando solicitud: " + e.getMessage() + "\"}");
            Log.log.error("Error procesando solicitud de alerta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * OPTIONS - Para CORS preflight requests
     */
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Envía una alerta por MQTT al tópico correspondiente
     */
    private boolean sendAlert(String streetId, int alertLevel) {
        try {
            String topic = "sensors/" + streetId + "/alerts";
            String message = "{\"alert_level\":" + alertLevel + "}";
            
            MQTTBroker broker = new MQTTBroker();
            MQTTPublisher.publish(broker, topic, message);
            
            Log.log.info("Alerta publicada en {}: {}", topic, message);
            return true;
            
        } catch (Exception e) {
            Log.log.error("Error publicando alerta MQTT: " + e.getMessage());
            return false;
        }
    }
}

