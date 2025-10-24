#ifndef ESP32_UTILS_MQTT_HPP
#define ESP32_UTILS_MQTT_HPP

#include <PubSubClient.h>
#include <WiFi.h>
#include <ArduinoJson.h>
#include "config.h"

// Cliente MQTT
WiFiClient espClient;
PubSubClient mqttClient(espClient);

// ============================================
// FUNCIONES DE INICIALIZACIÓN MQTT
// ============================================

/**
 * Callback que se ejecuta al recibir mensajes MQTT
 */
void OnMqttReceived(char* topic, byte* payload, unsigned int length) {
    Serial.println("");
    Serial.println("===========================================");
    Serial.println("📩 Mensaje MQTT Recibido");
    Serial.println("===========================================");
    Serial.print("  Tópico: ");
    Serial.println(topic);
    Serial.print("  Longitud: ");
    Serial.print(length);
    Serial.println(" bytes");
    
    // Convertir payload a String
    String message = "";
    for (unsigned int i = 0; i < length; i++) {
        message += (char)payload[i];
    }
    
    Serial.println("  Contenido:");
    Serial.println("  " + message);
    Serial.println("===========================================");
    
    // Parsear JSON si es el tópico de control
    if (String(topic) == TOPIC_SUBSCRIBE) {
        DynamicJsonDocument doc(256);
        DeserializationError error = deserializeJson(doc, message);
        
        if (!error) {
            // Procesar comandos de control
            if (doc.containsKey("command")) {
                String command = doc["command"].as<String>();
                Serial.print(" Comando recibido: ");
                Serial.println(command);
                
                // Comandos disponibles
                if (command == "reset") {
                    Serial.println(" Reiniciando dispositivo...");
                    delay(1000);
                    ESP.restart();
                } else {
                    Serial.println("⚠ Comando no reconocido");
                }
            }
        } else {
            Serial.print("✗ Error parseando JSON: ");
            Serial.println(error.c_str());
        }
    }
}
/**
 * Inicializa la configuración del cliente MQTT
 */
void InitMQTT() {
    Serial.println("");
    Serial.println("===========================================");
    Serial.println("Inicializando MQTT...");
    Serial.println("===========================================");
    
    mqttClient.setServer(MQTT_BROKER_ADRESS, MQTT_PORT);
    mqttClient.setCallback(OnMqttReceived);
    mqttClient.setBufferSize(1024);  // Buffer grande para JSON
    

    Serial.print("  Broker: ");
    Serial.println(MQTT_BROKER_ADRESS);
    Serial.print("  Puerto: ");
    Serial.println(MQTT_PORT);
    Serial.println("===========================================");
}

/**
 * Conecta al broker MQTT, sirve para suscribirse a algún tópico
 */
void ConnectMQTT() {
    while (!mqttClient.connected()) {
        Serial.print("→ Conectando a MQTT... ");
        
        if (mqttClient.connect(MQTT_CLIENT_NAME)) {
            Serial.println("✓ Conectado");
            
            // Suscribirse al tópico de control
            if (mqttClient.subscribe(TOPIC_SUBSCRIBE)) {
                Serial.print("✓ Suscrito a: ");
                Serial.println(TOPIC_SUBSCRIBE);
            } else {
                Serial.println("✗ Error al suscribirse");
            }
        } else {
            Serial.print("✗ Error, rc=");
            Serial.print(mqttClient.state());
            Serial.println(" | Reintentando en 5s...");
            delay(5000);
        }
    }
}



/**
 * Publica un mensaje JSON en el tópico de datos
 */
void PublishMQTT(String jsonMessage) {
    if (mqttClient.connected()) {
        if (mqttClient.publish(TOPIC_PUBLISH, jsonMessage.c_str(), false)) {
            Serial.println("✓ Datos publicados en MQTT");
        } else {
            Serial.println("✗ Error publicando datos");
        }
    } else {
        Serial.println("⚠ MQTT desconectado. Intentando reconectar...");
        ConnectMQTT();
    }
}

/**
 * Mantiene la conexión MQTT activa
 */
void HandleMQTT() {
    if (!mqttClient.connected()) {
        ConnectMQTT();
    }
    mqttClient.loop();
}

/**
 * Verifica el estado de la conexión MQTT
 */
bool IsMQTTConnected() {
    return mqttClient.connected();
}

#endif

