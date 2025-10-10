#ifndef ESP32_UTILS_MQTT_HPP
#define ESP32_UTILS_MQTT_HPP

#include <PubSubClient.h>
#include <WiFi.h>
#include <ArduinoJson.h>

// Cliente MQTT
WiFiClient espClient;
PubSubClient mqttClient(espClient);

// ============================================
// FUNCIONES DE INICIALIZACIÓN MQTT
// ============================================

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
 * Conecta al broker MQTT
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
        DynamicJsonDocument doc(512);
        DeserializationError error = deserializeJson(doc, message);
        
        if (!error) {
            // Procesar comandos de control
            if (doc.containsKey("command")) {
                String command = doc["command"].as<String>();
                Serial.print("🎮 Comando recibido: ");
                Serial.println(command);
                
                // Ejemplos de comandos que se pueden implementar
                if (command == "reset") {
                    Serial.println("⚡ Reiniciando dispositivo...");
                    delay(1000);
                    ESP.restart();
                } else if (command == "read_now") {
                    Serial.println("📊 Forzando lectura inmediata...");
                    // Aquí se llamaría a la función de lectura
                } else if (command == "fan_on" && doc.containsKey("value")) {
                    digitalWrite(FAN_PIN, doc["value"].as<bool>() ? HIGH : LOW);
                    Serial.println("🌀 Ventilador: " + String(doc["value"].as<bool>() ? "ON" : "OFF"));
                } else if (command == "heater_on" && doc.containsKey("value")) {
                    digitalWrite(HEATER_PIN, doc["value"].as<bool>() ? HIGH : LOW);
                    Serial.println("🔥 Calefactor: " + String(doc["value"].as<bool>() ? "ON" : "OFF"));
                } else if (command == "led_rgb" && doc.containsKey("r") && doc.containsKey("g") && doc.containsKey("b")) {
                    analogWrite(LED_RED_PIN, doc["r"].as<int>());
                    analogWrite(LED_GREEN_PIN, doc["g"].as<int>());
                    analogWrite(LED_BLUE_PIN, doc["b"].as<int>());
                    Serial.println("💡 LED RGB actualizado");
                }
            }
        } else {
            Serial.print("✗ Error parseando JSON: ");
            Serial.println(error.c_str());
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

