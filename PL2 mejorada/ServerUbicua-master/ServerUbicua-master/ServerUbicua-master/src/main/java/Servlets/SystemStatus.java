package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import logic.Log;
import Database.ConectionDDBB;
import mqtt.MQTTBroker;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Servlet para verificar el estado del sistema (MQTT y Base de Datos)
 */
@WebServlet("/SystemStatus")
public class SystemStatus extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    public SystemStatus() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        boolean mqttStatus = checkMQTTConnection();
        boolean dbStatus = checkDatabaseConnection();
        
        String statusJson = String.format(
            "{\"mqtt\":%b,\"database\":%b,\"overall\":%b}",
            mqttStatus, dbStatus, (mqttStatus && dbStatus)
        );
        
        out.println(statusJson);
        out.close();
    }

    private boolean checkMQTTConnection() {
        MemoryPersistence persistence = new MemoryPersistence();
        MqttClient client = null;
        try {
            client = new MqttClient(MQTTBroker.getBroker(), "StatusChecker-" + System.currentTimeMillis(), persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName(MQTTBroker.getUsername());
            connOpts.setPassword(MQTTBroker.getPassword().toCharArray());
            connOpts.setCleanSession(true);
            connOpts.setConnectionTimeout(5);
            client.connect(connOpts);
            boolean connected = client.isConnected();
            client.disconnect();
            Log.log.info("MQTT connection check: " + connected);
            return connected;
        } catch (Exception e) {
            Log.log.warn("MQTT connection check failed: " + e.getMessage());
            return false;
        } finally {
            try {
                if (client != null && client.isConnected()) {
                    client.disconnect();
                }
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    private boolean checkDatabaseConnection() {
        ConectionDDBB dbConnection = new ConectionDDBB();
        Connection conn = null;
        try {
            conn = dbConnection.obtainConnection(true);
            if (conn != null && !conn.isClosed()) {
                Log.log.info("Database connection check: OK");
                return true;
            }
            return false;
        } catch (SQLException e) {
            Log.log.warn("Database connection check failed: " + e.getMessage());
            return false;
        } catch (Exception e) {
            Log.log.warn("Database connection check failed: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                dbConnection.closeConnection(conn);
            }
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
