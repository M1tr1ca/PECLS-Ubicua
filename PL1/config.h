#ifndef CONFIG_H
#define CONFIG_H
// CONFIGURACIÓN WIFI
// ============================================
const char* ssid = "iPhone";                    // Nombre de tu red WiFi
const char* password = "12345678";          // Contraseña de tu red WiFi
const char* hostname = "cubicua";           //Establece nombre del ESP32 en la red
IPAddress ip(192, 168, 1, 200);
IPAddress gateway(192, 168, 1, 1);
IPAddress subnet(255, 255, 255, 0);

// ============================================
// CONFIGURACIÓN MQTT BROKER
// ============================================
const char* MQTT_BROKER_ADRESS = "172.20.10.11";  // Broker MQTT público
const uint16_t MQTT_PORT = 1883;
const char* MQTT_CLIENT_NAME = "LAB08JAV-G5";

// Tópicos MQTT
const char* TOPIC_PUBLISH = "uah/alcala/weather/data";
const char* TOPIC_SUBSCRIBE = "uah/alcala/weather/control";

// ============================================
// IDENTIFICACIÓN DE LA ESTACIÓN
// ============================================
const char* SENSOR_ID = "ST_1617";
const char* SENSOR_TYPE = "Estación metereológica IoT";
const char* STREET_ID = "ST_1617";
const char* STREET_NAME = "Calle Pepe Hillo";

// Ubicación: Hortaleza, Madrid
const float LATITUDE = 40.4513367;
const float LONGITUDE = -3.6409307;
const float LATITUDE_END = 40.4515721;
const float LENGTH_METERS = 150.84;
const char* DISTRICT = "Hortaleza";
const char* NEIGHBORHOOD = "Hortaleza";
const char* POSTAL_CODE = "28033";
const char* SURFACE_TYPE = "asphalt";
const int MAX_SPEED_KMH = 30;
const bool IS_BIDIRECTIONAL = true;

// ============================================
// CONFIGURACIÓN DE PINES - SENSORES
// ============================================
// BME280 - Sensor I2C (temperatura, humedad, presión)
#define BME_SDA 21          // BME280 I2C SDA
#define BME_SCL 22          // BME280 I2C SCL
#define BME280_ADDRESS 0x76 // Dirección I2C del BME280 (por defecto)



// ============================================
// CONFIGURACIÓN DE PINES - ACTUADORES
// ============================================
#define LED_RED_PIN 12      // LED indicador de estado (alertas y condiciones anormales)

// ============================================
// CONFIGURACIÓN DE PINES - ACTUADORES ALARMA
// ============================================

#define LED_ALARM_PIN_1 4    // LED de alarma (alertas y condiciones anormales)
#define LED_ALARM_PIN_2 5    // LED de alarma (alertas y condiciones anormales)
#define LED_ALARM_PIN_3 13    // LED de alarma (alertas y condiciones anormales)

// ============================================
// CONFIGURACIÓN DISPLAY 7 SEGMENTOS SA52-11EWA
// ============================================
#define DISPLAY_A 26      // Segmento A *
#define DISPLAY_B 25      // Segmento B *
#define DISPLAY_C 19      // Segmento C *
#define DISPLAY_D 18      // Segmento D *
#define DISPLAY_E 23      // Segmento E (CAMBIADO: GPIO 35 es solo entrada) *
#define DISPLAY_F 27      // Segmento F *
#define DISPLAY_G 14      // Segmento G *

// ============================================
// CONFIGURACIÓN DEL SISTEMA
// ============================================
#define READING_INTERVAL 30000  // Intervalo de lectura en ms (30 segundos)

// Umbrales de alertas
#define TEMP_HIGH 35.0              // Temperatura alta (°C) para activar alerta LED
#define HUMIDITY_HIGH 80.0          // Umbral de humedad alta (%) para activar alerta LED
#define CAQI_DANGEROUS 75           // CAQI peligroso (nivel medio-alto, escala 0-150)

// Configuración BME280
#define SEALEVELPRESSURE_HPA (1013.25)  // Presión a nivel del mar para cálculo de altitud

#endif