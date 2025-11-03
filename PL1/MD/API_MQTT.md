# 📡 API MQTT - Especificación

## 🌐 Configuración Broker

| Parámetro | Valor |
|-----------|-------|
| Broker | `172.20.10.11` (Servidor local) |
| Puerto | `1883` |
| Cliente | `LAB08JAV-G5` |
| QoS | `1` (At least once) |
| Keep Alive | `60s` |

## 📨 Tópicos

### 1. Publicación de Datos
**Tópico:** `uah/alcala/weather/data`
- **Dirección:** ESP32 → Broker
- **Frecuencia:** 5 segundos
- **QoS:** 1

### 2. Control Remoto
**Tópico:** `uah/alcala/weather/control`
- **Dirección:** Broker → ESP32
- **QoS:** 1

## 📊 Formato JSON

### Mensaje de Datos

```json
{
  "sensor_id": "ST_1617",
  "sensor_type": "Estación metereológica IoT",
  "street_id": "ST_1617",
  "timestamp": "2025-11-03 14:30:45:123",
  "location": {
    "latitude": 40.4513367,
    "longitude": -3.6409307,
    "altitude_meters": 658.5,
    "district": "Hortaleza",
    "neighborhood": "Hortaleza"
  },
  "data": {
    "temperature_celsius": 23.5,
    "humidity_percent": 65.3,
    "atmospheric_pressure_hpa": 1013.2,
    "wind_speed_kmh": 0.0,
    "wind_direction_degrees": 0,
    "uv_index": 0
  }
}
```

**Nota:** `wind_speed_kmh`, `wind_direction_degrees` y `uv_index` son valores por defecto (sin sensores físicos)

### Comandos de Control

**Nivel de Alerta (0-4):**
```json
{"alert_level": 0}  // Sin alerta
{"alert_level": 1}  // Alerta BAJA - 1 LED lento
{"alert_level": 2}  // Alerta MEDIA - 2 LEDs medio
{"alert_level": 3}  // Alerta ALTA - 3 LEDs rápido
{"alert_level": 4}  // Alerta CRÍTICA - 3 LEDs muy rápido
```

**Reiniciar Dispositivo:**
```json
{"command": "reset"}
```

## 🎯 Sistema de Alertas

| Nivel | LEDs Activos | Frecuencia | Período | Display |
|-------|--------------|------------|---------|---------|
| 0 | Ninguno | - | - | '0' |
| 1 | 1 | 1 Hz | 700ms | '1' |
| 2 | 2 | 2 Hz | 300ms | '2' |
| 3 | 3 | 4 Hz | 150ms | '3' |
| 4 | 3 | 10 Hz | 50ms | '4' |

### Comportamiento del Sistema

Al recibir `alert_level`:
1. Elimina tarea FreeRTOS anterior (si existe)
2. Apaga todos los LEDs
3. Crea nueva tarea según nivel
4. Actualiza display 7 segmentos
5. Los LEDs parpadean según el patrón

## 🔄 Flujo de Comunicación

```
Cliente               Broker              ESP32
  │                     │                   │
  ├─ PUBLISH ─────────►│                   │
  │  alert_level:2      │                   │
  │                     ├─ DELIVER ───────►│
  │                     │                   │
  │                     │                   ├─ Process
  │                     │                   │  - Delete old task
  │                     │                   │  - Create task LED_2
  │                     │                   │  - Display shows '2'
  │                     │                   │  - 2 LEDs parpadean 300ms
```



## 🧪 Ejemplos de Uso

### Python - Suscribirse

```python
import paho.mqtt.client as mqtt
import json

def on_message(client, userdata, msg):
    data = json.loads(msg.payload)
    print(f"Sensor: {data['sensor_id']}")
    print(f"Temp: {data['data']['temperature_celsius']}°C")
    print(f"Hum: {data['data']['humidity_percent']}%")
    print(f"Presión: {data['data']['atmospheric_pressure_hpa']} hPa")

client = mqtt.Client()
client.on_message = on_message
client.connect("172.20.10.11", 1883)
client.subscribe("uah/alcala/weather/data")
client.loop_forever()
```

### Bash - Publicar Comandos

```bash
# Nivel 1 - Alerta baja
mosquitto_pub -h 172.20.10.11 \
  -t "uah/alcala/weather/control" \
  -m '{"alert_level":1}'

# Nivel 3 - Alerta alta
mosquitto_pub -h 172.20.10.11 \
  -t "uah/alcala/weather/control" \
  -m '{"alert_level":3}'

# Reiniciar
mosquitto_pub -h 172.20.10.11 \
  -t "uah/alcala/weather/control" \
  -m '{"command":"reset"}'
```

### Node.js - Cliente

```javascript
const mqtt = require('mqtt');
const client = mqtt.connect('mqtt://172.20.10.11');

client.on('connect', () => {
    client.subscribe('uah/alcala/weather/data');
});

client.on('message', (topic, message) => {
    const data = JSON.parse(message);
    console.log(`Sensor: ${data.sensor_id}`);
    console.log(`Temp: ${data.data.temperature_celsius}°C`);
    console.log(`Altitud: ${data.location.altitude_meters}m`);
});
```

## 🔐 Códigos de Estado

| Código | Descripción | Acción |
|--------|-------------|--------|
| 0 | Conectado | OK |
| -4 | Timeout | Verificar red |
| -3 | Conexión perdida | Reconectar |
| -2 | Fallo conexión | Check broker |
| 5 | No autorizado | Credenciales |

## ⚙️ Optimización

- **Buffer**: 1024 bytes
- **Payload típico**: ~500 bytes (incluye ubicación detallada)
- **QoS 1**: Garantiza entrega
- **FreeRTOS**: Gestión eficiente de tareas LED
- **Frecuencia**: 5s (sensores), inmediato (comandos)
- **Timestamp**: NTP sincronizado (pool.ntp.org)
- **Display**: Control manual GPIO (sin librería)

---

*Protocolo MQTT v3.1.1 con gestión de alertas en tiempo real*
