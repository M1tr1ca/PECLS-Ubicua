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
 * Sensores implementados (5):
 * - BME280 #1: Temperatura, Humedad y Presión Atmosférica
 * - BME280 #2: Temperatura, Humedad y Presión Atmosférica (redundancia)
 * - MQ-135 #1: Calidad del aire (CO2, NH3, NOx, alcohol, benceno, humo)
 * - MQ-135 #2: Calidad del aire (sensor redundante)
 * - MQ-135 #3: Calidad del aire (sensor redundante)
 * 
 * Actuadores implementados (3):
 * - LED RGB: Indicador visual de estado
 * - Ventilador: Control de temperatura
 * - Calefactor: Control de temperatura
 * 
 * Total: 8 componentes (5 sensores + 3 actuadores)
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
Adafruit_BME280 bme1;  // BME280 sensor #1 (dirección 0x76)
Adafruit_BME280 bme2;  // BME280 sensor #2 (dirección 0x77)

// ============================================
// VARIABLES GLOBALES
// ============================================
unsigned long lastReadingTime = 0;
unsigned long lastPublishTime = 0;
int messageCount = 0;

// Variables de lecturas de sensores
float temperature1 = 0.0;      // Temperatura BME280 #1
float temperature2 = 0.0;      // Temperatura BME280 #2
float temperature_avg = 0.0;   // Temperatura promedio
float humidity1 = 0.0;         // Humedad BME280 #1
float humidity2 = 0.0;         // Humedad BME280 #2
float humidity_avg = 0.0;      // Humedad promedio
float pressure1 = 0.0;         // Presión BME280 #1
float pressure2 = 0.0;         // Presión BME280 #2
float pressure_avg = 0.0;      // Presión promedio
int airQuality1 = 0;           // AQI del MQ-135 #1
int airQuality2 = 0;           // AQI del MQ-135 #2
int airQuality3 = 0;           // AQI del MQ-135 #3
int airQuality_avg = 0;        // AQI promedio
int uvIndex = 0;               // Índice UV (simulado/opcional)
float windSpeed = 0.0;         // Velocidad del viento (simulado)
int windDirection = 0;         // Dirección del viento (simulado)

// Estados de sensores
bool bme1_available = false;
bool bme2_available = false;

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
    
    // Inicializar BME280 #1 (dirección 0x76)
    if (bme1.begin(BME280_ADDRESS_1)) {
        Serial.println("✓ BME280 #1 inicializado (0x76)");
        bme1.setSampling(Adafruit_BME280::MODE_NORMAL,
                        Adafruit_BME280::SAMPLING_X2,  // Temperatura
                        Adafruit_BME280::SAMPLING_X16, // Presión
                        Adafruit_BME280::SAMPLING_X1,  // Humedad
                        Adafruit_BME280::FILTER_X16,
                        Adafruit_BME280::STANDBY_MS_500);
        bme1_available = true;
    } else {
        Serial.println("⚠ BME280 #1 no encontrado (0x76)");
        bme1_available = false;
    }
    
    delay(100);
    
    // Inicializar BME280 #2 (dirección 0x77)
    if (bme2.begin(BME280_ADDRESS_2)) {
        Serial.println("✓ BME280 #2 inicializado (0x77)");
        bme2.setSampling(Adafruit_BME280::MODE_NORMAL,
                        Adafruit_BME280::SAMPLING_X2,  // Temperatura
                        Adafruit_BME280::SAMPLING_X16, // Presión
                        Adafruit_BME280::SAMPLING_X1,  // Humedad
                        Adafruit_BME280::FILTER_X16,
                        Adafruit_BME280::STANDBY_MS_500);
        bme2_available = true;
    } else {
        Serial.println("⚠ BME280 #2 no encontrado (0x77)");
        bme2_available = false;
    }
    
    // Verificar que al menos un BME280 esté disponible
    if (!bme1_available && !bme2_available) {
        Serial.println("⚠ ADVERTENCIA: Ningún BME280 detectado. Usando valores simulados.");
    }
    
    // Configurar pines analógicos para MQ-135
    pinMode(MQ135_PIN_1, INPUT);
    pinMode(MQ135_PIN_2, INPUT);
    pinMode(MQ135_PIN_3, INPUT);
    pinMode(UV_SENSOR_PIN, INPUT);
    Serial.println("✓ Sensores MQ-135 configurados");
    
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
 * Lee la temperatura de ambos BME280 y calcula el promedio
 */
float ReadTemperature() {
    float temp1 = 20.0;  // Valor por defecto
    float temp2 = 20.0;
    int validReadings = 0;
    
    if (bme1_available) {
        temp1 = bme1.readTemperature();
        if (!isnan(temp1) && temp1 > -40 && temp1 < 85) {
            temperature1 = temp1;
            validReadings++;
        }
    }
    
    if (bme2_available) {
        temp2 = bme2.readTemperature();
        if (!isnan(temp2) && temp2 > -40 && temp2 < 85) {
            temperature2 = temp2;
            validReadings++;
        }
    }
    
    // Calcular promedio si hay lecturas válidas
    if (validReadings > 0) {
        if (validReadings == 2) {
            return (temperature1 + temperature2) / 2.0;
        } else if (bme1_available) {
            return temperature1;
        } else {
            return temperature2;
        }
    }
    
    // Si no hay sensores, simular valor
    return 20.0 + random(-5, 10) / 10.0;
}

/**
 * Lee la humedad de ambos BME280 y calcula el promedio
 */
float ReadHumidity() {
    float hum1 = 60.0;  // Valor por defecto
    float hum2 = 60.0;
    int validReadings = 0;
    
    if (bme1_available) {
        hum1 = bme1.readHumidity();
        if (!isnan(hum1) && hum1 >= 0 && hum1 <= 100) {
            humidity1 = hum1;
            validReadings++;
        }
    }
    
    if (bme2_available) {
        hum2 = bme2.readHumidity();
        if (!isnan(hum2) && hum2 >= 0 && hum2 <= 100) {
            humidity2 = hum2;
            validReadings++;
        }
    }
    
    // Calcular promedio si hay lecturas válidas
    if (validReadings > 0) {
        if (validReadings == 2) {
            return (humidity1 + humidity2) / 2.0;
        } else if (bme1_available) {
            return humidity1;
        } else {
            return humidity2;
        }
    }
    
    // Si no hay sensores, simular valor
    return 60.0 + random(-10, 10);
}

/**
 * Lee la presión atmosférica de ambos BME280 y calcula el promedio
 */
float ReadPressure() {
    float press1 = 1013.25;  // Presión estándar
    float press2 = 1013.25;
    int validReadings = 0;
    
    if (bme1_available) {
        press1 = bme1.readPressure() / 100.0F;  // Convertir a hPa
        if (!isnan(press1) && press1 > 800 && press1 < 1200) {
            pressure1 = press1;
            validReadings++;
        }
    }
    
    if (bme2_available) {
        press2 = bme2.readPressure() / 100.0F;  // Convertir a hPa
        if (!isnan(press2) && press2 > 800 && press2 < 1200) {
            pressure2 = press2;
            validReadings++;
        }
    }
    
    // Calcular promedio si hay lecturas válidas
    if (validReadings > 0) {
        if (validReadings == 2) {
            return (pressure1 + pressure2) / 2.0;
        } else if (bme1_available) {
            return pressure1;
        } else {
            return pressure2;
        }
    }
    
    // Si no hay sensores, simular valor
    return 1013.25 + random(-5, 5);
}

/**
 * Lee el sensor MQ-135 y convierte a AQI
 * Formula basada en la hoja de datos del MQ-135
 */
int ReadMQ135(int pin) {
    // Leer valor analógico (0-4095 en ESP32)
    int sensorValue = analogRead(pin);
    
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
 * Lee los 3 sensores MQ-135 y calcula el promedio
 */
int ReadAirQuality() {
    airQuality1 = ReadMQ135(MQ135_PIN_1);
    airQuality2 = ReadMQ135(MQ135_PIN_2);
    airQuality3 = ReadMQ135(MQ135_PIN_3);
    
    // Calcular promedio de los 3 sensores
    int average = (airQuality1 + airQuality2 + airQuality3) / 3;
    
    return average;
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
    temperature_avg = ReadTemperature();
    humidity_avg = ReadHumidity();
    pressure_avg = ReadPressure();
    
    // Leer sensores MQ-135
    airQuality_avg = ReadAirQuality();
    
    // Leer sensor UV (opcional)
    uvIndex = ReadUVIndex();
    
    // Leer viento (simulado)
    windSpeed = ReadWindSpeed();
    windDirection = ReadWindDirection();
    
    Serial.println("Lecturas BME280:");
    if (bme1_available) {
        Serial.printf("  📟 BME280 #1: %.1f°C, %.1f%%, %.1f hPa\n", 
                     temperature1, humidity1, pressure1);
    }
    if (bme2_available) {
        Serial.printf("  📟 BME280 #2: %.1f°C, %.1f%%, %.1f hPa\n", 
                     temperature2, humidity2, pressure2);
    }
    Serial.println("-------------------------------------------");
    Serial.println("Promedios:");
    Serial.printf("  🌡️  Temperatura: %.1f°C\n", temperature_avg);
    Serial.printf("  💧 Humedad: %.1f%%\n", humidity_avg);
    Serial.printf("  📏 Presión: %.1f hPa\n", pressure_avg);
    Serial.println("-------------------------------------------");
    Serial.println("Calidad del Aire (MQ-135):");
    Serial.printf("  🏭 Sensor #1 (AQI): %d\n", airQuality1);
    Serial.printf("  🏭 Sensor #2 (AQI): %d\n", airQuality2);
    Serial.printf("  🏭 Sensor #3 (AQI): %d\n", airQuality3);
    Serial.printf("  📊 Promedio AQI: %d\n", airQuality_avg);
    Serial.println("-------------------------------------------");
    Serial.println("Otros datos:");
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
    if (temperature_avg > TEMP_FAN_THRESHOLD && !fanActive) {
        digitalWrite(FAN_PIN, HIGH);
        fanActive = true;
        Serial.println("🌀 Ventilador: ACTIVADO (temp alta)");
    } else if (temperature_avg <= TEMP_FAN_THRESHOLD - 2 && fanActive) {
        digitalWrite(FAN_PIN, LOW);
        fanActive = false;
        Serial.println("🌀 Ventilador: DESACTIVADO");
    }
    
    // Control del calefactor por temperatura
    if (temperature_avg < TEMP_HEATER_THRESHOLD && !heaterActive) {
        digitalWrite(HEATER_PIN, HIGH);
        heaterActive = true;
        Serial.println("🔥 Calefactor: ACTIVADO (temp baja)");
    } else if (temperature_avg >= TEMP_HEATER_THRESHOLD + 2 && heaterActive) {
        digitalWrite(HEATER_PIN, LOW);
        heaterActive = false;
        Serial.println("🔥 Calefactor: DESACTIVADO");
    }
    
    // Control del LED RGB según condiciones
    if (temperature_avg > 35 || uvIndex > 8) {
        IndicateStatus("warning");  // Naranja: condiciones extremas
    } else if (airQuality_avg > AQI_DANGEROUS) {
        SetLED(128, 0, 128);  // Morado: mala calidad del aire
    } else if (humidity_avg > HUMIDITY_HIGH) {
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
    data["temperature_celsius"] = round(temperature_avg * 10) / 10.0;
    data["humidity_percent"] = round(humidity_avg * 10) / 10.0;
    data["air_quality_index"] = airQuality_avg;
    data["wind_speed_kmh"] = round(windSpeed * 10) / 10.0;
    data["wind_direction_degrees"] = windDirection;
    data["atmospheric_pressure_hpa"] = round(pressure_avg * 10) / 10.0;
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