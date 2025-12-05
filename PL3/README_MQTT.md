# 📱 App Android - Monitorización de Calles de Madrid

## Descripción
Aplicación Android para monitorizar sensores IoT desplegados en las calles de Madrid. 
Muestra un mapa interactivo con marcadores en cada calle y permite consultar estadísticas 
en tiempo real de 4 tipos de sensores mediante MQTT.

---

## 🔌 Configuración MQTT

### Broker MQTT
- **Host (emulador):** `tcp://10.0.2.2:3000`
- **Host (dispositivo real):** `tcp://<IP_SERVIDOR>:3000`

### Formato de Topics MQTT

```
sensors/{street_id}/{sensor_type}/{sensor_id}
```

**Donde:**
- `{street_id}` = ID de la calle (ej: `ST_1617`, `ST_1`, `ST_2`...)
- `{sensor_type}` = Tipo de sensor:
  - `weather_station` - Estación meteorológica
  - `traffic_counter` - Contador de tráfico
  - `traffic_light` - Semáforo
  - `display` - Pantalla de información
- `{sensor_id}` = ID único del sensor (ej: `LAB08JAV-G5`)

### Ejemplos de Topics

| Tipo de Sensor | Topic MQTT |
|----------------|------------|
| Meteorológico | `sensors/ST_1617/weather_station/LAB08JAV-G5` |
| Contador de Tráfico | `sensors/ST_1617/traffic_counter/LAB08JAV-G5` |
| Semáforos | `sensors/ST_1617/traffic_light/LAB08JAV-G5` |
| Pantallas de Info | `sensors/ST_1617/display/LAB08JAV-G5` |

---

## 📡 Ejemplos de Mensajes MQTT

### 1️⃣ Sensor Meteorológico (Weather Station)

**Topic:** `sensors/ST_1617/weather_station/LAB08JAV-G5`

```json
{
  "sensor_id": "LAB08JAV-G5",
  "timestamp": "2025-12-05 15:30:00:000",
  "location": {
    "altitude_meters": 650.0
  },
  "data": {
    "temperature_celsius": 18.5,
    "humidity_percent": 72.3,
    "atmospheric_pressure_hpa": 1015.8
  }
}
```

**Comando para enviar (mosquitto_pub):**
```bash
mosquitto_pub -h localhost -p 3000 -t "sensors/ST_1617/weather_station/LAB08JAV-G5" -m '{"sensor_id":"LAB08JAV-G5","timestamp":"2025-12-05 15:30:00:000","location":{"altitude_meters":650.0},"data":{"temperature_celsius":18.5,"humidity_percent":72.3,"atmospheric_pressure_hpa":1015.8}}'
```

---

### 2️⃣ Contador de Tráfico (Traffic Counter)

**Topic:** `sensors/ST_1617/traffic_counter/LAB08JAV-G5`

```json
{
  "sensor_id": "LAB08JAV-G5",
  "timestamp": "2025-12-05 15:30:00:000",
  "data": {
    "vehicle_count": 245,
    "pedestrian_count": 89,
    "bicycle_count": 32,
    "average_speed_kmh": 35.5,
    "traffic_density": "medium",
    "direction": "north-south"
  }
}
```

**Comando para enviar:**
```bash
mosquitto_pub -h localhost -p 3000 -t "sensors/ST_1617/traffic_counter/LAB08JAV-G5" -m '{"sensor_id":"LAB08JAV-G5","timestamp":"2025-12-05 15:30:00:000","data":{"vehicle_count":245,"pedestrian_count":89,"bicycle_count":32,"average_speed_kmh":35.5,"traffic_density":"medium","direction":"north-south"}}'
```

---

### 3️⃣ Semáforos (Traffic Light)

**Topic:** `sensors/ST_1617/traffic_light/LAB08JAV-G5`

```json
{
  "sensor_id": "LAB08JAV-G5",
  "timestamp": "2025-12-05 15:30:00:000",
  "data": {
    "state": "green",
    "remaining_seconds": 25,
    "cycle_duration_seconds": 90,
    "pedestrian_waiting": false,
    "malfunction_detected": false,
    "circulation_direction": "east-west"
  }
}
```

**Estados posibles:** `red`, `yellow`, `green`

**Comandos de ejemplo:**
```bash
# Semáforo en ROJO
mosquitto_pub -h localhost -p 3000 -t "sensors/ST_1617/traffic_light/LAB08JAV-G5" -m '{"sensor_id":"LAB08JAV-G5","timestamp":"2025-12-05 15:30:00:000","data":{"state":"red","remaining_seconds":30}}'

# Semáforo en AMARILLO
mosquitto_pub -h localhost -p 3000 -t "sensors/ST_1617/traffic_light/LAB08JAV-G5" -m '{"sensor_id":"LAB08JAV-G5","timestamp":"2025-12-05 15:30:00:000","data":{"state":"yellow","remaining_seconds":5}}'

# Semáforo en VERDE
mosquitto_pub -h localhost -p 3000 -t "sensors/ST_1617/traffic_light/LAB08JAV-G5" -m '{"sensor_id":"LAB08JAV-G5","timestamp":"2025-12-05 15:30:00:000","data":{"state":"green","remaining_seconds":45}}'
```

---

### 4️⃣ Pantallas de Información (Display)

**Topic:** `sensors/ST_1617/display/LAB08JAV-G5`

```json
{
  "sensor_id": "LAB08JAV-G5",
  "timestamp": "2025-12-05 15:30:00:000",
  "data": {
    "display_status": "active",
    "current_message": "Bienvenidos a Madrid - Temperatura: 18°C",
    "brightness_percent": 75,
    "content_type": "info",
    "temperature_celsius": 32.5,
    "energy_consumption_watts": 150.0
  }
}
```

**Estados posibles para display_status:** `active`, `inactive`, `error`, `maintenance`

**Comando para enviar:**
```bash
mosquitto_pub -h localhost -p 3000 -t "sensors/ST_1617/display/LAB08JAV-G5" -m '{"sensor_id":"LAB08JAV-G5","timestamp":"2025-12-05 15:30:00:000","data":{"display_status":"active","current_message":"Bienvenidos a Madrid - Temperatura: 18°C","brightness_percent":75}}'
```

---

## 🗺️ Calles Disponibles

| ID Calle | Street ID | Nombre | Distrito |
|----------|-----------|--------|----------|
| 1 | ST_1 | Calle de Nicolasa Gómez | San Blas-Canillejas |
| 2 | ST_2 | Calle de Luis Peidró | Vicálvaro |
| 3 | ST_3 | Calle del Puerto de Bonaigua | Moratalaz |
| 4 | ST_4 | Calle de Antonio Leyva | Carabanchel |
| 5 | ST_5 | Calle de la Colegiata | Centro |
| 6 | ST_6 | Calle Yuste | Puente de Vallecas |
| 7 | ST_7 | Calle de Joaquín Dicenta | Chamberí |
| 8 | ST_8 | Avenida de Moratalaz | Moratalaz |
| 9 | ST_9 | Calle de Cavanilles | Retiro |
| 10 | ST_10 | Calle de Iriarte | Salamanca |
| 11 | ST_11 | Calle de Príncipe de Vergara | Salamanca |
| 12 | ST_12 | Calle de Bravo Murillo | Tetuán |
| 13 | ST_13 | Gran Vía | Centro |
| 14 | ST_14 | Paseo de la Castellana | Chamartín |
| 15 | ST_15 | Calle de Alcalá | Centro |

---

## 🧪 Script de Prueba Completo

### Para Bash (Linux/Mac):
```bash
#!/bin/bash

HOST="localhost"
PORT="3000"
STREET_ID="ST_1617"
SENSOR_ID="LAB08JAV-G5"
TIMESTAMP=$(date +"%Y-%m-%d %H:%M:%S:000")

echo "📡 Enviando datos de prueba..."

# Weather Station
echo "🌡️ Enviando datos meteorológicos..."
mosquitto_pub -h $HOST -p $PORT -t "sensors/$STREET_ID/weather_station/$SENSOR_ID" \
  -m "{\"sensor_id\":\"$SENSOR_ID\",\"timestamp\":\"$TIMESTAMP\",\"location\":{\"altitude_meters\":650.0},\"data\":{\"temperature_celsius\":22.5,\"humidity_percent\":65.0,\"atmospheric_pressure_hpa\":1013.25}}"

sleep 1

# Traffic Counter
echo "🚗 Enviando datos de contador de tráfico..."
mosquitto_pub -h $HOST -p $PORT -t "sensors/$STREET_ID/traffic_counter/$SENSOR_ID" \
  -m "{\"sensor_id\":\"$SENSOR_ID\",\"timestamp\":\"$TIMESTAMP\",\"data\":{\"vehicle_count\":156,\"pedestrian_count\":42,\"bicycle_count\":18}}"

sleep 1

# Traffic Light
echo "🚦 Enviando estado del semáforo..."
mosquitto_pub -h $HOST -p $PORT -t "sensors/$STREET_ID/traffic_light/$SENSOR_ID" \
  -m "{\"sensor_id\":\"$SENSOR_ID\",\"timestamp\":\"$TIMESTAMP\",\"data\":{\"state\":\"green\",\"remaining_seconds\":30}}"

sleep 1

# Display
echo "📺 Enviando datos de pantalla..."
mosquitto_pub -h $HOST -p $PORT -t "sensors/$STREET_ID/display/$SENSOR_ID" \
  -m "{\"sensor_id\":\"$SENSOR_ID\",\"timestamp\":\"$TIMESTAMP\",\"data\":{\"display_status\":\"active\",\"current_message\":\"Hoy hace buen tiempo en Madrid\",\"brightness_percent\":80}}"

echo "✅ Todos los datos enviados correctamente!"
```

### Para Docker (Mosquitto en contenedor):
```bash
# Entrar al contenedor
docker exec -it mosquitto sh

# Publicar mensaje de weather
mosquitto_pub -h localhost -p 1883 -t "sensors/ST_1617/weather_station/LAB08JAV-G5" \
  -m '{"sensor_id":"LAB08JAV-G5","timestamp":"2025-12-05 15:30:00:000","location":{"altitude_meters":650.0},"data":{"temperature_celsius":20.0,"humidity_percent":55.0,"atmospheric_pressure_hpa":1012.0}}'
```

---

## ⚙️ Configuración ESP32

Tu archivo `config.h` del ESP32 debería verse así:

```cpp
// Tópicos MQTT - Formato: sensors/{street_id}/{sensor_type}/{sensor_id}
const char* TOPIC_PUBLISH = "sensors/ST_1617/weather_station/LAB08JAV-G5";
const char* TOPIC_SUBSCRIBE = "sensors/ST_1617/alerts";

// Identificación
const char* SENSOR_ID = "LAB08JAV-G5";
const char* SENSOR_TYPE = "weather";
const char* STREET_ID = "ST_1617";
const char* STREET_NAME = "Calle Pepe Hillo";
```

---

## 📱 Flujo de la App

```
┌─────────────────┐     5 seg      ┌──────────────────┐
│   Splash Screen │ ─────────────▶ │   Mapa Madrid    │
│   (MainActivity)│                │   (MapActivity)  │
└─────────────────┘                └────────┬─────────┘
                                            │
                                   Click en marcador
                                            │
                                            ▼
                              ┌──────────────────────────┐
                              │    Menú de Sensores      │
                              │  (SensorMenuActivity)    │
                              └─────────────┬────────────┘
                                            │
          ┌─────────────────────────────────┼─────────────────────────────────┐
          │                                 │                                 │
          ▼                                 ▼                                 ▼
┌──────────────────┐             ┌──────────────────┐             ┌──────────────────┐
│  Meteorológico   │             │ Contador Tráfico │             │    Semáforos     │
│  weather_station │             │  traffic_counter │             │  traffic_light   │
└──────────────────┘             └──────────────────┘             └──────────────────┘
                                            │
                                            ▼
                                  ┌──────────────────┐
                                  │ Pantallas Info   │
                                  │     display      │
                                  └──────────────────┘
```

---

## 📦 Dependencias

- **OSMDroid 6.1.18** - Mapas OpenStreetMap
- **Paho MQTT 1.2.5** - Cliente MQTT
- **Retrofit 2.9.0** - Cliente HTTP REST
