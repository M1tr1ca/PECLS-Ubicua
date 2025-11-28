package mqtt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import Database.ConectionDDBB;
import logic.Log;

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
        
        // Ignorar mensajes de control (alertas y comandos)
        if (topic.endsWith("/control")) {
            Log.logmqtt.info("Mensaje de control ignorado (no se guarda en BD)");
            return;
        }
        
        ConectionDDBB db = new ConectionDDBB();
        Connection con = null;
        
        try {
            // Parsear el JSON
            JsonObject json = JsonParser.parseString(message.toString()).getAsJsonObject();
            
            // Verificar si es un mensaje de control (por si llega por otro topic)
            if (json.has("alert_level") || json.has("command")) {
                Log.logmqtt.info("Mensaje de control ignorado (no se guarda en BD)");
                return;
            }
            
            // Verificar que tenga los campos necesarios de datos de sensor
            if (!json.has("sensor_id") || !json.has("timestamp") || !json.has("data")) {
                Log.logmqtt.warn("Mensaje ignorado: no tiene formato de datos de sensor");
                return;
            }
            
            String sensorId = json.get("sensor_id").getAsString();
            String timestampStr = json.get("timestamp").getAsString();
            
            // Parsear timestamp (formato: "2025-11-14 10:23:45:127")
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
            java.util.Date parsedDate = sdf.parse(timestampStr);
            Timestamp timestamp = new Timestamp(parsedDate.getTime());
            
            // Detectar tipo de sensor
            String sensorType = json.has("sensor_type") ? json.get("sensor_type").getAsString() : "weather";
            JsonObject data = json.getAsJsonObject("data");
            
            con = db.obtainConnection(true);
            
            switch (sensorType) {
                case "traffic_counter":
                    saveTrafficCounter(con, sensorId, timestamp, data);
                    break;
                case "traffic_light":
                    saveTrafficLight(con, sensorId, timestamp, data);
                    break;
                case "information_display":
                    saveInformationDisplay(con, sensorId, timestamp, data);
                    break;
                case "weather":
                    // Sensor meteorológico (weather) - comportamiento original
                    saveWeatherSensor(con, sensorId, timestamp, data, json);
                    break;
                default:
                    saveOther(con, sensorId, json.get("timestamp").getAsString(), json);
                    break;
            }
            
        } catch (Exception e) {
            
            try {
                JsonObject json = JsonParser.parseString(message.toString()).getAsJsonObject();
                Log.logmqtt.error("Error guardando en BD: {}", e.getMessage(), " guardando en tabla Other");
                saveOther(con, json.get("sensor_id").getAsString(), json.get("timestamp").getAsString(), JsonParser.parseString(message.toString()).getAsJsonObject());

            } catch (Exception i) {
                Log.logmqtt.error("Error guardando en BD: {}", e.getMessage());
                i.printStackTrace();
            }
            
        } finally {
            if (con != null) {
                db.closeConnection(con);
            }
        }
    }
    
    private void saveWeatherSensor(Connection con, String sensorId, Timestamp timestamp, JsonObject data, JsonObject json) throws Exception {
        double temperature = data.get("temperature_celsius").getAsDouble();
        double humidity = data.get("humidity_percent").getAsDouble();
        double pressure = data.get("atmospheric_pressure_hpa").getAsDouble();
        
        JsonObject location = json.getAsJsonObject("location");
        double altitude = location.get("altitude_meters").getAsDouble();
        
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
        
        Log.logmqtt.info("Datos weather guardados: sensor={}, temp={}, humidity={}", sensorId, temperature, humidity);
    }
    private void saveOther(Connection con, String sensorId, String timestamp, JsonObject json) throws Exception{
        String sql ="INSERT INTO other (sensor_id, timestamp, json) VALUES (?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);

        ps.setString(1, sensorId);
        ps.setString(2, timestamp);
        ps.setString(3, json.toString());

        ps.executeUpdate();
        ps.close();
        Log.logmqtt.info("Datos other guardados: json={}", json.toString());

    }
    
    private void saveTrafficCounter(Connection con, String sensorId, Timestamp timestamp, JsonObject data) throws Exception {
        String sql = "INSERT INTO traffic_counter_readings (sensor_id, timestamp, vehicle_count, pedestrian_count, bicycle_count, direction, counter_type, technology, average_speed_kmh, occupancy_percentage, traffic_density) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        
        ps.setString(1, sensorId);
        ps.setTimestamp(2, timestamp);
        ps.setInt(3, getIntOrDefault(data, "vehicle_count", 0));
        ps.setInt(4, getIntOrDefault(data, "pedestrian_count", 0));
        ps.setInt(5, getIntOrDefault(data, "bicycle_count", 0));
        ps.setString(6, getStringOrDefault(data, "direction", ""));
        ps.setString(7, getStringOrDefault(data, "counter_type", ""));
        ps.setString(8, getStringOrDefault(data, "technology", ""));
        ps.setDouble(9, getDoubleOrDefault(data, "average_speed_kmh", 0.0));
        ps.setDouble(10, getDoubleOrDefault(data, "occupancy_percentage", 0.0));
        ps.setString(11, getStringOrDefault(data, "traffic_density", ""));
        
        ps.executeUpdate();
        ps.close();
        
        Log.logmqtt.info("Datos traffic_counter guardados: sensor={}, vehicles={}, pedestrians={}", 
            sensorId, getIntOrDefault(data, "vehicle_count", 0), getIntOrDefault(data, "pedestrian_count", 0));
    }
    
    private void saveTrafficLight(Connection con, String sensorId, Timestamp timestamp, JsonObject data) throws Exception {
        String sql = "INSERT INTO traffic_light_readings (sensor_id, timestamp, current_state, cycle_position_seconds, time_remaining_seconds, cycle_duration_seconds, traffic_light_type, circulation_direction, pedestrian_waiting, pedestrian_button_pressed, malfunction_detected, cycle_count, state_changed, last_state_change) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        
        ps.setString(1, sensorId);
        ps.setTimestamp(2, timestamp);
        ps.setString(3, getStringOrDefault(data, "current_state", ""));
        ps.setInt(4, getIntOrDefault(data, "cycle_position_seconds", 0));
        ps.setInt(5, getIntOrDefault(data, "time_remaining_seconds", 0));
        ps.setInt(6, getIntOrDefault(data, "cycle_duration_seconds", 0));
        ps.setString(7, getStringOrDefault(data, "traffic_light_type", ""));
        ps.setString(8, getStringOrDefault(data, "circulation_direction", ""));
        ps.setBoolean(9, getBooleanOrDefault(data, "pedestrian_waiting", false));
        ps.setBoolean(10, getBooleanOrDefault(data, "pedestrian_button_pressed", false));
        ps.setBoolean(11, getBooleanOrDefault(data, "malfunction_detected", false));
        ps.setInt(12, getIntOrDefault(data, "cycle_count", 0));
        ps.setBoolean(13, getBooleanOrDefault(data, "state_changed", false));
        
        // Parsear last_state_change (formato ISO: "2025-09-18T09:50:39.549745")
        Timestamp lastStateChange = null;
        if (data.has("last_state_change") && !data.get("last_state_change").isJsonNull()) {
            try {
                String isoDate = data.get("last_state_change").getAsString();
                OffsetDateTime odt = OffsetDateTime.parse(isoDate + "Z", DateTimeFormatter.ISO_DATE_TIME);
                lastStateChange = Timestamp.from(odt.toInstant());
            } catch (Exception e) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
                    java.util.Date parsedDate = sdf.parse(data.get("last_state_change").getAsString());
                    lastStateChange = new Timestamp(parsedDate.getTime());
                } catch (Exception e2) {
                    Log.logmqtt.warn("No se pudo parsear last_state_change: {}", e2.getMessage());
                }
            }
        }
        ps.setTimestamp(14, lastStateChange);
        
        ps.executeUpdate();
        ps.close();
        
        Log.logmqtt.info("Datos traffic_light guardados: sensor={}, state={}, remaining={}s", 
            sensorId, getStringOrDefault(data, "current_state", ""), getIntOrDefault(data, "time_remaining_seconds", 0));
    }
    
    private void saveInformationDisplay(Connection con, String sensorId, Timestamp timestamp, JsonObject data) throws Exception {
        String sql = "INSERT INTO information_display_readings (sensor_id, timestamp, display_status, current_message, content_type, brightness_level, display_type, display_size_inches, supports_color, temperature_celsius, energy_consumption_watts, last_content_update) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        
        ps.setString(1, sensorId);
        ps.setTimestamp(2, timestamp);
        ps.setString(3, getStringOrDefault(data, "display_status", ""));
        ps.setString(4, getStringOrDefault(data, "current_message", ""));
        ps.setString(5, getStringOrDefault(data, "content_type", ""));
        ps.setInt(6, getIntOrDefault(data, "brightness_level", 0));
        ps.setString(7, getStringOrDefault(data, "display_type", ""));
        ps.setDouble(8, getDoubleOrDefault(data, "display_size_inches", 0.0));
        ps.setBoolean(9, getBooleanOrDefault(data, "supports_color", false));
        ps.setDouble(10, getDoubleOrDefault(data, "temperature_celsius", 0.0));
        ps.setDouble(11, getDoubleOrDefault(data, "energy_consumption_watts", 0.0));
        
        // Parsear last_content_update (formato ISO)
        Timestamp lastContentUpdate = null;
        if (data.has("last_content_update") && !data.get("last_content_update").isJsonNull()) {
            try {
                String isoDate = data.get("last_content_update").getAsString();
                OffsetDateTime odt = OffsetDateTime.parse(isoDate + "Z", DateTimeFormatter.ISO_DATE_TIME);
                lastContentUpdate = Timestamp.from(odt.toInstant());
            } catch (Exception e) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
                    java.util.Date parsedDate = sdf.parse(data.get("last_content_update").getAsString());
                    lastContentUpdate = new Timestamp(parsedDate.getTime());
                } catch (Exception e2) {
                    Log.logmqtt.warn("No se pudo parsear last_content_update: {}", e2.getMessage());
                }
            }
        }
        ps.setTimestamp(12, lastContentUpdate);
        
        ps.executeUpdate();
        ps.close();
        
        Log.logmqtt.info("Datos information_display guardados: sensor={}, status={}, message={}", 
            sensorId, getStringOrDefault(data, "display_status", ""), getStringOrDefault(data, "current_message", ""));
    }
    
    // Helper methods
    private int getIntOrDefault(JsonObject obj, String key, int defaultValue) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsInt() : defaultValue;
    }
    
    private double getDoubleOrDefault(JsonObject obj, String key, double defaultValue) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsDouble() : defaultValue;
    }
    
    private String getStringOrDefault(JsonObject obj, String key, String defaultValue) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : defaultValue;
    }
    
    private boolean getBooleanOrDefault(JsonObject obj, String key, boolean defaultValue) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsBoolean() : defaultValue;
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}
