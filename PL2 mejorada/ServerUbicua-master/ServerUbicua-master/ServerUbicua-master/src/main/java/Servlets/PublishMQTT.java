package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import logic.Log;
import mqtt.MQTTBroker;
import mqtt.MQTTPublisher;

/**
 * Servlet para publicar mensajes JSON personalizados a cualquier topic MQTT
 */
@WebServlet("/PublishMQTT")
public class PublishMQTT extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    public PublishMQTT() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            String topic = request.getParameter("topic");
            String message = request.getParameter("message");
            
            if (topic == null || topic.trim().isEmpty()) {
                out.println("{\"success\":false,\"error\":\"Topic is required\"}");
                return;
            }
            
            if (message == null || message.trim().isEmpty()) {
                out.println("{\"success\":false,\"error\":\"Message is required\"}");
                return;
            }
            
            // Publicar el mensaje al topic especificado
            MQTTPublisher.publish(new MQTTBroker(), topic, message);
            Log.log.info("Custom message published to topic '" + topic + "': " + message);
            
            out.println("{\"success\":true,\"topic\":\"" + escapeJson(topic) + "\",\"message\":" + message + "}");
            
        } catch (Exception e) {
            out.println("{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            Log.log.error("Error publishing MQTT message: " + e);
        } finally {
            out.close();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // Intentar leer JSON del body primero
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            
            String body = sb.toString();
            String topic = null;
            String message = null;
            
            if (body != null && !body.trim().isEmpty() && body.contains("\"topic\"")) {
                // Parsear JSON manualmente (básico)
                // Formato esperado: {"topic":"xxx","message":{...}}
                int topicStart = body.indexOf("\"topic\"");
                if (topicStart != -1) {
                    int colonPos = body.indexOf(":", topicStart);
                    int quoteStart = body.indexOf("\"", colonPos);
                    int quoteEnd = body.indexOf("\"", quoteStart + 1);
                    if (quoteStart != -1 && quoteEnd != -1) {
                        topic = body.substring(quoteStart + 1, quoteEnd);
                    }
                }
                
                int messageStart = body.indexOf("\"message\"");
                if (messageStart != -1) {
                    int colonPos = body.indexOf(":", messageStart);
                    // El mensaje puede ser un objeto JSON o un string
                    String remaining = body.substring(colonPos + 1).trim();
                    if (remaining.startsWith("{")) {
                        // Es un objeto JSON, encontrar el cierre
                        int braceCount = 0;
                        int endPos = 0;
                        for (int i = 0; i < remaining.length(); i++) {
                            char c = remaining.charAt(i);
                            if (c == '{') braceCount++;
                            else if (c == '}') {
                                braceCount--;
                                if (braceCount == 0) {
                                    endPos = i + 1;
                                    break;
                                }
                            }
                        }
                        message = remaining.substring(0, endPos);
                    } else if (remaining.startsWith("\"")) {
                        // Es un string
                        int quoteEnd = remaining.indexOf("\"", 1);
                        message = remaining.substring(1, quoteEnd);
                    }
                }
            } else {
                // Usar parámetros tradicionales
                topic = request.getParameter("topic");
                message = request.getParameter("message");
            }
            
            if (topic == null || topic.trim().isEmpty()) {
                out.println("{\"success\":false,\"error\":\"Topic is required\"}");
                return;
            }
            
            if (message == null || message.trim().isEmpty()) {
                out.println("{\"success\":false,\"error\":\"Message is required\"}");
                return;
            }
            
            // Publicar el mensaje al topic especificado
            MQTTPublisher.publish(new MQTTBroker(), topic, message);
            Log.log.info("Custom message published to topic '" + topic + "': " + message);
            
            out.println("{\"success\":true,\"topic\":\"" + escapeJson(topic) + "\",\"sentMessage\":" + message + "}");
            
        } catch (Exception e) {
            out.println("{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            Log.log.error("Error publishing MQTT message: " + e);
        } finally {
            out.close();
        }
    }
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
