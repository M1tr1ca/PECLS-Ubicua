/*
 * =====================================================
 * ESTACIÓN METEOROLÓGICA IoT - CIUDAD 4.0
 * Universidad de Alcalá de Henares
 * =====================================================
 * 
 * Proyecto: PECL1 - Computación Ubicua
 * Dispositivo: Estación Meteorológica
 * Ubicación: Alcalá de Henares, Centro
 * Tipo: Weather Station (weather)
 * 
 * Descripción:
 * Este dispositivo IoT captura datos meteorológicos en tiempo real
 * y los envía a un broker MQTT siguiendo el formato JSON especificado.
 * 
 * Sensores implementados (2):
 * - BME280: Temperatura, Humedad y Presión Atmosférica (3 en 1)
 * - MQ-135: Calidad del aire (CO2, NH3, NOx, alcohol, benceno, humo)
 * 
 * Actuadores implementados (1):
 * - LED Rojo: Indicador visual de alertas (temperatura alta, CAQI peligroso, humedad alta)
 * 
 * Total: 3 componentes (2 sensores + 1 actuador)
 * =====================================================
 */

#include <WiFi.h>   
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include  <Wire.h> //Incluye protocolo de comunicación I^2C, el sensor transmitirá los datos en 1's y 0's (SDA), sincronizados con un reloj (SCL)
//Wire.h establece por defecto el pin 21 para SDA y el pin 22 para SCL

#include <Adafruit_BME280.h> //Librería que facilita el manejo del sensor (inicializa el sensor, lee datos, fórmulas de calibración...)
#include "config.h"
#include "ESP32_UTILS.hpp"
#include "ESP32_Utils_MQTT.hpp"


Adafruit_BME280 sensor_bme280; //Creamos una instancia del objeto para el sensor

// ============================================
// VARIABLES GLOBALES
// ============================================
unsigned long lastReadingTime = 0;
unsigned long lastPublishTime = 0;
int messageCount = 0;

// Variables de lecturas de sensores
float temperature = 0.0;       // Temperatura BME280
float humidity = 0.0;          // Humedad BME280
float pressure = 0.0;          // Presión BME280
float altitude = 0.0;          // Calcula la altitud a partir de la presión
int airQuality = 0;            // CAQI del MQ-135

// Estados de sensores
bool bme_available = false;
bool mq135_available = false;

// ============================================
// FUNCIONES DE CONFIGURACIÓN
// ============================================

/**
 * Inicializa todos los pines de entrada/salida
 */
void InitPins() {
    Serial.println("Configurando pines...");
    
    // Pines de actuadores como salida, rojo si la temperatura o calidad del aire es alta
    pinMode(LED_RED_PIN, OUTPUT);
    
    // Pines de sensores analógicos como entrada, lo hacemos en InitSensors()
    //pinMode(MQ135_PIN, INPUT);

    //BME280: Usa I2C (no necesita pinMode, se configura con Wire.begin())
    
    // Estado inicial: LED apagado
    digitalWrite(LED_RED_PIN, LOW);
    
    Serial.println("✓ Pines configurados");
}

/**
 * Inicializa todos los sensores
 */
void InitSensors() {
    Serial.println("Inicializando sensores...");
 

    // Inicializar BME280
    if(!sensor_bme280.begin(0x76)){ //Trata de inicializar el sensor en esa dirección de memoria que es la estándar para ese sensor en el I^2C
    Serial.println("No se encontró el sensor BME280. Revisar las conexiones.");
    while (1);
    }
    bme_available = true;
    Serial.println("Sensor BME280 inicializado correctamente");
    
    // Configurar pines analógicos para MQ-135
    pinMode(MQ135_PIN, INPUT);
    
    // Verificar que el sensor MQ-135 responde
    int testRead = analogRead(MQ135_PIN);
    if (testRead > 0 && testRead < 4095) {
        mq135_available = true;
        Serial.println("✓ Sensor MQ-135 configurado y disponible");
    } else {
        mq135_available = false;
        Serial.println("⚠ MQ-135: Señal inusual. Verifica las conexiones.");
        //Serial.println("⚠ Usando valores simulados para calidad del aire.");
    }
    
    Serial.println("✓ Inicialización de sensores completada");
}

/**
 * Control del LED Rojo
 */
void SetLED(bool state) {
    digitalWrite(LED_RED_PIN, state ? HIGH : LOW);
}

/**
 * Indica estado con LED
 */
void IndicateStatus(String status) {
    if (status == "ok") {
        digitalWrite(LED_RED_PIN, LOW);  // Apagado: todo bien
    } else if (status == "warning" || status == "error" || status == "connecting") {
        digitalWrite(LED_RED_PIN, HIGH);  // Encendido: alerta o conectando
    }
}

// ============================================
// FUNCIONES DE LECTURA DE SENSORES
// ============================================

/**
 * Lee la temperatura del BME280
 */
float ReadTemperature() {
    temperature = sensor_bme280.readTemperature();
    return temperature; //Si el sensor se desconectase, mandaría un valor NaN, o similar, hay que comprobarlo cuando recibe este valor
}

/**
 * Lee la humedad del BME280
 */
float ReadHumidity() {
    humidity = sensor_bme280.readHumidity(); 
    return humidity;//Si el sensor se desconectase, mandaría un valor NaN, o similar, hay que comprobarlo cuando recibe este valor

}

/**
 * Lee la presión atmosférica del BME280
 */
float ReadPressure() {
    pressure = sensor_bme280.readPressure() / 100.0; //Por defecto se recibe en Pa, se divide entre 100 para convertirlo en hPa (medida típica)
    return pressure;//Si el sensor se desconectase, mandaría un valor NaN, o similar, hay que comprobarlo cuando recibe este valor
}
float ReadAltitude(){
    altitude = sensor_bme280.readAltitude(1013.25); // 1013.25 es la presión barométrica a nivel del mar por defecto
    return altitude;//Si el sensor se desconectase, mandaría un valor NaN, o similar, hay que comprobarlo cuando recibe este valor
}

/**
 * Lee el sensor MQ-135 y convierte a CAQI
 * Formula basada en la hoja de datos del MQ-135
 */
int ReadAirQuality() {
    if (mq135_available) {
        // Leer valor analógico (0-4095 en ESP32)
        int sensorValue = analogRead(MQ135_PIN);
        
        // Verificar lectura válida
        if (sensorValue > 0 && sensorValue < 4095) {
            // Convertir a voltaje (0-3.3V)
            float voltage = (sensorValue / 4095.0) * 3.3;
            
            // Calcular resistencia del sensor
            // Rs = [(Vc x RL) / Vout] - RL
            float Rs = ((3.3 * MQ135_RL) / voltage) - MQ135_RL;
            
            // Calcular ratio Rs/Ro
            float ratio = Rs / MQ135_RO_CLEAN_AIR;
            
            // Convertir a concentración de CO2 en ppm (fórmula aproximada)
            // ppm = 116.6020682 * pow(ratio, -2.769034857)
            float ppm = 116.6020682 * pow(ratio, -2.769034857);
            
            // Convertir ppm a CAQI (Common Air Quality Index)
            // Basado en normativa europea
            int caqi;
            if (ppm <= 600) {
                // map convierte el valor de CO2 (entre 0-600 ppm) a la escala CAQI (0-25)
                caqi = map(ppm, 0, 600, 0, 25);           // Muy bajo (0-25)
            } else if (ppm <= 800) {
                caqi = map(ppm, 600, 800, 26, 50);        // Bajo (26-50)
            } else if (ppm <= 1000) {
                caqi = map(ppm, 800, 1000, 51, 75);       // Medio (51-75)
            } else if (ppm <= 1500) {
                caqi = map(ppm, 1000, 1500, 76, 100);     // Alto (76-100)
            } else {
                caqi = map(ppm, 1500, 5000, 101, 150);    // Muy alto (>100)
            }
            
            return constrain(caqi, 0, 150);
        }
    }
    
    // Si no hay sensor o lectura inválida, retornar error
    Serial.println("⚠ Error: MQ-135 no disponible o lectura inválida");
    return -1;  // Valor de error
}

/**
 * Lee todos los sensores
 */
void ReadAllSensors() {
    Serial.println("");
    Serial.println("===========================================");
    Serial.println("📊 Leyendo sensores...");
    Serial.println("===========================================");
    
    // Leer sensores BME280
    temperature = ReadTemperature();
    humidity = ReadHumidity();
    pressure = ReadPressure();
    altitude = ReadAltitude();
    
    // Leer sensor MQ-135
    airQuality = ReadAirQuality();
    
    Serial.println("Lecturas de sensores:");
    Serial.printf("  🌡️ Temperatura: %.1f°C\n", temperature);
    Serial.printf("  💧 Humedad: %.1f%%\n", humidity);
    Serial.printf("  📏 Presión: %.1f hPa\n", pressure);
    Serial.printf("      Altitud: %.1f m\n", altitude);
    Serial.printf("  🏭 Calidad del Aire (CAQI): %d\n", airQuality);
    Serial.println("===========================================");
}

// ============================================
// CONTROL DE ACTUADORES
// ============================================

/**
 * Controla los actuadores basándose en las lecturas
 */
void ControlActuators() {
    // Control del LED rojo según condiciones
    if (temperature > TEMP_HIGH || airQuality > CAQI_DANGEROUS || humidity > HUMIDITY_HIGH) {
        digitalWrite(LED_RED_PIN, HIGH);  // Encendido: condiciones anormales
    } else {
        digitalWrite(LED_RED_PIN, LOW);   // Apagado: todo normal
    }
}

// ============================================
// CREACIÓN Y ENVÍO DE MENSAJES JSON
// ============================================

/**
 * Crea el mensaje JSON según el formato especificado
 */
String CreateJSONMessage() {
    DynamicJsonDocument doc(1024);
    
    // Información básica de la estación
    doc["sensor_id"] = SENSOR_ID;
    doc["sensor_type"] = SENSOR_TYPE;
    doc["street_id"] = STREET_ID;
    
    // Timestamp (formato ISO 8601)
    char timestamp[30];
    unsigned long currentTime = millis();
    sprintf(timestamp, "2025-10-%02d T%02d:%02d:%02d.%03lu",
            (int)(currentTime / 86400000) % 30 + 1,  // Día
            (int)(currentTime / 3600000) % 24,        // Hora
            (int)(currentTime / 60000) % 60,          // Minuto
            (int)(currentTime / 1000) % 60,           // Segundo
            currentTime % 1000);                       // Milisegundo
    doc["timestamp"] = timestamp;
    
    // Ubicación
    JsonObject location = doc.createNestedObject("location");
    location["latitude"] = LATITUDE;
    location["longitude"] = LONGITUDE;
    location["altitude_meters"] = ALTITUDE;
    location["district"] = DISTRICT;
    location["neighborhood"] = NEIGHBORHOOD;
    
    // Datos meteorológicos
    JsonObject data = doc.createNestedObject("data");
    data["temperature_celsius"] = round(temperature * 10) / 10.0;
    data["humidity_percent"] = round(humidity * 10) / 10.0;
    data["air_quality_index"] = airQuality;
    data["atmospheric_pressure_hpa"] = round(pressure * 10) / 10.0;
    
    // Serializar a String
    String jsonString;
    serializeJson(doc, jsonString);
    
    return jsonString;
}

/**
 * Publica los datos en MQTT
 */
void PublishData() {
    if (!IsMQTTConnected()) {
        Serial.println("⚠ MQTT no conectado. Saltando publicación.");
        return;
    }
    
    String jsonMessage = CreateJSONMessage();
    
    Serial.println("");
    Serial.println("===========================================");
    Serial.println("📤 Publicando datos...");
    Serial.println("===========================================");
    Serial.println("JSON generado:");
    Serial.println(jsonMessage);
    Serial.println("-------------------------------------------");
    
    PublishMQTT(jsonMessage);
    
    messageCount++;
    Serial.print("✓ Mensaje #");
    Serial.print(messageCount);
    Serial.println(" enviado");
    Serial.println("===========================================");
}

// ============================================
// SETUP Y LOOP
// ============================================

void setup() {
    Serial.begin(115200);
    delay(1000);
    
    Serial.println("");
    Serial.println("═══════════════════════════════════════════");
    Serial.println("  ESTACIÓN METEOROLÓGICA IoT");
    Serial.println("  Universidad de Alcalá de Henares");
    Serial.println("  PECL1 - Computación Ubicua");
    Serial.println("═══════════════════════════════════════════");
    Serial.println("");
    
    // Configurar pines
    InitPins();
    
    // Indicar estado de conexión
    IndicateStatus("connecting");
    
    // Conectar a WiFi
    WiFi.onEvent(WiFiEvent);
    ConnectWifi_STA(false);
    
    // Inicializar MQTT
    InitMQTT();
    ConnectMQTT();
    
    // Inicializar sensores
    InitSensors();
    
    // Primera lectura
    ReadAllSensors();
    ControlActuators();
    
    IndicateStatus("ok");
    
    Serial.println("");
    Serial.println("✓ Sistema inicializado correctamente");
    Serial.println("✓ Estación lista para operar");
    Serial.println("");
}

void loop() {
    // Mantener conexiones activas
    CheckWiFiConnection();
    HandleMQTT();
    
    // Leer sensores cada READING_INTERVAL
    if (millis() - lastReadingTime >= READING_INTERVAL) {
        ReadAllSensors();
        ControlActuators();
        PublishData();
        lastReadingTime = millis();
    }
    
    // Pequeña pausa para no saturar el loop
    delay(100);
}