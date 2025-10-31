#ifndef ESP32_UTILS_MQTT_HPP
#define ESP32_UTILS_MQTT_HPP

#include <PubSubClient.h>
#include <WiFi.h>
#include <ArduinoJson.h>
#include "config.h"

// Cliente MQTT
WiFiClient espClient;
PubSubClient mqttClient(espClient);

// Declaración externa de funciones del display (definidas en .ino)
extern void DisplayNumber(int number);

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
            // Procesar nivel de alerta
            if (doc.containsKey("alert_level")) {
                int alertLevel = doc["alert_level"].as<int>();
                Serial.print("🚨 Nivel de alerta recibido: ");
                Serial.println(alertLevel);
                
                // Mostrar nivel en el display de 7 segmentos
                DisplayNumber(alertLevel);
                Serial.print("📟 Display mostrando: ");
                Serial.println(alertLevel);
                
                // Configurar parpadeo según nivel de alerta
                switch(alertLevel) {
                    case 0: // Sin alerta - LED apagado
                        digitalWrite(LED_RED_PIN, LOW);
                        Serial.println("✓ Sin alerta - LED apagado");
                        break;
                        
                    case 1: // Alerta baja - Parpadeo lento (1 vez por segundo)
                        Serial.println("⚠️ Alerta BAJA - Parpadeo lento");
                        for(int i = 0; i < 3; i++) {
                            digitalWrite(LED_RED_PIN, HIGH);
                            delay(500);
                            digitalWrite(LED_RED_PIN, LOW);
                            delay(500);
                        }
                        break;
                        
                    case 2: // Alerta media - Parpadeo medio (2 veces por segundo)
                        Serial.println("⚠️ Alerta MEDIA - Parpadeo medio");
                        for(int i = 0; i < 6; i++) {
                            digitalWrite(LED_RED_PIN, HIGH);
                            delay(250);
                            digitalWrite(LED_RED_PIN, LOW);
                            delay(250);
                        }
                        break;
                        
                    case 3: // Alerta alta - Parpadeo rápido (4 veces por segundo)
                        Serial.println("🚨 Alerta ALTA - Parpadeo rápido");
                        for(int i = 0; i < 12; i++) {
                            digitalWrite(LED_RED_PIN, HIGH);
                            delay(125);
                            digitalWrite(LED_RED_PIN, LOW);
                            delay(125);
                        }
                        break;
                        
                    case 4: // Alerta crítica - LED encendido permanentemente
                        digitalWrite(LED_RED_PIN, HIGH);
                        Serial.println("💀 Alerta CRÍTICA - LED encendido continuo");
                        break;
                        
                    default:
                        Serial.println("⚠️ Nivel de alerta no válido (usar 0-4)");
                        DisplayNumber(-1); // Mostrar guión en el display
                        break;
                }
            }
            // Procesar comandos de control
            else if (doc.containsKey("command")) {
                String command = doc["command"].as<String>();
                Serial.print("✓ Comando recibido: ");
                Serial.println(command);
                
                // Comandos disponibles
                if (command == "reset") {
                    Serial.println("🔄 Reiniciando dispositivo...");
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
 * Callback simplificado que solo muestra el mensaje como texto
 */
void OnMqttReceived2(char* topic, byte* payload, unsigned int length) {
    // Convertir payload a String
    String message = "";
    for (unsigned int i = 0; i < length; i++) {
        message += (char)payload[i];
    }
    
    // Mostrar solo el mensaje
    Serial.println(message);
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

