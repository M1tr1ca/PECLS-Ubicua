package servlets;

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
        // 1. Intentamos obtener los parámetros de la forma estándar (Compatible con tu JQuery)
        String topic = request.getParameter("topic");
        String message = request.getParameter("message");

        // 2. Si son nulos, podría ser que alguien envió JSON RAW (Body). 
        // Solo ENTONCES intentamos leer el body manualmente.
        if (topic == null && message == null) {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String body = sb.toString();
            
            // Aquí iría tu lógica de parseo manual si realmente quieres soportar JSON raw
            // Pero para tu código actual, esto es secundario.
            if (body != null && body.contains("\"topic\"")) {
                // ... tu lógica de extracción manual ...
                // Nota: Recomiendo usar una librería como Gson o Jackson aquí en el futuro
                // en lugar de substring, que es propenso a errores.
                
                // Intento rápido de rescate para JSON manual (simplificado de tu código anterior)
                 int topicStart = body.indexOf("\"topic\"");
                 if (topicStart != -1) {
                     int colon = body.indexOf(":", topicStart);
                     int q1 = body.indexOf("\"", colon);
                     int q2 = body.indexOf("\"", q1 + 1);
                     if (q1 != -1 && q2 != -1) topic = body.substring(q1 + 1, q2);
                 }
                 int msgStart = body.indexOf("\"message\"");
                 if (msgStart != -1) {
                     int colon = body.indexOf(":", msgStart);
                     // Asumimos que el mensaje termina antes del cierre }
                     String temp = body.substring(colon + 1).trim();
                     if (temp.startsWith("\"")) {
                         int q2 = temp.indexOf("\"", 1);
                         message = temp.substring(1, q2);
                     } else {
                         // Es un objeto json, simplificación burda:
                         message = temp.substring(0, temp.lastIndexOf("}"));
                     }
                 }
            }
        }
        
        // 3. Validaciones
        if (topic == null || topic.trim().isEmpty()) {
            out.println("{\"success\":false,\"error\":\"Topic is required\"}");
            return;
        }
        
        if (message == null || message.trim().isEmpty()) {
            out.println("{\"success\":false,\"error\":\"Message is required\"}");
            return;
        }
        
        // 4. Publicar
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
