package logic;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import mqtt.MQTTBroker;
import mqtt.MQTTPublisher;
import mqtt.MQTTSuscriber;

/**
 * Clase encargada de inicializar el sistema MQTT al arrancar el servidor
 * Se suscribe a los tópicos de los sensores meteorológicos
 */
@WebListener
public class Projectinitializer implements ServletContextListener {

    private MQTTSuscriber suscriber;

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Log.log.info("-->Servidor detenido, desconectando MQTT<--");
        if (suscriber != null) {
            suscriber.disconnect();
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Log.log.info("===========================================");
        Log.log.info("  SERVIDOR UBICUA - PECL2 INICIADO");
        Log.log.info("===========================================");
        Log.log.info("-->Suscribiéndose a tópicos MQTT<--");
        
        MQTTBroker broker = new MQTTBroker();
        suscriber = new MQTTSuscriber();
        
        // Suscribirse con wildcard para recibir de todos los sensores
        // Formato del tópico: sensors/{street_id}/weather_station/{sensor_id}
        // Usamos # para suscribirnos a todos los sensores
        String topicPattern = "sensors/#";
        suscriber.suscribeTopic(broker, topicPattern);
        
        Log.log.info("Suscrito al patrón de tópicos: {}", topicPattern);
        
        // Publicar mensaje de test para confirmar conexión
        MQTTPublisher.publish(broker, "server/status", "{\"status\":\"online\",\"message\":\"Servidor Ubicua PECL2 iniciado\"}");
        
        Log.log.info("===========================================");
        Log.log.info("  SISTEMA LISTO PARA RECIBIR DATOS");
        Log.log.info("===========================================");
    }
}
