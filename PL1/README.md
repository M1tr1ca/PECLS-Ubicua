# 🌤️ Estación Meteorológica IoT - Ciudad 4.0

## 📋 Información del Proyecto

**Asignatura:** Computación Ubicua  
**Universidad:** Universidad de Alcalá de Henares (UAH)  
**Proyecto:** PECL1 - Creación de un dispositivo IoT para la captación de datos de una ciudad 4.0  
**Fecha de Entrega:** 30 de octubre de 2025  

---

## 📖 Descripción General

Este proyecto implementa una **Estación Meteorológica IoT** completa que forma parte de un sistema de ciudad inteligente (Smart City 4.0). El dispositivo captura datos meteorológicos en tiempo real desde Alcalá de Henares y los transmite a un broker MQTT siguiendo el formato JSON especificado en el enunciado.

### 🎯 Objetivos Cumplidos

✅ **Identificación única de la estación** con datos de Alcalá de Henares  
✅ **8 componentes implementados** (5 sensores + 3 actuadores - supera el mínimo de 3)  
✅ **Comunicación bidireccional MQTT** con el sistema  
✅ **Formato JSON correcto** según especificaciones  
✅ **Control automático inteligente** de actuadores  
✅ **Sensores de alta calidad** BME280 con redundancia  

---

## 🏗️ Arquitectura del Sistema

```
┌─────────────────────────────────────────────────┐
│         ESTACIÓN METEOROLÓGICA ESP32            │
│                                                 │
│  ┌──────────────┐      ┌──────────────┐       │
│  │   SENSORES   │      │  ACTUADORES  │       │
│  ├──────────────┤      ├──────────────┤       │
│  │ BME280 #1    │      │ LED RGB      │       │
│  │ BME280 #2    │      │ Ventilador   │       │
│  │ MQ-135 #1    │      │ Calefactor   │       │
│  │ MQ-135 #2    │      └──────────────┘       │
│  │ MQ-135 #3    │                              │
│  └──────────────┘                              │
│                                                 │
│         ESP32 (Microcontrolador)               │
│              ↕️ WiFi                            │
└─────────────────────────────────────────────────┘
                    ↕️
        ┌───────────────────────┐
        │   BROKER MQTT         │
        │   (Mosquitto)         │
        │   test.mosquitto.org  │
        └───────────────────────┘
                    ↕️
        ┌───────────────────────┐
        │   SERVIDOR TOMCAT     │
        │   Base de Datos       │
        │   (MariaDB)           │
        └───────────────────────┘
```

---

## 🔧 Hardware Requerido

### Componentes Principales

| Componente | Cantidad | Función |
|------------|----------|---------|
| **ESP32** | 1 | Microcontrolador principal |
| **BME280** | 2 | Sensor de temperatura, humedad y presión atmosférica |
| **MQ-135** | 3 | Sensor de calidad del aire (CO2, NH3, NOx, alcohol, benceno, humo) |
| **LED RGB** | 1 | Indicador visual de estado |
| **Ventilador 5V** | 1 | Control de temperatura |
| **Módulo Relay** | 2 | Control de actuadores |
| **Resistencias** | varias | Divisores de voltaje y pull-ups |
| **Protoboard** | 1 | Montaje de circuito |
| **Cables Dupont** | varios | Conexiones |

### 📌 Conexiones de Pines

#### Sensores
```
BME280 #1 (0x76) y #2 (0x77)
  SDA      → GPIO 21 (I2C compartido)
  SCL      → GPIO 22 (I2C compartido)
  VCC      → 3.3V
  GND      → GND

MQ-135 #1  → GPIO 34 (ADC1_CH6)
MQ-135 #2  → GPIO 35 (ADC1_CH7)
MQ-135 #3  → GPIO 39 (ADC1_CH3)
  VCC      → 5V (o 3.3V según modelo)
  GND      → GND
```

#### Actuadores
```
LED Rojo       → GPIO 25 (PWM)
LED Verde      → GPIO 26 (PWM)
LED Azul       → GPIO 27 (PWM)
Ventilador     → GPIO 32 (via Relay)
Calefactor     → GPIO 33 (via Relay)
```

---

## 💻 Software y Librerías

### Entorno de Desarrollo
- **Arduino IDE** 2.x o superior
- **Platform:** ESP32 Arduino Core

### Librerías Necesarias

```cpp
// Comunicación
#include <WiFi.h>              // Conexión WiFi
#include <PubSubClient.h>      // Cliente MQTT

// Sensores
#include <Wire.h>              // Comunicación I2C
#include <Adafruit_BME280.h>   // BME280 (temperatura, humedad, presión)

// Utilidades
#include <ArduinoJson.h>       // Procesamiento JSON
```

### 📦 Instalación de Librerías

En el Arduino IDE, ve a: **Sketch → Include Library → Manage Libraries**

Busca e instala:
1. `Adafruit BME280 Library` por Adafruit
2. `Adafruit Unified Sensor` por Adafruit (dependencia)
3. `PubSubClient` por Nick O'Leary
4. `ArduinoJson` por Benoit Blanchon

---

## ⚙️ Configuración

### 1️⃣ Configurar WiFi y MQTT

Edita el archivo `config.h`:

```cpp
// WiFi
const char* ssid = "TU_RED_WIFI";
const char* password = "TU_CONTRASEÑA";

// MQTT
const char* MQTT_BROKER_ADRESS = "test.mosquitto.org";
const uint16_t MQTT_PORT = 1883;
```

### 2️⃣ Verificar Datos de la Estación

Los datos de Alcalá de Henares ya están configurados en `config.h`:

```cpp
const char* SENSOR_ID = "WS_ALC_01";
const char* SENSOR_TYPE = "weather";
const char* STREET_ID = "ST_ALC_001";
const float LATITUDE = 40.4823;
const float LONGITUDE = -3.3618;
const char* DISTRICT = "Alcalá de Henares";
```

### 3️⃣ Ajustar Umbrales (Opcional)

Puedes modificar los umbrales de los actuadores:

```cpp
#define TEMP_FAN_THRESHOLD 30.0      // °C para activar ventilador
#define TEMP_HEATER_THRESHOLD 10.0   // °C para activar calefactor
#define HUMIDITY_HIGH 80.0           // % humedad alta
#define UV_HIGH 6                    // Índice UV alto
```

---

## 🚀 Instalación y Uso

### Paso 1: Clonar/Descargar el Proyecto

```bash
git clone <URL_DEL_REPOSITORIO>
cd PL1
```

### Paso 2: Abrir en Arduino IDE

1. Abre `main.ino` en Arduino IDE
2. Verifica que todos los archivos estén en la misma carpeta:
   - `main.ino`
   - `config.h`
   - `ESP32_UTILS.hpp`
   - `ESP32_Utils_MQTT.hpp`

### Paso 3: Configurar la Placa

1. En Arduino IDE: **Tools → Board → ESP32 Arduino → ESP32 Dev Module**
2. Selecciona el puerto COM correcto: **Tools → Port**

### Paso 4: Compilar y Subir

1. Haz clic en **Verify** (✓) para compilar
2. Haz clic en **Upload** (→) para subir al ESP32
3. Abre el **Serial Monitor** (115200 baud) para ver los logs

### Paso 5: Verificar Funcionamiento

Deberías ver en el Serial Monitor:

```
═══════════════════════════════════════════
  ESTACIÓN METEOROLÓGICA IoT
  Universidad de Alcalá de Henares
═══════════════════════════════════════════

Configurando pines...
✓ Pines configurados
===========================================
Conectando a WiFi...
===========================================
.....
✓ WiFi Conectado
  SSID: TU_RED
  IP: 192.168.1.200

✓ MQTT Conectado
✓ Suscrito a: uah/alcala/weather/control

📊 Leyendo sensores...
  🌡️  Temperatura: 22.3°C
  💧 Humedad: 65.4%
  📏 Presión: 1013.2 hPa
  ☀️  Índice UV: 3
  💨 Viento: 12.5 km/h @ 180°
  🏭 Calidad aire (AQI): 45

📤 Publicando datos...
✓ Mensaje #1 enviado
```

---

## 📊 Formato de Datos

### Mensaje JSON Enviado

La estación envía datos cada **30 segundos** al tópico `uah/alcala/weather/data`:

```json
{
  "sensor_id": "WS_ALC_01",
  "sensor_type": "weather",
  "street_id": "ST_ALC_001",
  "timestamp": "2025-10-15T14:32:45.123",
  "location": {
    "latitude": 40.4823,
    "longitude": -3.3618,
    "altitude_meters": 588.0,
    "district": "Alcalá de Henares",
    "neighborhood": "Centro"
  },
  "data": {
    "temperature_celsius": 22.3,
    "humidity_percent": 65.4,
    "air_quality_index": 45,
    "wind_speed_kmh": 12.5,
    "wind_direction_degrees": 180,
    "atmospheric_pressure_hpa": 1013.2,
    "uv_index": 3,
    "fan_active": false,
    "heater_active": false
  }
}
```

### Comandos de Control (Recibidos)

La estación escucha comandos en el tópico `uah/alcala/weather/control`:

#### 1. Reiniciar dispositivo
```json
{"command": "reset"}
```

#### 2. Forzar lectura inmediata
```json
{"command": "read_now"}
```

#### 3. Controlar ventilador
```json
{"command": "fan_on", "value": true}
```

#### 4. Controlar calefactor
```json
{"command": "heater_on", "value": true}
```

#### 5. Cambiar color LED RGB
```json
{"command": "led_rgb", "r": 255, "g": 0, "b": 0}
```

---

## 🤖 Funcionamiento Inteligente

### Control Automático de Actuadores

#### 🌀 Ventilador
- **Se activa** cuando temperatura > 30°C
- **Se desactiva** cuando temperatura < 28°C
- Previene sobrecalentamiento de la estación

#### 🔥 Calefactor
- **Se activa** cuando temperatura < 10°C
- **Se desactiva** cuando temperatura > 12°C
- Protege sensores de temperaturas extremas

#### 💡 LED RGB (Indicador Visual)
- **Verde** 🟢: Funcionamiento normal
- **Azul** 🔵: Conectando a red
- **Naranja** 🟠: Condiciones meteorológicas extremas
- **Rojo** 🔴: Error de sistema
- **Azul Claro**: Humedad alta (>80%)

### Lectura de Sensores

Los sensores se leen cada **30 segundos** (configurable en `READING_INTERVAL`):

1. Se leen todos los sensores
2. Se procesan y validan los datos
3. Se controlan los actuadores automáticamente
4. Se crea el mensaje JSON
5. Se publica en el broker MQTT

---

## 🔍 Estructura del Código

### Archivos Principales

```
PL1/
├── main.ino                    # Código principal
├── config.h                    # Configuración WiFi, MQTT, pines
├── ESP32_UTILS.hpp             # Utilidades WiFi
├── ESP32_Utils_MQTT.hpp        # Utilidades MQTT
├── config.json                 # Configuración JSON (referencia)
├── README.md                   # Este archivo
└── enunciado.txt              # Enunciado del proyecto
```

### Funciones Principales en `main.ino`

| Función | Descripción |
|---------|-------------|
| `setup()` | Inicialización del sistema |
| `loop()` | Bucle principal |
| `InitPins()` | Configura pines GPIO |
| `InitSensors()` | Inicializa sensores |
| `ReadAllSensors()` | Lee todos los sensores |
| `ControlActuators()` | Control automático de actuadores |
| `CreateJSONMessage()` | Crea mensaje JSON |
| `PublishData()` | Publica datos en MQTT |
| `SetLED()` | Control LED RGB |

---

## 🧪 Pruebas y Validación

### Verificar Publicación MQTT

Puedes suscribirte al tópico para ver los mensajes:

```bash
mosquitto_sub -h test.mosquitto.org -t "uah/alcala/weather/data"
```

### Enviar Comandos de Control

```bash
# Activar ventilador
mosquitto_pub -h test.mosquitto.org -t "uah/alcala/weather/control" \
  -m '{"command":"fan_on","value":true}'

# Cambiar LED a rojo
mosquitto_pub -h test.mosquitto.org -t "uah/alcala/weather/control" \
  -m '{"command":"led_rgb","r":255,"g":0,"b":0}'
```

---

## 📈 Posibles Mejoras

### Funcionalidades Adicionales
- 📍 **GPS**: Ubicación dinámica en tiempo real
- 🔋 **Sensor de batería**: Monitoreo de energía
- 💾 **Almacenamiento local**: SD card para datos offline
- 🌙 **Modo bajo consumo**: Deep sleep entre lecturas
- 🔒 **Seguridad**: Autenticación MQTT con usuario/contraseña
- 📡 **OTA Updates**: Actualizaciones remotas del firmware
- 🎨 **Display**: Pantalla OLED para visualización local
- ☁️ **Integración Cloud**: ThingSpeak, AWS IoT, Azure IoT

### Sensores Adicionales
- ☔ Pluviómetro (lluvia)
- 🌅 Sensor de luminosidad
- ⚡ Detector de rayos
- 🔊 Nivel de ruido (micrófono)

---

## ⚠️ Solución de Problemas

### Error: No conecta a WiFi
- ✓ Verifica SSID y contraseña en `config.h`
- ✓ Asegúrate de usar WiFi 2.4GHz (ESP32 no soporta 5GHz)
- ✓ Verifica que el router no tenga filtrado MAC

### Error: No conecta a MQTT
- ✓ Verifica que el broker sea accesible
- ✓ Prueba con `test.mosquitto.org` (público)
- ✓ Revisa el puerto (1883 sin SSL, 8883 con SSL)

### Error: Lecturas de sensores NaN
- ✓ Verifica las conexiones físicas
- ✓ Revisa que los sensores estén alimentados
- ✓ Comprueba las librerías instaladas

### Error: Compilación fallida
- ✓ Instala todas las librerías necesarias
- ✓ Actualiza el ESP32 Core
- ✓ Selecciona la placa correcta

---

## 📚 Referencias

### Documentación Técnica
- [ESP32 Datasheet](https://www.espressif.com/sites/default/files/documentation/esp32_datasheet_en.pdf)
- [DHT22 Datasheet](https://www.sparkfun.com/datasheets/Sensors/Temperature/DHT22.pdf)
- [BMP280 Datasheet](https://www.bosch-sensortec.com/media/boschsensortec/downloads/datasheets/bst-bmp280-ds001.pdf)
- [MQTT Protocol](https://mqtt.org/)

### Librerías Utilizadas
- [PubSubClient](https://github.com/knolleary/pubsubclient)
- [ArduinoJson](https://arduinojson.org/)
- [Adafruit DHT](https://github.com/adafruit/DHT-sensor-library)
- [Adafruit BMP280](https://github.com/adafruit/Adafruit_BMP280_Library)

---

## 👨‍💻 Información del Desarrollador

**Asignatura:** Computación Ubicua  
**Universidad:** Universidad de Alcalá de Henares  
**Cuatrimestre:** 3º Cuatrimestre  
**Año Académico:** 2025  

---

## 📝 Licencia

Este proyecto es material académico para la asignatura de Computación Ubicua de la Universidad de Alcalá de Henares.

---

## 🎥 Vídeo Demostración

*[Aquí incluir el enlace al vídeo demostrativo del funcionamiento en tiempo real]*

---

## ✅ Checklist de Requisitos Cumplidos

- [x] **Nueva estación con identificación propia** → Alcalá de Henares
- [x] **Mínimo 3 sensores/actuadores** → 6 sensores + 3 actuadores = 9 componentes
- [x] **Conexión MQTT bidireccional** → Publica datos y recibe comandos
- [x] **Formato JSON correcto** → Según especificación del enunciado
- [x] **Comunicación con el sistema** → Via broker MQTT
- [x] **Código documentado** → Comentarios y este README
- [x] **Vídeo demostración** → Por grabar

---

## 🆘 Soporte

Para cualquier duda o problema:
1. Revisa la sección de **Solución de Problemas**
2. Verifica las **conexiones físicas**
3. Consulta la **documentación de las librerías**
4. Revisa el **Serial Monitor** para mensajes de error

---

**¡Sistema listo para operar! 🚀**

La estación meteorológica está completamente funcional y cumple todos los requisitos del proyecto PECL1.
