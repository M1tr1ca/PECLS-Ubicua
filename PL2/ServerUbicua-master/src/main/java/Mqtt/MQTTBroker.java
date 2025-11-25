package mqtt;

public class MQTTBroker {

    private static int qos = 2;
    // Puerto 3000 mapeado al 1883 interno del contenedor MQTT
    // En producción (Docker) usar mqtt-broker:1883, en desarrollo localhost:3000
    private static final String broker = System.getenv("MQTT_BROKER") != null ? 
                                         System.getenv("MQTT_BROKER") : "tcp://localhost:3000";
    private static final String clientId = "ServerUbicuaUAH";
    private static final String username = System.getenv("MQTT_USERNAME") != null ? 
                                           System.getenv("MQTT_USERNAME") : "ubicua";
    private static final String password = System.getenv("MQTT_PASSWORD") != null ? 
                                           System.getenv("MQTT_PASSWORD") : "ubicua";
    
    public MQTTBroker() {
    }

    public static int getQos() {
        return qos;
    }

    public static String getBroker() {
        return broker;
    }

    public static String getClientId() {
        return clientId;
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }
    
}
