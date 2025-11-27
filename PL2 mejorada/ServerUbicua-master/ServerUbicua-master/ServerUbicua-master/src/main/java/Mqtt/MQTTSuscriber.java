package mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import Database.ConectionDDBB;
import logic.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class MQTTSuscriber implements MqttCallback {

    private MqttClient client;
    private String brokerUrl;
    private String clientId;
    private String username;
    private String password;

    public MQTTSuscriber(MQTTBroker broker) {
        this.brokerUrl = broker.getBroker();
        this.clientId = broker.getClientId();
        this.username = broker.getUsername();
        this.password = broker.getPassword();
    }

    public void subscribeTopic(String topic) {
        try {
            MemoryPersistence persistence = new MemoryPersistence();
            client = new MqttClient(brokerUrl, MQTTBroker.getSubscriberClientId(), persistence);

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName(username);
            connOpts.setPassword(password.toCharArray());
            connOpts.setCleanSession(false); // Para mantener la suscripción
            connOpts.setAutomaticReconnect(true); // Reconexión automática
            connOpts.setConnectionTimeout(10);

            client.setCallback(this);
            client.connect(connOpts);

            client.subscribe(topic, 1); // QoS 1 para asegurarse de recibir
            Log.logmqtt.info("Subscribed to {}", topic);

        } catch (MqttException e) {
            Log.logmqtt.error("Error subscribing to topic: {}", e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.logmqtt.warn("MQTT Connection lost, cause: {}", cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        Log.logmqtt.info("{}: {}", topic, message.toString());
        
        ConectionDDBB db = new ConectionDDBB();
        Connection con = null;
        
        try {
            // Parsear el JSON
            JsonObject json = JsonParser.parseString(message.toString()).getAsJsonObject();
            
            String sensorId = json.get("sensor_id").getAsString();
            String timestampStr = json.get("timestamp").getAsString();
            
            JsonObject data = json.getAsJsonObject("data");
            double temperature = data.get("temperature_celsius").getAsDouble();
            double humidity = data.get("humidity_percent").getAsDouble();
            double pressure = data.get("atmospheric_pressure_hpa").getAsDouble();
            
            JsonObject location = json.getAsJsonObject("location");
            double altitude = location.get("altitude_meters").getAsDouble();
            
            // Parsear timestamp (formato: "2025-11-14 10:23:45:127")
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
            java.util.Date parsedDate = sdf.parse(timestampStr);
            Timestamp timestamp = new Timestamp(parsedDate.getTime());
            
            // Insertar en la base de datos
            con = db.obtainConnection(true);
            String sql = "INSERT INTO sensor_readings (sensor_id, timestamp, temperature_celsius, humidity_percent, atmospheric_pressure_hpa, altitude_meters) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            
            ps.setString(1, sensorId);
            ps.setTimestamp(2, timestamp);
            ps.setDouble(3, temperature);
            ps.setDouble(4, humidity);
            ps.setDouble(5, pressure);
            ps.setDouble(6, altitude);
            
            ps.executeUpdate();
            ps.close();
            
            Log.logmqtt.info("Datos guardados: sensor={}, temp={}, humidity={}", sensorId, temperature, humidity);
            
        } catch (Exception e) {
            Log.logmqtt.error("Error guardando en BD: {}", e.getMessage());
        } finally {
            if (con != null) {
                db.closeConnection(con);
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}
