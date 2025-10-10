// ============================================
// CONFIGURACIÓN WIFI
// ============================================
const char* ssid = "ssid";                    // Cambia por tu red WiFi
const char* password = "estoesesparta";       // Cambia por tu contraseña
const char* hostname = "cubicua";
IPAddress ip(192, 168, 1, 200);
IPAddress gateway(192, 168, 1, 1);
IPAddress subnet(255, 255, 255, 0);

// ============================================
// CONFIGURACIÓN MQTT BROKER
// ============================================
const char* MQTT_BROKER_ADRESS = "test.mosquitto.org";  // Broker MQTT público
const uint16_t MQTT_PORT = 1883;
const char* MQTT_CLIENT_NAME = "ESP32_WS_ALC_01";

// Tópicos MQTT
const char* TOPIC_PUBLISH = "uah/alcala/weather/data";
const char* TOPIC_SUBSCRIBE = "uah/alcala/weather/control";

// ============================================
// IDENTIFICACIÓN DE LA ESTACIÓN
// ============================================
const char* SENSOR_ID = "WS_ALC_01";
const char* SENSOR_TYPE = "weather";
const char* STREET_ID = "ST_ALC_001";

// Ubicación: Alcalá de Henares
const float LATITUDE = 40.4823;
const float LONGITUDE = -3.3618;
const float ALTITUDE = 588.0;
const char* DISTRICT = "Alcalá de Henares";
const char* NEIGHBORHOOD = "Centro";

// ============================================
// CONFIGURACIÓN DE PINES - SENSORES
// ============================================
// BME280 - Sensores I2C (2 unidades)
#define BME_SDA 21          // BME280 I2C SDA (ambos sensores en mismo bus)
#define BME_SCL 22          // BME280 I2C SCL (ambos sensores en mismo bus)
// Direcciones I2C de los BME280
#define BME280_ADDRESS_1 0x76  // BME280 #1 (dirección por defecto)
#define BME280_ADDRESS_2 0x77  // BME280 #2 (dirección alternativa)

// MQ-135 - Sensores analógicos de calidad del aire (3 unidades)
#define MQ135_PIN_1 34      // MQ-135 sensor #1
#define MQ135_PIN_2 35      // MQ-135 sensor #2
#define MQ135_PIN_3 39      // MQ-135 sensor #3

// Sensor UV simulado (opcional, para completar datos meteorológicos)
#define UV_SENSOR_PIN 36    // Sensor UV analógico (opcional)

// ============================================
// CONFIGURACIÓN DE PINES - ACTUADORES
// ============================================
#define LED_RED_PIN 25      // LED RGB - Rojo
#define LED_GREEN_PIN 26    // LED RGB - Verde
#define LED_BLUE_PIN 27     // LED RGB - Azul
#define FAN_PIN 32          // Ventilador de enfriamiento
#define HEATER_PIN 33       // Calefactor

// ============================================
// CONFIGURACIÓN DEL SISTEMA
// ============================================
#define READING_INTERVAL 30000  // Intervalo de lectura en ms (30 segundos)

// Umbrales de actuadores
#define TEMP_FAN_THRESHOLD 30.0     // Activar ventilador si temp > 30°C
#define TEMP_HEATER_THRESHOLD 10.0  // Activar calefactor si temp < 10°C
#define HUMIDITY_HIGH 80.0          // Umbral de humedad alta
#define UV_HIGH 6                   // Índice UV alto
#define AQI_DANGEROUS 150           // AQI peligroso

// Configuración BME280
#define SEALEVELPRESSURE_HPA (1013.25)  // Presión a nivel del mar para cálculo de altitud

// Configuración MQ-135
#define MQ135_RL 10.0              // Resistencia de carga en kΩ
#define MQ135_RO_CLEAN_AIR 3.6     // Ratio Ro en aire limpio
