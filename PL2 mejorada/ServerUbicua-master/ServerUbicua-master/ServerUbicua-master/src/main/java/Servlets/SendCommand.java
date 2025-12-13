package servlets;

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
 * Servlet para enviar comandos MQTT al ESP32
 * Parámetros:
 *   - street_id: ID de la calle (ej: ST_0871) - REQUERIDO
 *   - alert_level: Nivel de alerta (0-4)
 *   - command: Comando a enviar (ej: restart)
 */
@WebServlet("/SendCommand")
public class SendCommand extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_STREET_ID = "ST_1617";
    
    public SendCommand() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = response.getWriter();
        
        try {
            // Obtener street_id del parámetro, usar default si no se proporciona
            String streetId = request.getParameter("street_id");
            if (streetId == null || streetId.isEmpty()) {
                streetId = DEFAULT_STREET_ID;
            }
            
            // Construir el topic dinámicamente
            String topic = "sensors/" + streetId + "/alerts";
            
            String alertLevel = request.getParameter("alert_level");
            String command = request.getParameter("command");
            
            String jsonMessage = "";
            
            if (alertLevel != null) {
                int level = Integer.parseInt(alertLevel);
                if (level >= 0 && level <= 4) {
                    jsonMessage = "{\"alert_level\":" + level + "}";
                    Log.log.info("Sending alert level: " + level + " to street: " + streetId);
                } else {
                    out.println("{\"success\":false,\"error\":\"Invalid alert level\"}");
                    return;
                }
            } else if (command != null) {
                jsonMessage = "{\"command\":\"" + command + "\"}";
                Log.log.info("Sending command: " + command + " to street: " + streetId);
            } else {
                out.println("{\"success\":false,\"error\":\"No command specified\"}");
                return;
            }
            
            MQTTPublisher.publish(new MQTTBroker(), topic, jsonMessage);
            Log.log.info("Message sent to topic " + topic + ": " + jsonMessage);
            out.println("{\"success\":true,\"topic\":\"" + topic + "\",\"message\":\"" + jsonMessage.replace("\"", "\\\"") + "\"}");            
        } catch (NumberFormatException nfe) {
            out.println("{\"success\":false,\"error\":\"Invalid number format\"}");
            Log.log.error("Number Format Exception: " + nfe);
        } catch (Exception e) {
            out.println("{\"success\":false,\"error\":\"" + e.getMessage() + "\"}");
            Log.log.error("Exception: " + e);
        } finally {
            out.close();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
