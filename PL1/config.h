
// CONFIGURACIÓN WIFI
// ============================================
const char* ssid = "cubicuz";                    // Nombre de tu red WiFi
const char* password = "estoesesparta";          // Contraseña de tu red WiFi
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
// BME280 - Sensor I2C (temperatura, humedad, presión)
#define BME_SDA 21          // BME280 I2C SDA
#define BME_SCL 22          // BME280 I2C SCL
#define BME280_ADDRESS 0x76 // Dirección I2C del BME280 (por defecto)

// MQ-135 - Sensor analógico de calidad del aire (CO2, NH3, NOx, alcohol, benceno, humo)
#define MQ135_PIN 34        // Pin analógico para sensor MQ-135

// ============================================
// CONFIGURACIÓN DE PINES - ACTUADORES
// ============================================
#define LED_RED_PIN 25      // LED indicador de estado (alertas y condiciones anormales)

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

// Configuración MQ-135 para cálculo de CAQI
#define MQ135_RL 10.0              // Resistencia de carga en kΩ
#define MQ135_RO_CLEAN_AIR 3.6     // Ratio Rs/Ro en aire limpio (condiciones de calibración)
