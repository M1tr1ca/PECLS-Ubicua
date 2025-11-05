/*
 * =====================================================
 * ESTACIÓN METEOROLÓGICA IoT - CIUDAD 4.0
 * UAH - PECL1 - Computación Ubicua
 * =====================================================
 * 
 * Proyecto: PECL1 - Computación Ubicua
 * Dispositivo: Estación Meteorológica
 * Ubicación: Alcalá de Henares, Centro
 * Tipo: Weather Station (weather)
 * Autores: Grupo 5 - Juan Pérez Resa y David Nicolás Mitrica
 * 
 * Descripción:
 * Este dispositivo IoT captura datos meteorológicos en tiempo real
 * y los envía a un broker MQTT siguiendo el formato JSON especificado
 * y recibe comandos para controlar los actuadores y la esp32.
 * 
 * Sensores implementados (1):
 * - BME280: Temperatura, Humedad y Presión Atmosférica (3 en 1)
 * 
 * Actuadores implementados (2):
 * - LEDs Alerta: Indicador visual de alertas (temperatura alta) o información que nos manden desde el broker MQTT
 * - Display 7 segmentos: Visualización de datos en tiempo real
 * 
 * Total: 3 componentes (1 sensor + 2 actuadores)
 * =====================================================
 */

#include <WiFi.h>   
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include  <Wire.h> //Incluye protocolo de comunicación I^2C, el sensor transmitirá los datos en 1's y 0's (SDA), sincronizados con un reloj (SCL)
#include <Adafruit_BME280.h> //Librería que facilita el manejo del sensor (inicializa el sensor, lee datos, fórmulas de calibración...)

#include "config.h"
#include "ESP32_UTILS.hpp"
#include <sys/time.h>
#include "time.h"
#include "ESP32_Utils_MQTT.hpp"

Adafruit_BME280 sensor_bme280; //Creamos una instancia del objeto para el sensor


// ============================================
// CONFIGURACIÓN DISPLAY 7 SEGMENTOS MANUAL
// ============================================
// Tabla de segmentos para cada dígito (0-9)
// Orden: A, B, C, D, E, F, G
// 1 = encendido, 0 = apagado
const byte DIGIT_PATTERNS[10][7] = {
  {0, 0, 0, 0, 0, 0, 1}, // 0
  {1, 0, 0, 1, 1, 1, 1}, // 1
  {0, 0, 1, 0, 0, 1, 0}, // 2
  {0, 0, 0, 0, 1, 1, 0}, // 3
  {1, 0, 0, 1, 1, 0, 0}, // 4
  {0, 1, 0, 0, 1, 0, 0}, // 5
  {0, 1, 0, 0, 0, 0, 0}, // 6
  {0, 0, 0, 1, 1, 1, 1}, // 7
  {0, 0, 0, 0, 0, 0, 0}, // 8
  {0, 0, 0, 0, 1, 0, 0}  // 9
};

// Array con los pines de los segmentos
const int SEGMENT_PINS[7] = {
  DISPLAY_A, DISPLAY_B, DISPLAY_C, DISPLAY_D, 
  DISPLAY_E, DISPLAY_F, DISPLAY_G
};


struct tm timeinfo; //Utilizado para almacenar fecha y hora
struct timeval tv; // Contiene el tiempo en segundos y precisión en microsegundos
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


// Estados de sensores
bool bme_available = false;

// ============================================
// FUNCIONES DISPLAY 7 SEGMENTOS MANUAL
// ============================================

/**
 * Inicializa los pines del display de 7 segmentos
 */
void InitDisplay() {
    // Configurar todos los pines de segmentos como salida
    for (int i = 0; i < 7; i++) {
        pinMode(SEGMENT_PINS[i], OUTPUT);
        digitalWrite(SEGMENT_PINS[i], LOW); // Apagar todos los segmentos (cátodo común)
    }
    
    // Mostrar 0 inicialmente
    DisplayNumber(0);
    
    Serial.println("Display de 7 segmentos inicializado (manual)");
}

/**
 * Muestra un número (0-9) en el display de 7 segmentos
 * @param number: Número a mostrar (0-9). Si está fuera de rango, apaga el display
 */
void DisplayNumber(int number) {
    // Validar que el número esté en el rango 0-9
    if (number < 0 || number > 9) {
        // Apagar todos los segmentos si el número es inválido
        for (int i = 0; i < 7; i++) {
            digitalWrite(SEGMENT_PINS[i], LOW);
        }
        return;
    }
    
    // Activar/desactivar cada segmento según el patrón del dígito
    // Para display de CÁTODO COMÚN: HIGH = encendido, LOW = apagado
    for (int i = 0; i < 7; i++) {
        digitalWrite(SEGMENT_PINS[i], DIGIT_PATTERNS[number][i]);
    }
}

/**
 * Apaga completamente el display
 */
void DisplayOff() {
    for (int i = 0; i < 7; i++) {
        digitalWrite(SEGMENT_PINS[i], LOW);
    }
}

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
/**
 * Calcula la altitud basándose en la presión atmosférica
 */
float ReadAltitude(){
    altitude = sensor_bme280.readAltitude(1013.25); // 1013.25 es la presión barométrica a nivel del mar por defecto
    return altitude;//Si el sensor se desconectase, mandaría un valor NaN, o similar, hay que comprobarlo cuando recibe este valor
}


/**
 * Lee todos los sensores
 */
void ReadAllSensors() {
    Serial.println("");
    Serial.println("===========================================");
    Serial.println(" Leyendo sensores...");
    Serial.println("===========================================");
    
    // Leer sensores BME280
    temperature = ReadTemperature();
    humidity = ReadHumidity();
    pressure = ReadPressure();
    altitude = ReadAltitude();
    
    
    Serial.println("Lecturas de sensores:");
    Serial.printf("   Temperatura: %.1f°C\n", temperature);
    Serial.printf("   Humedad: %.1f%%\n", humidity);
    Serial.printf("   Presión: %.1f hPa\n", pressure);
    Serial.printf("      Altitud: %.1f m\n", altitude);
    Serial.println("===========================================");

    Serial.println("EL JSON es el siguiente: ");
    Serial.println(CreateJSONMessage());
}

// ============================================
// CONTROL DE ACTUADORES
// ============================================

/**
 * Controla los actuadores basándose en las lecturas
 */
 
void ControlActuators() {
    // Control del LED rojo según condiciones
    if (temperature > TEMP_HIGH || humidity > HUMIDITY_HIGH) {
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
    

    // Información básica de la estación (usando valores de config.h)
    doc["sensor_id"] = SENSOR_ID;
    doc["sensor_type"] = SENSOR_TYPE;
    doc["street_id"] = STREET_ID;
    
    // Timestamp (formato ISO 8601 con Z al final)
    //Es más eficiente trabajar con direcciones de memoria
    getLocalTime(&timeinfo); //Rellena estructura timeinfo con la hora del sistema
    gettimeofday(&tv, NULL); //Rellena estructura tv con timestamp de horas y milisegundos
    int millisPart = tv.tv_usec / 1000;

// 1. Define un buffer (array de caracteres) donde se guardará la cadena formateada.
char timeBuffer[30]; 

// 2. Usa snprintf para construir la cadena formateada y guardarla en el buffer.
// snprintf(destino, tamaño, formato, argumentos...)
snprintf(timeBuffer, sizeof(timeBuffer), 
         "%04d-%02d-%02d %02d:%02d:%02d:%03d",
         timeinfo.tm_year + 1900,  // Año
         timeinfo.tm_mon + 1,      // Mes
         timeinfo.tm_mday,         // Día
         timeinfo.tm_hour,         // Hora
         timeinfo.tm_min,          // Minuto
         timeinfo.tm_sec,          // Segundo
         millisPart);              // Milisegundos

// 3. Asigna la cadena del buffer a tu documento.
doc["timestamp"] = timeBuffer;           // Milisegundos
    
    // Ubicación (valores únicos, no rangos)
    JsonObject location = doc.createNestedObject("location");
    location["latitude_start"] = LATITUDE_START;
    location["latitude_end"] = LATITUDE_END;
    location["longitude_start"] = LONGITUDE_START;
    location["longitude_end"] = LONGITUDE_END;
    location["altitude_meters"] = round(ReadAltitude() * 10) / 10.0;
    location["district"] = DISTRICT;
    location["neighborhood"] = NEIGHBORHOOD;
    
    // Datos meteorológicos
    JsonObject data = doc.createNestedObject("data");
    data["temperature_celsius"] = round(ReadTemperature() * 10) / 10.0;
    data["humidity_percent"] = round(ReadHumidity() * 10) / 10.0;
    data["atmospheric_pressure_hpa"] = round(ReadPressure() * 10) / 10.0;
        
    // Serializar a String
    String jsonString;
    serializeJson(doc, jsonString);
    
    return jsonString;
}

// ============================================
// PUBLICACIÓN DE DATOS MQTT
// ============================================
void PublishData() {
    if (!IsMQTTConnected()) {
        Serial.println("MQTT no conectado. Saltando publicación.");
        return;
    }
    
    String jsonMessage = CreateJSONMessage();
    
    Serial.println("");
    Serial.println("===========================================");
    Serial.println(" Publicando datos...");
    Serial.println("===========================================");
    Serial.println("JSON generado:");
    Serial.println(jsonMessage);
    Serial.println("-------------------------------------------");
    
    PublishMQTT(jsonMessage);
    
    messageCount++;
    Serial.print("Mensaje #");
    Serial.print(messageCount);
    Serial.println(" enviado");
    Serial.println("===========================================");
}

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
    
    // Configurar pines de actuadores
    pinMode(LED_RED_PIN, OUTPUT);

    //Configurar pines de actuadores de alarma
    pinMode(LED_ALARM_PIN_1, OUTPUT);
    pinMode(LED_ALARM_PIN_2, OUTPUT);
    pinMode(LED_ALARM_PIN_3, OUTPUT);

    digitalWrite(LED_ALARM_PIN_1, LOW);
    digitalWrite(LED_ALARM_PIN_2, LOW);
    digitalWrite(LED_ALARM_PIN_3, LOW);

    // Inicializar display de 7 segmentos
    InitDisplay();

    // Conectar a WiFi
    ConnectWifi_STA(false); //False indica que no queremos una dirección IP estática

    configTime(3600, 0, "pool.ntp.org"); //Introducimos una vez la hora en el sistema desde un servidor, después, el esp32 lleva la cuenta
    while(!getLocalTime(&timeinfo)){
        Serial.println("Esperando sincronización NTP para la hora...");
        delay(500);
    }
    Serial.println("Hora sincronizada");

    // Inicializar sensores
    InitSensors();
    
    // Inicializar MQTT
    InitMQTT();

    // Conectar a MQTT
    ConnectMQTT();

    // Control inicial de actuadores
    ControlActuators();
    
    Serial.println("");
    Serial.println("Estación lista para operar");
    Serial.println("");
    ReadAllSensors();
    ControlActuators();
    PublishData();
}

void loop() {
    // Mantener conexiones activas
    CheckWiFiConnection();
    HandleMQTT();
    
    // Leer sensores cada 5 segundos (sin bloquear)
    // [x] : ponemos lo de millis ya que el delay congela todo el programa, inlcuido la parte de wifi y mqtt
    if (millis() - lastReadingTime >= READING_INTERVAL) {  // Cada 30 segundos
        ReadAllSensors();
        ControlActuators();
        PublishData();
        lastReadingTime = millis();
    }
    
    // Pequeña pausa para no saturar el loop
    delay(100);  // Solo 100ms
}