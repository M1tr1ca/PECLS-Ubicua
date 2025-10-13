# 🏗️ Arquitectura del Sistema - Estación Meteorológica IoT

## 📐 Visión General de la Arquitectura

El sistema está diseñado con una arquitectura modular de tres capas:

1. **Capa de Sensores** - Adquisición de datos físicos
2. **Capa de Procesamiento** - ESP32 con lógica de control
3. **Capa de Comunicación** - Transmisión MQTT a la nube

```
┌─────────────────────────────────────────────────────────┐
│                  CAPA DE COMUNICACIÓN                    │
│  ┌────────────────────────────────────────────────┐     │
│  │        MQTT Broker (test.mosquitto.org)        │     │
│  │           Tópico: uah/alcala/weather/*         │     │
│  └────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────┘
                            ▲
                            │ WiFi (802.11)
                            │ JSON/MQTT
                            ▼
┌─────────────────────────────────────────────────────────┐
│              CAPA DE PROCESAMIENTO (ESP32)               │
│  ┌─────────────────────────────────────────────────┐    │
│  │  • Control de flujo principal (main.ino)        │    │
│  │  • Gestión WiFi (ESP32_UTILS.hpp)              │    │
│  │  • Gestión MQTT (ESP32_Utils_MQTT.hpp)         │    │
│  │  • Procesamiento de datos                       │    │
│  │  • Cálculo de CAQI                              │    │
│  │  • Generación de JSON                           │    │
│  │  • Control de actuadores                        │    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
                            ▲
                            │ I2C / Analógico
                            │
┌─────────────────────────────────────────────────────────┐
│                   CAPA DE SENSORES                       │
│  ┌──────────────────┐  ┌──────────────────┐            │
│  │     BME280       │  │     MQ-135       │   LED Rojo  │
│  │  (I2C: 0x76)     │  │   (Analógico)    │      🔴      │
│  │ • Temperatura    │  │ • Calidad Aire   │  (GPIO 25)  │
│  │ • Humedad        │  │ • Gases CO2,NH3  │             │
│  │ • Presión        │  │ • CAQI 0-150     │             │
│  └──────────────────┘  └──────────────────┘             │
└─────────────────────────────────────────────────────────┘
```

## 🔌 Diagrama de Conexiones Hardware

### ESP32 - Asignación de Pines

| Componente | Pin ESP32 | Tipo | Función |
|------------|-----------|------|---------|
| BME280 SDA | GPIO 21 | I2C | Datos I2C |
| BME280 SCL | GPIO 22 | I2C | Clock I2C |
| MQ-135 | GPIO 34 | Analógico | Lectura ADC |
| LED Rojo | GPIO 25 | Digital Out | Alerta visual |

### Esquema de Conexión BME280

```
ESP32                    BME280
┌─────────┐           ┌──────────┐
│         │           │          │
│  3.3V   ├──────────►│   VCC    │
│         │           │          │
│  GND    ├──────────►│   GND    │
│         │           │          │
│  GPIO21 ├─────────►│   SDA    │ (Pull-up 4.7kΩ)
│  (SDA)  │           │          │
│         │           │          │
│  GPIO22 ├─────────►│   SCL    │ (Pull-up 4.7kΩ)
│  (SCL)  │           │          │
└─────────┘           └──────────┘

Dirección I2C: 0x76 (por defecto)
```

### Esquema de Conexión MQ-135

```
ESP32                    MQ-135
┌─────────┐           ┌──────────┐
│         │           │          │
│  5V     ├──────────►│   VCC    │
│         │           │          │
│  GND    ├──────────►│   GND    │
│         │           │          │
│  GPIO34 ├─────────►│   AOUT   │
│  (ADC)  │           │          │
└─────────┘           └──────────┘

ADC: 12 bits (0-4095)
Voltaje: 0-3.3V
Resistencia de carga (RL): 10kΩ
```

### Esquema de Conexión LED

```
ESP32                    LED Rojo
┌─────────┐           ┌──────────┐
│         │           │          │
│  GPIO25 ├───[220Ω]──►│   (+)    │
│         │           │          │
│  GND    ├───────────►│   (-)    │
│         │           │          │
└─────────┘           └──────────┘
```

## 🧩 Componentes Software

### 1. main.ino - Programa Principal

**Responsabilidades:**
- Inicialización del sistema
- Bucle principal de lectura y publicación
- Orquestación de componentes
- Control de actuadores

**Funciones principales:**
```cpp
void setup()                    // Inicialización completa del sistema
void loop()                     // Bucle principal de ejecución
void InitPins()                 // Configuración de pines GPIO
void InitSensors()              // Inicialización de sensores
void ReadAllSensors()           // Lectura de todos los sensores
void ControlActuators()         // Control de LED según condiciones
String CreateJSONMessage()      // Generación de mensaje JSON
void PublishData()              // Publicación MQTT
```

### 2. ESP32_UTILS.hpp - Gestión WiFi

**Responsabilidades:**
- Conexión a red WiFi
- Gestión de reconexión automática
- Manejo de eventos WiFi
- Configuración de red (DHCP/Estática)

**Funciones principales:**
```cpp
void ConnectWifi_STA(bool useStaticIP)  // Conecta como estación WiFi
void ConnectWifi_AP(bool useStaticIP)   // Modo punto de acceso
void CheckWiFiConnection()               // Verifica conexión WiFi
void WiFiEvent(WiFiEvent_t event)       // Manejador de eventos
```

### 3. ESP32_Utils_MQTT.hpp - Gestión MQTT

**Responsabilidades:**
- Conexión al broker MQTT
- Publicación de datos
- Suscripción a tópicos de control
- Procesamiento de comandos remotos

**Funciones principales:**
```cpp
void InitMQTT()                          // Inicializa cliente MQTT
void ConnectMQTT()                       // Conecta al broker
void OnMqttReceived(...)                 // Callback de mensajes
void PublishMQTT(String jsonMessage)     // Publica mensaje
void HandleMQTT()                        // Mantiene conexión activa
bool IsMQTTConnected()                   // Verifica estado
```

### 4. config.h - Configuración del Sistema

**Contiene:**
- Credenciales WiFi (SSID, contraseña)
- Configuración de red (IP, gateway, subnet)
- Parámetros MQTT (broker, puerto, tópicos)
- Identificación del dispositivo
- Pines GPIO
- Umbrales de alertas
- Constantes del sistema

### 5. config.json - Parámetros en JSON

**Estructura:**
```json
{
  "mqtt": { /* Configuración MQTT */ },
  "station": { /* Identificación y ubicación */ },
  "sensors": { /* Rangos y intervalos */ },
  "actuators": { /* Configuración de alertas */ }
}
```

## 🔄 Flujo de Datos

### Ciclo Completo de Operación

```
1. INICIALIZACIÓN
   ├── Configurar pines GPIO
   ├── Conectar WiFi
   ├── Conectar MQTT
   └── Inicializar sensores

2. LECTURA (cada 30 segundos)
   ├── Leer BME280 (Temp, Hum, Presión)
   ├── Leer MQ-135 (Calidad del aire)
   └── Validar datos

3. PROCESAMIENTO
   ├── Calcular CAQI desde MQ-135
   ├── Verificar umbrales de alerta
   └── Generar JSON

4. ACTUACIÓN
   ├── Evaluar condiciones (Temp, Hum, CAQI)
   └── Controlar LED Rojo

5. TRANSMISIÓN
   ├── Publicar JSON en MQTT
   └── Esperar siguiente ciclo

6. MONITOREO CONTINUO
   ├── Verificar WiFi
   ├── Verificar MQTT
   └── Escuchar comandos remotos
```

## 📊 Algoritmo de Cálculo CAQI

### Common Air Quality Index (CAQI)

El sensor MQ-135 mide concentraciones de gases, que se convierten a CAQI:

```cpp
// 1. Lectura analógica (0-4095)
int sensorValue = analogRead(MQ135_PIN);

// 2. Conversión a voltaje (0-3.3V)
float voltage = (sensorValue / 4095.0) * 3.3;

// 3. Cálculo de resistencia del sensor
float Rs = ((3.3 * RL) / voltage) - RL;

// 4. Ratio Rs/Ro (calibración en aire limpio)
float ratio = Rs / RO_CLEAN_AIR;

// 5. Concentración de CO2 en ppm
float ppm = 116.6020682 * pow(ratio, -2.769034857);

// 6. Conversión a CAQI (0-150)
if (ppm <= 600)       caqi = map(ppm, 0, 600, 0, 25);      // Muy bajo
else if (ppm <= 800)  caqi = map(ppm, 600, 800, 26, 50);   // Bajo
else if (ppm <= 1000) caqi = map(ppm, 800, 1000, 51, 75);  // Medio
else if (ppm <= 1500) caqi = map(ppm, 1000, 1500, 76, 100);// Alto
else                  caqi = map(ppm, 1500, 5000, 101, 150);// Muy alto
```

### Escala CAQI

| CAQI | Calidad | Color | Acción |
|------|---------|-------|--------|
| 0-25 | Muy Buena | 🟢 Verde | Normal |
| 26-50 | Buena | 🟡 Amarillo | Normal |
| 51-75 | Media | 🟠 Naranja | Precaución |
| 76-100 | Mala | 🔴 Rojo | Alerta LED ON |
| >100 | Muy Mala | 🟣 Morado | Alerta LED ON |

## 🎯 Sistema de Alertas

### Condiciones de Activación del LED

```cpp
void ControlActuators() {
    bool alerta = false;
    
    // Condición 1: Temperatura alta
    if (temperature > 35.0) {
        alerta = true;
        Serial.println("⚠ ALERTA: Temperatura alta");
    }
    
    // Condición 2: Humedad alta
    if (humidity > 80.0) {
        alerta = true;
        Serial.println("⚠ ALERTA: Humedad alta");
    }
    
    // Condición 3: CAQI peligroso
    if (airQuality > 75) {
        alerta = true;
        Serial.println("⚠ ALERTA: Calidad del aire peligrosa");
    }
    
    digitalWrite(LED_RED_PIN, alerta ? HIGH : LOW);
}
```

### Tabla de Umbrales

| Parámetro | Umbral | Acción |
|-----------|--------|--------|
| Temperatura | >35°C | LED ON |
| Humedad | >80% | LED ON |
| CAQI | >75 | LED ON |
| WiFi Desconectado | - | Reconexión auto |
| MQTT Desconectado | - | Reconexión auto |

## 🔐 Seguridad y Fiabilidad

### Validación de Datos

```cpp
// Temperatura: rango válido -40°C a 85°C
if (!isnan(temp) && temp > -40 && temp < 85) {
    return temp;
}

// Humedad: rango válido 0% a 100%
if (!isnan(hum) && hum >= 0 && hum <= 100) {
    return hum;
}

// Presión: rango válido 800 hPa a 1200 hPa
if (!isnan(press) && press > 800 && press < 1200) {
    return press;
}
```

### Manejo de Errores

- **Sensor no disponible**: Retorna valor de error (-999.0, -1.0, -1)
- **Lectura inválida**: Log en Serial y valor de error
- **WiFi desconectado**: Reconexión automática
- **MQTT desconectado**: Reintentos cada 5 segundos
- **Buffer MQTT**: 1024 bytes para mensajes JSON grandes

## 📈 Optimizaciones Implementadas

### 1. BME280 - Configuración de Muestreo

```cpp
bme.setSampling(
    Adafruit_BME280::MODE_NORMAL,      // Modo continuo
    Adafruit_BME280::SAMPLING_X2,      // Temp: 2x oversampling
    Adafruit_BME280::SAMPLING_X16,     // Presión: 16x oversampling
    Adafruit_BME280::SAMPLING_X1,      // Humedad: 1x
    Adafruit_BME280::FILTER_X16,       // Filtro digital 16x
    Adafruit_BME280::STANDBY_MS_500    // Standby 500ms
);
```

**Beneficios:**
- Reducción de ruido en lecturas
- Mayor precisión en presión (16x oversampling)
- Filtrado digital para estabilidad
- Balance entre precisión y consumo

### 2. Control de Temporización

```cpp
#define READING_INTERVAL 30000  // 30 segundos

// En loop()
if (millis() - lastReadingTime >= READING_INTERVAL) {
    ReadAllSensors();
    ControlActuators();
    PublishData();
    lastReadingTime = millis();
}
```

**Beneficios:**
- Evita lecturas innecesarias
- Reduce tráfico MQTT
- Optimiza consumo de energía
- Permite tiempo de estabilización

### 3. Buffer MQTT Ampliado

```cpp
mqttClient.setBufferSize(1024);  // 1KB para JSON
```

**Beneficios:**
- Soporta mensajes JSON complejos
- Evita truncamiento de datos
- Permite expansión futura

## 🔧 Parámetros Configurables

### En config.h

```cpp
// Red WiFi
const char* ssid = "cubicuz";
const char* password = "estoesesparta";

// MQTT
const char* MQTT_BROKER_ADRESS = "test.mosquitto.org";
const uint16_t MQTT_PORT = 1883;

// Umbrales
#define TEMP_HIGH 35.0
#define HUMIDITY_HIGH 80.0
#define CAQI_DANGEROUS 75

// Intervalos
#define READING_INTERVAL 30000  // ms
```

### En config.json

```json
{
  "sensors": {
    "reading_interval_seconds": 30,
    "temperature_range": [-10, 40],
    "humidity_range": [20, 90]
  },
  "actuators": {
    "led_alert": {
      "enabled": true,
      "auto_mode": true
    }
  }
}
```

## 📡 Topología de Red

```
Internet
   │
   │
   ▼
┌─────────────────────┐
│  Router WiFi        │
│  (192.168.1.1)      │
└─────────────────────┘
          │
          │ WiFi 802.11
          │
          ▼
    ┌──────────┐
    │  ESP32   │
    │ Weather  │
    │ Station  │
    └──────────┘
          │
          │ MQTT over TCP/IP
          │
          ▼
┌─────────────────────────┐
│  test.mosquitto.org     │
│  MQTT Broker (Puerto    │
│  1883)                  │
└─────────────────────────┘
          │
          ▼
    ┌─────────────┐
    │  Clientes   │
    │  MQTT       │
    │  (Dashboard)│
    └─────────────┘
```

## 🔄 Estados del Sistema

### Máquina de Estados

```
[INICIO]
   │
   ▼
[INICIALIZANDO]
   ├── Configurar Hardware
   ├── Conectar WiFi
   ├── Conectar MQTT
   └── Iniciar Sensores
   │
   ▼
[OPERATIVO]◄──────┐
   ├── Leer       │
   ├── Procesar   │
   ├── Actuar     │
   ├── Publicar   │
   └──────────────┘
   │
   │ (Error)
   ▼
[RECONECTANDO]
   ├── WiFi Lost → Reconnect
   ├── MQTT Lost → Reconnect
   └── Sensor Error → Log
   │
   └──► [OPERATIVO]
```

---

*Arquitectura diseñada para escalabilidad, robustez y mantenibilidad*

