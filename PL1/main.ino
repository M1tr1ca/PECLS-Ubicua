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
//FIXME: Descomentar en algún momento 
/**
#include "ESP32_Utils_MQTT.hpp"
*/
#include <MQUnifiedsensor.h>


#define placa "ESP_32" //Usado para MQUnifiedsensor
#define Voltage_Resolution 3.3
#define pin 34  // Pin analógico del ESP32
#define type "MQ-135"
#define ADC_Bit_Resolution 12
#define RatioMQ135CleanAir 3.6
Adafruit_BME280 sensor_bme280; //Creamos una instancia del objeto para el sensor
MQUnifiedsensor MQ135(placa, Voltage_Resolution, ADC_Bit_Resolution, pin, type);

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
float airQuality = 0;            // Asumimos que el MQ-135 detecta CO_2. MQ-135 es capaz de detectar distintos tipos de gases, pero no diferencia cuál es cuál. Lo calculamos asumiendo que todo es CO_2.

// Estados de sensores
bool bme_available = false;
bool mq135_available = false;

// ============================================
// FUNCIONES DE CONFIGURACIÓN
// ============================================

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

    MQ135.setRegressionMethod(1); // _PPM = a*ratio^b
    MQ135.setA(110.47);   // Coeficiente A para CO₂
    MQ135.setB(-2.862);   // Coeficiente B para CO₂
  
    MQ135.init();
  
    // Precalentamiento recomendado
    Serial.println("Precalentando sensor MQ-135 (espera 2-3 minutos mínimo)...");
    delay(30000); // 30 segundos de espera inicial
  
  Serial.print("Calibrando sensor MQ-135 en aire exterior");
  float calcR0 = 0;
  for(int i = 1; i <= 10; i++) {
    MQ135.update();
    calcR0 += MQ135.calibrate(RatioMQ135CleanAir);
    Serial.print(".");
    delay(500); // Pausa entre lecturas
  }
  MQ135.setR0(calcR0/10);
  
  // Verificar problemas de conexión
  if (isinf(calcR0)) {
    Serial.println("\nWarning: Connection issue found, R0 is infinite (Open circuit detected) please check your wiring and supply");
    while (1);
  }
  if (calcR0 == 0) {
    Serial.println("\nWarning: Connection issue found, R0 is zero (Analog pin with short circuit to ground) please check your wiring and supply");
    while (1);
  }
  
  Serial.println("\nCalibración completa!");
  Serial.print("R0 calculado: ");
  Serial.println(calcR0);
  Serial.println("-----------------------------------");
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

float ReadAirQuality(){
    airQuality = MQ135.readSensor();
    return airQuality;
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
    Serial.printf("  🏭 Calidad del Aire (CAQI): %.1f ppm\n", airQuality);
    Serial.println("===========================================");

    Serial.println("EL JSON es el siguiente: ");
    Serial.println(CreateJSONMessage());
}

// ============================================
// CREACIÓN Y ENVÍO DE MENSAJES JSON
// ============================================
/** 
 * Crea el mensaje JSON según el formato especificado
 */
String CreateJSONMessage() {
    DynamicJsonDocument doc(1024);
    
    // Información básica de la estación (usando valores de config.h)
    doc["sensor_id"] = SENSOR_ID;
    doc["sensor_type"] = SENSOR_TYPE;
    doc["street_id"] = STREET_ID;
    
    //FIXME: Cuando sepamos cómo conectarse al WiFi, coger hora a través de la red (NTP)
    // Timestamp (formato ISO 8601 con Z al final)
    char timestamp[30];
    unsigned long currentTime = millis();
    sprintf(timestamp, "2025-10-%02dT%02d:%02d:%02d.%03luZ",
            (int)(currentTime / 86400000) % 30 + 1,  // Día
            (int)(currentTime / 3600000) % 24,        // Hora
            (int)(currentTime / 60000) % 60,          // Minuto
            (int)(currentTime / 1000) % 60,           // Segundo
            currentTime % 1000);                       // Milisegundo
    doc["timestamp"] = timestamp;
    
    // Ubicación (valores únicos, no rangos)
    JsonObject location = doc.createNestedObject("location");
    location["latitude"] = LATITUDE;
    location["longitude"] = LONGITUDE;
    location["altitude_meters"] = round(ReadAltitude() * 10) / 10.0;
    location["district"] = DISTRICT;
    location["neighborhood"] = NEIGHBORHOOD;
    
    // Datos meteorológicos
    JsonObject data = doc.createNestedObject("data");
    data["temperature_celsius"] = round(ReadTemperature() * 10) / 10.0;
    data["humidity_percent"] = round(ReadHumidity() * 10) / 10.0;
    data["air_quality_index"] = round(ReadAirQuality() * 10) / 10.0;
    data["atmospheric_pressure_hpa"] = round(ReadPressure() * 10) / 10.0;
    
    // Campos opcionales (sin sensor físico, valores por defecto)
    data["wind_speed_kmh"] = 0.0;           // Sin sensor de viento
    data["wind_direction_degrees"] = 0;     // Sin sensor de viento
    data["uv_index"] = 0;                   // Sin sensor UV
    
    // Serializar a String
    String jsonString;
    serializeJson(doc, jsonString);
    
    return jsonString;
}

/**
 * Publica los datos en MQTT
 
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
*/
// ============================================
// SETUP Y LOOP
// ============================================

void setup() {
    Serial.begin(115200);

    
    Serial.println("");
    Serial.println("═══════════════════════════════════════════");
    Serial.println("  ESTACIÓN METEOROLÓGICA IoT");
    Serial.println("  Universidad de Alcalá de Henares");
    Serial.println("  PECL1 - Computación Ubicua");
    Serial.println("═══════════════════════════════════════════");
    Serial.println("");
    
    // Conectar a WiFi
    ConnectWifi_STA(false);

    InitSensors();
    
    // Inicializar MQTT
    //InitMQTT();
    //ConnectMQTT();
    
    
    Serial.println("");
    Serial.println("✓ Estación lista para operar");
    Serial.println("");
}

void loop() {
    // Mantener conexiones activas
    CheckWiFiConnection();
    //HandleMQTT();
    
    // Leer sensores cada 5 segundos (sin bloquear)
    // [x] : ponemos lo de millis ya que el delay congela todo el programa, inlcuido la parte de wifi y mqtt
    if (millis() - lastReadingTime >= 5000) {  // 5000ms = 5 segundos
        ReadAllSensors();
        //PublishData();
        lastReadingTime = millis();
    }
    
    // Pequeña pausa para no saturar el loop (no bloqueante)
    delay(100);  // Solo 100ms
}