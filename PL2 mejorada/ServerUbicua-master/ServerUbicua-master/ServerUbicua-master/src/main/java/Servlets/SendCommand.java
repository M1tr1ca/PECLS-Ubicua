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
 */
@WebServlet("/SendCommand")
public class SendCommand extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String CONTROL_TOPIC = "sensors/ST_1617/alerts";
    
    public SendCommand() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            String alertLevel = request.getParameter("alert_level");
            String command = request.getParameter("command");
            
            String jsonMessage = "";
            
            if (alertLevel != null && !alertLevel.isEmpty()) {
                int level = Integer.parseInt(alertLevel);
                if (level >= 0 && level <= 4) {
                    jsonMessage = "{\"alert_level\":" + level + "}";
                    Log.log.info("Sending alert level: " + level);
                } else {
                    out.println("{\"success\":false,\"error\":\"Invalid alert level\"}");
                    return;
                }
            } else if (command != null && !command.isEmpty()) {
                jsonMessage = "{\"command\":\"" + command + "\"}";
                Log.log.info("Sending command: " + command);
            } else {
                out.println("{\"success\":false,\"error\":\"No command specified\"}");
                return;
            }
            
            MQTTPublisher.publish(new MQTTBroker(), CONTROL_TOPIC, jsonMessage);
            Log.log.info("Message sent to topic " + CONTROL_TOPIC + ": " + jsonMessage);
            out.println("{\"success\":true,\"message\":\"" + jsonMessage + "\"}");
            
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
