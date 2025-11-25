package mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import logic.Log;
import logic.Location;
import logic.SensorData;
import logic.SensorLogic;
import logic.SensorReading;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Suscriptor MQTT que recibe mensajes de los sensores ESP32
 * y los almacena en la base de datos PostgreSQL
 */
public class MQTTSuscriber implements MqttCallback {

    private static final Gson gson = new GsonBuilder().create();
    private MqttClient client;

    public void suscribeTopic(MQTTBroker broker, String topic) {
        Log.logmqtt.debug("Suscribiéndose a tópicos");
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            client = new MqttClient(MQTTBroker.getBroker(), MQTTBroker.getClientId() + "_sub", persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName(MQTTBroker.getUsername());
            connOpts.setPassword(MQTTBroker.getPassword().toCharArray());
            connOpts.setCleanSession(true);
            connOpts.setAutomaticReconnect(true);
            connOpts.setConnectionTimeout(30);
            connOpts.setKeepAliveInterval(60);
            
            Log.logmqtt.debug("MQTT Conectando al broker: " + MQTTBroker.getBroker());
            client.connect(connOpts);
            Log.logmqtt.debug("MQTT Conectado");
            client.setCallback(this);

            // Suscribirse al tópico con wildcard para recibir de todos los sensores
            client.subscribe(topic);
            Log.logmqtt.info("Suscrito a {}", topic);

        } catch (MqttException me) {
            Log.logmqtt.error("Error suscribiéndose al tópico: {}", me.getMessage());
        } catch (Exception e) {
            Log.logmqtt.error("Error suscribiéndose al tópico: {}", e.getMessage());
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.logmqtt.warn("Conexión MQTT perdida: {}", cause.getMessage());
        Log.logmqtt.info("Intentando reconectar...");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = message.toString();
        Log.logmqtt.info("Mensaje recibido en {}: {}", topic, payload);

        try {
            // Parsear el JSON del mensaje
            SensorReading reading = parseMessage(payload);
            
            if (reading != null) {
                // Guardar en la base de datos
                boolean saved = SensorLogic.saveSensorReading(reading);
                
                if (saved) {
                    Log.logmqtt.info("Lectura guardada exitosamente para sensor: {}", reading.getSensorId());
                    
                    // Aquí se podría implementar lógica para enviar alertas
                    checkAndSendAlerts(reading);
                } else {
                    Log.logmqtt.error("Error guardando lectura del sensor: {}", reading.getSensorId());
                }
            }
        } catch (Exception e) {
            Log.logmqtt.error("Error procesando mensaje MQTT: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.logmqtt.debug("Entrega completada: {}", token.getMessageId());
    }

    /**
     * Parsea el mensaje JSON recibido del sensor
     */
    private SensorReading parseMessage(String json) {
        try {
            JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();
            
            SensorReading reading = new SensorReading();
            
            // Campos obligatorios
            reading.setSensorId(jsonObj.has("sensor_id") ? jsonObj.get("sensor_id").getAsString() : null);
            reading.setSensorType(jsonObj.has("sensor_type") ? jsonObj.get("sensor_type").getAsString() : "weather");
            reading.setStreetId(jsonObj.has("street_id") ? jsonObj.get("street_id").getAsString() : null);
            
            // Parsear timestamp
            if (jsonObj.has("timestamp")) {
                String timestampStr = jsonObj.get("timestamp").getAsString();
                reading.setTimestamp(parseTimestamp(timestampStr));
            } else {
                reading.setTimestamp(new Timestamp(System.currentTimeMillis()));
            }
            
            // Parsear ubicación
            if (jsonObj.has("location")) {
                JsonObject locObj = jsonObj.getAsJsonObject("location");
                Location location = gson.fromJson(locObj, Location.class);
                reading.setLocation(location);
            }
            
            // Parsear datos del sensor
            if (jsonObj.has("data")) {
                JsonObject dataObj = jsonObj.getAsJsonObject("data");
                SensorData data = gson.fromJson(dataObj, SensorData.class);
                reading.setData(data);
            }
            
            // Validar campos obligatorios
            if (reading.getSensorId() == null || reading.getSensorId().isEmpty()) {
                Log.logmqtt.warn("Mensaje sin sensor_id, ignorando");
                return null;
            }
            
            if (reading.getStreetId() == null || reading.getStreetId().isEmpty()) {
                Log.logmqtt.warn("Mensaje sin street_id, ignorando");
                return null;
            }
            
            return reading;
            
        } catch (Exception e) {
            Log.logmqtt.error("Error parseando JSON: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Parsea el timestamp del mensaje en varios formatos posibles
     */
    private Timestamp parseTimestamp(String timestampStr) {
        // Formatos posibles según las instrucciones y la PL1
        String[] patterns = {
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",  // Formato ISO con microsegundos
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",  // Formato ISO con milisegundos y Z
            "yyyy-MM-dd'T'HH:mm:ss.SSS",     // Formato ISO con milisegundos
            "yyyy-MM-dd'T'HH:mm:ss",         // Formato ISO sin milisegundos
            "yyyy-MM-dd HH:mm:ss:SSS",       // Formato personalizado de la PL1
            "yyyy-MM-dd HH:mm:ss"            // Formato simple
        };
        
        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                LocalDateTime dateTime = LocalDateTime.parse(timestampStr, formatter);
                return Timestamp.valueOf(dateTime);
            } catch (DateTimeParseException e) {
                // Intentar siguiente formato
            }
        }
        
        // Si no se puede parsear, usar timestamp actual
        Log.logmqtt.warn("No se pudo parsear timestamp '{}', usando actual", timestampStr);
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * Verifica condiciones de alerta y envía notificaciones si es necesario
     */
    private void checkAndSendAlerts(SensorReading reading) {
        if (reading.getData() == null) return;
        
        SensorData data = reading.getData();
        int alertLevel = 0;
        
        // Verificar temperatura alta
        if (data.getTemperatureCelsius() != null) {
            if (data.getTemperatureCelsius() >= 40.0) {
                alertLevel = 4; // Crítica
            } else if (data.getTemperatureCelsius() >= 35.0) {
                alertLevel = 3; // Alta
            } else if (data.getTemperatureCelsius() >= 30.0) {
                alertLevel = 2; // Media
            } else if (data.getTemperatureCelsius() >= 28.0) {
                alertLevel = 1; // Baja
            }
        }
        
        // Verificar humedad muy alta
        if (data.getHumidityPercent() != null && data.getHumidityPercent() >= 90.0) {
            alertLevel = Math.max(alertLevel, 2);
        }
        
        // Si hay alerta, publicar mensaje de control
        if (alertLevel > 0) {
            String alertTopic = "sensors/" + reading.getStreetId() + "/alerts";
            String alertMessage = "{\"alert_level\":" + alertLevel + "}";
            
            MQTTBroker broker = new MQTTBroker();
            MQTTPublisher.publish(broker, alertTopic, alertMessage);
            
            Log.logmqtt.info("Alerta nivel {} enviada para sensor {} en tópico {}", 
                           alertLevel, reading.getSensorId(), alertTopic);
        }
    }

    /**
     * Desconecta el cliente MQTT
     */
    public void disconnect() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                Log.logmqtt.info("Cliente MQTT desconectado");
            }
        } catch (MqttException e) {
            Log.logmqtt.error("Error desconectando MQTT: {}", e.getMessage());
        }
    }
}
