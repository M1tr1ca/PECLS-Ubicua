# 📡 Especificación API MQTT - Estación Meteorológica IoT

## 📋 Visión General

Este documento describe la especificación completa de la API de comunicación MQTT utilizada por la Estación Meteorológica IoT. Define los tópicos, formatos de mensajes, comandos y flujos de comunicación.

## 🌐 Configuración del Broker MQTT

### Parámetros de Conexión

| Parámetro | Valor | Descripción |
|-----------|-------|-------------|
| **Broker** | `test.mosquitto.org` | Broker MQTT público |
| **Puerto** | `1883` | Puerto TCP estándar MQTT |
| **Cliente ID** | `ESP32_WS_ALC_01` | Identificador único del cliente |
| **Keep Alive** | `60` segundos | Intervalo de keep-alive |
| **QoS** | `1` | Quality of Service (at least once) |
| **Clean Session** | `true` | Sesión limpia al conectar |
| **Retain** | `false` | No retener mensajes |

### Credenciales

Para el broker público `test.mosquitto.org`:
- **Usuario**: No requerido
- **Contraseña**: No requerida

> ⚠️ **Nota de Seguridad**: Para producción, usar broker privado con autenticación TLS/SSL.

## 📨 Tópicos MQTT

### Estructura de Tópicos

```
uah/
└── alcala/
    └── weather/
        ├── data        (Publicación de datos)
        └── control     (Recepción de comandos)
```

### Tópico de Datos (Publicación)

**Tópico:** `uah/alcala/weather/data`

- **Dirección:** Publicación (ESP32 → Broker)
- **QoS:** 1 (At least once)
- **Retain:** false
- **Frecuencia:** Cada 30 segundos
- **Formato:** JSON

**Payload de ejemplo:**

```json
{
  "sensor_id": "WS_ALC_01",
  "sensor_type": "weather",
  "street_id": "ST_ALC_001",
  "timestamp": "2025-10-13T14:30:45.123",
  "location": {
    "latitude": 40.4823,
    "longitude": -3.3618,
    "altitude_meters": 588.0,
    "district": "Alcalá de Henares",
    "neighborhood": "Centro"
  },
  "data": {
    "temperature_celsius": 23.5,
    "humidity_percent": 65.3,
    "air_quality_index": 45,
    "atmospheric_pressure_hpa": 1013.2
  }
}
```

### Tópico de Control (Suscripción)

**Tópico:** `uah/alcala/weather/control`

- **Dirección:** Suscripción (Broker → ESP32)
- **QoS:** 1 (At least once)
- **Formato:** JSON

**Comandos soportados:**

```json
{
  "command": "reset"
}
```

```json
{
  "command": "status"
}
```

```json
{
  "command": "config",
  "params": {
    "reading_interval": 60
  }
}
```

## 📊 Formato de Mensajes JSON

### Mensaje de Datos Meteorológicos

#### Estructura Completa

```json
{
  "sensor_id": "string",
  "sensor_type": "string",
  "street_id": "string",
  "timestamp": "string (ISO 8601)",
  "location": {
    "latitude": "number (float)",
    "longitude": "number (float)",
    "altitude_meters": "number (float)",
    "district": "string",
    "neighborhood": "string"
  },
  "data": {
    "temperature_celsius": "number (float, 1 decimal)",
    "humidity_percent": "number (float, 1 decimal)",
    "air_quality_index": "number (integer, 0-150)",
    "atmospheric_pressure_hpa": "number (float, 1 decimal)"
  }
}
```

#### Especificación de Campos

##### Campos Raíz

| Campo | Tipo | Requerido | Descripción | Ejemplo |
|-------|------|-----------|-------------|---------|
| `sensor_id` | string | ✅ | ID único del sensor | `"WS_ALC_01"` |
| `sensor_type` | string | ✅ | Tipo de sensor | `"weather"` |
| `street_id` | string | ✅ | ID de la calle/zona | `"ST_ALC_001"` |
| `timestamp` | string | ✅ | Fecha/hora ISO 8601 | `"2025-10-13T14:30:45.123"` |
| `location` | object | ✅ | Objeto de ubicación | Ver tabla siguiente |
| `data` | object | ✅ | Datos meteorológicos | Ver tabla siguiente |

##### Objeto `location`

| Campo | Tipo | Requerido | Rango | Descripción |
|-------|------|-----------|-------|-------------|
| `latitude` | float | ✅ | -90 a 90 | Latitud en grados decimales |
| `longitude` | float | ✅ | -180 a 180 | Longitud en grados decimales |
| `altitude_meters` | float | ✅ | 0 a 9000 | Altitud sobre el nivel del mar |
| `district` | string | ✅ | - | Distrito o ciudad |
| `neighborhood` | string | ✅ | - | Barrio o zona |

**Ejemplo:**
```json
"location": {
  "latitude": 40.4823,
  "longitude": -3.3618,
  "altitude_meters": 588.0,
  "district": "Alcalá de Henares",
  "neighborhood": "Centro"
}
```

##### Objeto `data`

| Campo | Tipo | Requerido | Rango | Unidad | Precisión |
|-------|------|-----------|-------|--------|-----------|
| `temperature_celsius` | float | ✅ | -40 a 85 | °C | 0.1°C |
| `humidity_percent` | float | ✅ | 0 a 100 | % | 0.1% |
| `air_quality_index` | integer | ✅ | 0 a 150 | CAQI | 1 |
| `atmospheric_pressure_hpa` | float | ✅ | 300 a 1100 | hPa | 0.1 hPa |

**Ejemplo:**
```json
"data": {
  "temperature_celsius": 23.5,
  "humidity_percent": 65.3,
  "air_quality_index": 45,
  "atmospheric_pressure_hpa": 1013.2
}
```

### Mensajes de Control

#### Comando: Reset

Reinicia el dispositivo ESP32.

```json
{
  "command": "reset"
}
```

**Respuesta esperada:** El dispositivo se reinicia y vuelve a conectar.

#### Comando: Status

Solicita el estado actual del dispositivo.

```json
{
  "command": "status"
}
```

**Respuesta (futuro):**
```json
{
  "sensor_id": "WS_ALC_01",
  "status": "operational",
  "uptime_seconds": 3600,
  "wifi_rssi": -45,
  "mqtt_connected": true,
  "sensors": {
    "bme280": "ok",
    "mq135": "ok"
  }
}
```

#### Comando: Config

Cambia la configuración del dispositivo (futuro).

```json
{
  "command": "config",
  "params": {
    "reading_interval": 60,
    "led_enabled": true
  }
}
```

## 🔄 Flujos de Comunicación

### Flujo de Publicación de Datos

```
┌─────────┐                                    ┌──────────────┐
│  ESP32  │                                    │ MQTT Broker  │
└────┬────┘                                    └──────┬───────┘
     │                                                 │
     │  1. Leer sensores cada 30s                    │
     │─────────────────────────────►                 │
     │                                                 │
     │  2. Crear JSON                                 │
     │─────────────────────────────►                 │
     │                                                 │
     │  3. PUBLISH uah/alcala/weather/data           │
     │────────────────────────────────────────────►  │
     │                                                 │
     │  4. PUBACK (QoS 1)                            │
     │ ◄────────────────────────────────────────────│
     │                                                 │
     │                                                 │
     │                                                 ▼
     │                                          ┌─────────────┐
     │                                          │ Suscriptores│
     │                                          │ (Dashboard, │
     │                                          │  Analytics) │
     │                                          └─────────────┘
```

### Flujo de Recepción de Comandos

```
┌─────────────┐         ┌──────────────┐         ┌─────────┐
│  Dashboard  │         │ MQTT Broker  │         │  ESP32  │
└──────┬──────┘         └──────┬───────┘         └────┬────┘
       │                        │                      │
       │  1. Enviar comando     │                      │
       │──────────────────────► │                      │
       │  PUBLISH control        │                      │
       │                         │                      │
       │                         │  2. Entregar mensaje │
       │                         │─────────────────────►│
       │                         │                      │
       │                         │  3. Procesar comando │
       │                         │                      │─►
       │                         │                      │
       │                         │  4. Ejecutar acción  │
       │                         │     (ej: reset)      │
       │                         │                      │
```

### Secuencia de Conexión MQTT

```
ESP32                           Broker
  │                               │
  │  1. TCP Connect (port 1883)   │
  │──────────────────────────────►│
  │                               │
  │  2. CONNECT packet            │
  │  (Client ID: ESP32_WS_ALC_01) │
  │──────────────────────────────►│
  │                               │
  │  3. CONNACK                   │
  │◄──────────────────────────────│
  │                               │
  │  4. SUBSCRIBE                 │
  │  (uah/alcala/weather/control) │
  │──────────────────────────────►│
  │                               │
  │  5. SUBACK                    │
  │◄──────────────────────────────│
  │                               │
  │  [Conexión establecida]       │
  │                               │
  │  6. PUBLISH (cada 30s)        │
  │  (uah/alcala/weather/data)    │
  │──────────────────────────────►│
  │                               │
  │  7. PUBACK                    │
  │◄──────────────────────────────│
  │                               │
```

## 🎯 Códigos de Estado MQTT

### Códigos de Retorno de Conexión

| Código | Nombre | Descripción | Solución |
|--------|--------|-------------|----------|
| 0 | MQTT_CONNECTED | Conectado exitosamente | - |
| -4 | MQTT_CONNECTION_TIMEOUT | Timeout de conexión | Verificar broker/red |
| -3 | MQTT_CONNECTION_LOST | Conexión perdida | Verificar WiFi |
| -2 | MQTT_CONNECT_FAILED | Conexión fallida | Verificar parámetros |
| -1 | MQTT_DISCONNECTED | Desconectado | Normal antes de conectar |
| 1 | MQTT_CONNECT_BAD_PROTOCOL | Protocolo incorrecto | Actualizar biblioteca |
| 2 | MQTT_CONNECT_BAD_CLIENT_ID | Client ID rechazado | Cambiar Client ID |
| 3 | MQTT_CONNECT_UNAVAILABLE | Broker no disponible | Verificar broker |
| 4 | MQTT_CONNECT_BAD_CREDENTIALS | Credenciales inválidas | Verificar user/pass |
| 5 | MQTT_CONNECT_UNAUTHORIZED | No autorizado | Verificar permisos |

### Estados en el Código

```cpp
void ConnectMQTT() {
    while (!mqttClient.connected()) {
        if (mqttClient.connect(MQTT_CLIENT_NAME)) {
            Serial.println("✓ Conectado");
        } else {
            Serial.print("✗ Error, rc=");
            Serial.print(mqttClient.state());
            // Ver tabla de códigos arriba
            delay(5000);
        }
    }
}
```

## 📐 Esquemas JSON

### Schema JSON (JSON Schema Draft 7)

#### Mensaje de Datos

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "WeatherStationData",
  "type": "object",
  "required": ["sensor_id", "sensor_type", "street_id", "timestamp", "location", "data"],
  "properties": {
    "sensor_id": {
      "type": "string",
      "pattern": "^WS_[A-Z]+_[0-9]+$"
    },
    "sensor_type": {
      "type": "string",
      "enum": ["weather"]
    },
    "street_id": {
      "type": "string",
      "pattern": "^ST_[A-Z]+_[0-9]+$"
    },
    "timestamp": {
      "type": "string",
      "format": "date-time"
    },
    "location": {
      "type": "object",
      "required": ["latitude", "longitude", "altitude_meters", "district", "neighborhood"],
      "properties": {
        "latitude": {
          "type": "number",
          "minimum": -90,
          "maximum": 90
        },
        "longitude": {
          "type": "number",
          "minimum": -180,
          "maximum": 180
        },
        "altitude_meters": {
          "type": "number",
          "minimum": 0
        },
        "district": { "type": "string" },
        "neighborhood": { "type": "string" }
      }
    },
    "data": {
      "type": "object",
      "required": ["temperature_celsius", "humidity_percent", "air_quality_index", "atmospheric_pressure_hpa"],
      "properties": {
        "temperature_celsius": {
          "type": "number",
          "minimum": -40,
          "maximum": 85
        },
        "humidity_percent": {
          "type": "number",
          "minimum": 0,
          "maximum": 100
        },
        "air_quality_index": {
          "type": "integer",
          "minimum": 0,
          "maximum": 150
        },
        "atmospheric_pressure_hpa": {
          "type": "number",
          "minimum": 300,
          "maximum": 1100
        }
      }
    }
  }
}
```

#### Comando de Control

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "WeatherStationControl",
  "type": "object",
  "required": ["command"],
  "properties": {
    "command": {
      "type": "string",
      "enum": ["reset", "status", "config"]
    },
    "params": {
      "type": "object"
    }
  }
}
```

## 🔧 Ejemplos de Uso

### Ejemplo 1: Suscribirse con Mosquitto Client

```bash
# Suscribirse a todos los tópicos de la estación
mosquitto_sub -h test.mosquitto.org \
  -t "uah/alcala/weather/#" \
  -v

# Suscribirse solo a datos
mosquitto_sub -h test.mosquitto.org \
  -t "uah/alcala/weather/data" \
  -F "@Y-@m-@dT@H:@M:@S@z : %t : %p"
```

### Ejemplo 2: Publicar Comando con Mosquitto Client

```bash
# Comando reset
mosquitto_pub -h test.mosquitto.org \
  -t "uah/alcala/weather/control" \
  -m '{"command":"reset"}'

# Comando status
mosquitto_pub -h test.mosquitto.org \
  -t "uah/alcala/weather/control" \
  -m '{"command":"status"}'
```

### Ejemplo 3: Cliente Python

```python
import paho.mqtt.client as mqtt
import json

def on_connect(client, userdata, flags, rc):
    print(f"Conectado con código: {rc}")
    client.subscribe("uah/alcala/weather/data")

def on_message(client, userdata, msg):
    data = json.loads(msg.payload)
    print(f"Temperatura: {data['data']['temperature_celsius']}°C")
    print(f"Humedad: {data['data']['humidity_percent']}%")
    print(f"CAQI: {data['data']['air_quality_index']}")

client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

client.connect("test.mosquitto.org", 1883, 60)
client.loop_forever()
```

### Ejemplo 4: Cliente Node.js

```javascript
const mqtt = require('mqtt');

const client = mqtt.connect('mqtt://test.mosquitto.org:1883');

client.on('connect', () => {
    console.log('Conectado al broker MQTT');
    client.subscribe('uah/alcala/weather/data');
});

client.on('message', (topic, message) => {
    const data = JSON.parse(message.toString());
    console.log(`
        Sensor: ${data.sensor_id}
        Temp: ${data.data.temperature_celsius}°C
        Humedad: ${data.data.humidity_percent}%
        CAQI: ${data.data.air_quality_index}
        Presión: ${data.data.atmospheric_pressure_hpa} hPa
    `);
});
```

### Ejemplo 5: Dashboard Node-RED

**Flow JSON para Node-RED:**

```json
[
    {
        "id": "mqtt_in",
        "type": "mqtt in",
        "broker": "test.mosquitto.org",
        "topic": "uah/alcala/weather/data",
        "qos": "1"
    },
    {
        "id": "json_parse",
        "type": "json"
    },
    {
        "id": "gauge_temp",
        "type": "ui_gauge",
        "name": "Temperatura",
        "min": "-10",
        "max": "40",
        "unit": "°C"
    }
]
```

## 📊 Índice CAQI (Common Air Quality Index)

### Escala y Significado

| CAQI | Nivel | Color | Descripción | Acción Recomendada |
|------|-------|-------|-------------|-------------------|
| 0-25 | Muy Bueno | 🟢 Verde | Calidad del aire excelente | Ninguna |
| 26-50 | Bueno | 🟡 Amarillo | Calidad del aire aceptable | Ninguna |
| 51-75 | Moderado | 🟠 Naranja | Calidad aceptable para la mayoría | Grupos sensibles: precaución |
| 76-100 | Malo | 🔴 Rojo | Calidad del aire mala | Reducir actividad al aire libre |
| 101-150 | Muy Malo | 🟣 Morado | Calidad del aire muy mala | Evitar actividad al aire libre |

### Conversión de PPM a CAQI

El sensor MQ-135 mide concentración de gases en PPM (partes por millón), que se convierte a CAQI:

| CO₂ (PPM) | CAQI | Nivel |
|-----------|------|-------|
| 0-600 | 0-25 | Muy Bueno |
| 600-800 | 26-50 | Bueno |
| 800-1000 | 51-75 | Moderado |
| 1000-1500 | 76-100 | Malo |
| >1500 | 101-150 | Muy Malo |

## 🔐 Seguridad

### Recomendaciones de Producción

Para un entorno de producción, implementar:

1. **Autenticación MQTT**
```cpp
mqttClient.connect(MQTT_CLIENT_NAME, "usuario", "contraseña");
```

2. **TLS/SSL**
```cpp
WiFiClientSecure espClient;
espClient.setCACert(ca_cert);
PubSubClient mqttClient(espClient);
```

3. **Tokens JWT** para autenticación de comandos

4. **ACLs** (Access Control Lists) en el broker

### Validación de Mensajes

El ESP32 valida:
- ✅ Rango de valores de sensores
- ✅ Formato JSON válido
- ✅ Comandos permitidos
- ✅ Conexión segura al broker

## 📈 Optimización y Rendimiento

### Tamaño de Mensajes

- **Payload típico**: ~400 bytes
- **Buffer MQTT**: 1024 bytes
- **Overhead MQTT**: ~10-20 bytes
- **Compresión**: No implementada

### Frecuencia de Publicación

- **Estándar**: 30 segundos
- **Rápido**: 10 segundos (mayor tráfico)
- **Eco**: 60 segundos (menor consumo)

```cpp
#define READING_INTERVAL 30000  // Modificar en config.h
```

### QoS (Quality of Service)

| QoS | Nombre | Garantía | Uso |
|-----|--------|----------|-----|
| 0 | At most once | Sin confirmación | No crítico |
| 1 | At least once | Confirmado | **Datos** (actual) |
| 2 | Exactly once | Garantizado | Comandos críticos |

## 🧪 Testing

### Herramientas de Prueba

1. **MQTT Explorer** - GUI para explorar tópicos
2. **mosquitto_sub/pub** - CLI para suscripción/publicación
3. **Postman** - Cliente MQTT integrado
4. **HiveMQ Websocket Client** - Cliente web

### Scripts de Prueba

**Prueba de carga (Bash):**

```bash
#!/bin/bash
for i in {1..100}; do
    mosquitto_pub -h test.mosquitto.org \
      -t "uah/alcala/weather/data" \
      -m "{\"sensor_id\":\"WS_TEST_$i\",\"data\":{\"temperature_celsius\":$((20 + RANDOM % 10))}}"
    sleep 1
done
```

**Monitor continuo (Python):**

```python
import paho.mqtt.client as mqtt
from datetime import datetime

def on_message(client, userdata, msg):
    print(f"[{datetime.now()}] {msg.topic}: {msg.payload.decode()}")

client = mqtt.Client()
client.on_message = on_message
client.connect("test.mosquitto.org", 1883, 60)
client.subscribe("uah/alcala/weather/#")
client.loop_forever()
```

## 📚 Referencias

- [MQTT v3.1.1 Specification](https://docs.oasis-open.org/mqtt/mqtt/v3.1.1/mqtt-v3.1.1.html)
- [JSON Schema](https://json-schema.org/)
- [PubSubClient Library](https://pubsubclient.knolleary.net/)
- [ArduinoJson](https://arduinojson.org/)
- [CAQI European Standard](https://www.airqualitynow.eu/about_indices_definition.php)

---

*API diseñada siguiendo estándares IoT y mejores prácticas de comunicación MQTT*

