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
 * Actuadores implementados (3):
 * - LED RGB: Indicador visual de estado
 * - Ventilador: Control de temperatura
 * - Calefactor: Control de temperatura
 * 
 * Total: 5 componentes (2 sensores + 3 actuadores)
 * =====================================================
 */

#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include <Wire.h>
#include <Adafruit_BME280.h>
#include "config.h"
#include "ESP32_UTILS.hpp"
#include "ESP32_Utils_MQTT.hpp"

// ============================================
// OBJETOS DE SENSORES
// ============================================
Adafruit_BME280 bme;  // BME280 sensor (dirección 0x76)

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
int airQuality = 0;            // AQI del MQ-135
int uvIndex = 0;               // Índice UV (simulado/opcional)
float windSpeed = 0.0;         // Velocidad del viento (simulado)
int windDirection = 0;         // Dirección del viento (simulado)

// Estados de sensores
bool bme_available = false;

// Estados de actuadores
bool fanActive = false;
bool heaterActive = false;

// ============================================
// FUNCIONES DE CONFIGURACIÓN
// ============================================

/**
 * Inicializa todos los pines de entrada/salida
 */
void InitPins() {
    Serial.println("Configurando pines...");
    
    // Pines de actuadores como salida
    pinMode(LED_RED_PIN, OUTPUT);
    pinMode(LED_GREEN_PIN, OUTPUT);
    pinMode(LED_BLUE_PIN, OUTPUT);
    pinMode(FAN_PIN, OUTPUT);
    pinMode(HEATER_PIN, OUTPUT);
    
    // Pines de sensores analógicos como entrada
    pinMode(UV_SENSOR_PIN, INPUT);
    pinMode(WIND_SPEED_PIN, INPUT);
    pinMode(WIND_DIR_PIN, INPUT);
    pinMode(AIR_QUALITY_PIN, INPUT);
    
    // Estado inicial: apagado
    digitalWrite(FAN_PIN, LOW);
    digitalWrite(HEATER_PIN, LOW);
    SetLED(0, 0, 0);
    
    Serial.println("✓ Pines configurados");
}

/**
 * Inicializa todos los sensores
 */
void InitSensors() {
    Serial.println("Inicializando sensores...");
    
    // Inicializar comunicación I2C
    Wire.begin(BME_SDA, BME_SCL);
    delay(100);
    
    // Inicializar BME280
    if (bme.begin(BME280_ADDRESS)) {
        Serial.println("✓ BME280 inicializado correctamente");
        bme.setSampling(Adafruit_BME280::MODE_NORMAL,
                        Adafruit_BME280::SAMPLING_X2,  // Temperatura
                        Adafruit_BME280::SAMPLING_X16, // Presión
                        Adafruit_BME280::SAMPLING_X1,  // Humedad
                        Adafruit_BME280::FILTER_X16,
                        Adafruit_BME280::STANDBY_MS_500);
        bme_available = true;
    } else {
        Serial.println("⚠ BME280 no encontrado. Verifica las conexiones.");
        Serial.println("⚠ Usando valores simulados.");
        bme_available = false;
    }
    
    // Configurar pines analógicos para MQ-135
    pinMode(MQ135_PIN, INPUT);
    pinMode(UV_SENSOR_PIN, INPUT);
    Serial.println("✓ Sensor MQ-135 configurado");
    
    Serial.println("✓ Inicialización de sensores completada");
}

/**
 * Control del LED RGB
 */
void SetLED(int r, int g, int b) {
    analogWrite(LED_RED_PIN, r);
    analogWrite(LED_GREEN_PIN, g);
    analogWrite(LED_BLUE_PIN, b);
}

/**
 * Indica estado con LED
 */
void IndicateStatus(String status) {
    if (status == "ok") {
        SetLED(0, 255, 0);  // Verde
    } else if (status == "warning") {
        SetLED(255, 165, 0);  // Naranja
    } else if (status == "error") {
        SetLED(255, 0, 0);  // Rojo
    } else if (status == "connecting") {
        SetLED(0, 0, 255);  // Azul
    }
}

// ============================================
// FUNCIONES DE LECTURA DE SENSORES
// ============================================

/**
 * Lee la temperatura del BME280
 */
float ReadTemperature() {
    if (bme_available) {
        float temp = bme.readTemperature();
        if (!isnan(temp) && temp > -40 && temp < 85) {
            return temp;
        }
    }
    
    // Si no hay sensor o lectura inválida, simular valor
    return 20.0 + random(-5, 10) / 10.0;
}

/**
 * Lee la humedad del BME280
 */
float ReadHumidity() {
    if (bme_available) {
        float hum = bme.readHumidity();
        if (!isnan(hum) && hum >= 0 && hum <= 100) {
            return hum;
        }
    }
    
    // Si no hay sensor o lectura inválida, simular valor
    return 60.0 + random(-10, 10);
}

/**
 * Lee la presión atmosférica del BME280
 */
float ReadPressure() {
    if (bme_available) {
        float press = bme.readPressure() / 100.0F;  // Convertir a hPa
        if (!isnan(press) && press > 800 && press < 1200) {
            return press;
        }
    }
    
    // Si no hay sensor o lectura inválida, simular valor
    return 1013.25 + random(-5, 5);
}

/**
 * Lee el sensor MQ-135 y convierte a AQI
 * Formula basada en la hoja de datos del MQ-135
 */
int ReadAirQuality() {
    // Leer valor analógico (0-4095 en ESP32)
    int sensorValue = analogRead(MQ135_PIN);
    
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
    
    // Convertir ppm a AQI (Air Quality Index)
    // Basado en estándares EPA de USA
    int aqi;
    if (ppm < 400) {
        aqi = map(ppm, 0, 400, 0, 50);        // Buena (0-50)
    } else if (ppm < 1000) {
        aqi = map(ppm, 400, 1000, 51, 100);   // Moderada (51-100)
    } else if (ppm < 2000) {
        aqi = map(ppm, 1000, 2000, 101, 150); // Dañina para sensibles (101-150)
    } else if (ppm < 5000) {
        aqi = map(ppm, 2000, 5000, 151, 200); // Dañina (151-200)
    } else {
        aqi = map(ppm, 5000, 10000, 201, 300); // Muy dañina (201-300)
    }
    
    return constrain(aqi, 0, 500);
}

/**
 * Calcula el índice UV desde el sensor analógico (opcional)
 */
int ReadUVIndex() {
    int sensorValue = analogRead(UV_SENSOR_PIN);
    // Conversión aproximada (depende del sensor específico)
    int uvIndex = map(sensorValue, 0, 4095, 0, 11);
    return constrain(uvIndex, 0, 11);
}

/**
 * Lee la velocidad del viento (simulado para demostración)
 */
float ReadWindSpeed() {
    // En un sistema real, aquí irían las lecturas del anemómetro
    // Por ahora, generamos datos simulados realistas
    return random(0, 30) / 10.0;  // 0-3.0 km/h (viento ligero)
}

/**
 * Lee la dirección del viento (simulado para demostración)
 */
int ReadWindDirection() {
    // En un sistema real, aquí irían las lecturas de la veleta
    // Por ahora, generamos datos simulados
    int directions[] = {0, 45, 90, 135, 180, 225, 270, 315};
    return directions[random(0, 8)];
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
    
    // Leer sensor MQ-135
    airQuality = ReadAirQuality();
    
    // Leer sensor UV (opcional)
    uvIndex = ReadUVIndex();
    
    // Leer viento (simulado)
    windSpeed = ReadWindSpeed();
    windDirection = ReadWindDirection();
    
    Serial.println("Lecturas de sensores:");
    Serial.printf("  🌡️  Temperatura: %.1f°C\n", temperature);
    Serial.printf("  💧 Humedad: %.1f%%\n", humidity);
    Serial.printf("  📏 Presión: %.1f hPa\n", pressure);
    Serial.printf("  🏭 Calidad del Aire (AQI): %d\n", airQuality);
    Serial.printf("  ☀️  Índice UV: %d\n", uvIndex);
    Serial.printf("  💨 Viento: %.1f km/h @ %d°\n", windSpeed, windDirection);
    Serial.println("===========================================");
}

// ============================================
// CONTROL DE ACTUADORES
// ============================================

/**
 * Controla los actuadores basándose en las lecturas
 */
void ControlActuators() {
    // Control del ventilador por temperatura
    if (temperature > TEMP_FAN_THRESHOLD && !fanActive) {
        digitalWrite(FAN_PIN, HIGH);
        fanActive = true;
        Serial.println("🌀 Ventilador: ACTIVADO (temp alta)");
    } else if (temperature <= TEMP_FAN_THRESHOLD - 2 && fanActive) {
        digitalWrite(FAN_PIN, LOW);
        fanActive = false;
        Serial.println("🌀 Ventilador: DESACTIVADO");
    }
    
    // Control del calefactor por temperatura
    if (temperature < TEMP_HEATER_THRESHOLD && !heaterActive) {
        digitalWrite(HEATER_PIN, HIGH);
        heaterActive = true;
        Serial.println("🔥 Calefactor: ACTIVADO (temp baja)");
    } else if (temperature >= TEMP_HEATER_THRESHOLD + 2 && heaterActive) {
        digitalWrite(HEATER_PIN, LOW);
        heaterActive = false;
        Serial.println("🔥 Calefactor: DESACTIVADO");
    }
    
    // Control del LED RGB según condiciones
    if (temperature > 35 || uvIndex > 8) {
        IndicateStatus("warning");  // Naranja: condiciones extremas
    } else if (airQuality > AQI_DANGEROUS) {
        SetLED(128, 0, 128);  // Morado: mala calidad del aire
    } else if (humidity > HUMIDITY_HIGH) {
        SetLED(0, 100, 200);  // Azul: humedad alta
    } else {
        IndicateStatus("ok");  // Verde: todo normal
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
    data["wind_speed_kmh"] = round(windSpeed * 10) / 10.0;
    data["wind_direction_degrees"] = windDirection;
    data["atmospheric_pressure_hpa"] = round(pressure * 10) / 10.0;
    data["uv_index"] = uvIndex;
    
    // Información adicional de actuadores (extra)
    data["fan_active"] = fanActive;
    data["heater_active"] = heaterActive;
    
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