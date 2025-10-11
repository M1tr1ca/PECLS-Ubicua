# 📄 Especificación del Formato JSON

## 📋 Formato Completo del Mensaje

Este documento detalla el formato JSON utilizado por la estación meteorológica según las especificaciones del proyecto PECL1.

---

## 📤 Mensaje de Datos (Publicación)

### Estructura Completa

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

---

## 📝 Descripción de Campos

### Nivel Raíz

| Campo | Tipo | Descripción | Ejemplo |
|-------|------|-------------|---------|
| `sensor_id` | String | Identificador único de la estación | "WS_ALC_01" |
| `sensor_type` | String | Tipo de estación (siempre "weather") | "weather" |
| `street_id` | String | Identificador de la calle/ubicación | "ST_ALC_001" |
| `timestamp` | String | Marca temporal ISO 8601 | "2025-10-15T14:32:45.123" |
| `location` | Object | Información de ubicación | {...} |
| `data` | Object | Datos meteorológicos | {...} |

### Objeto `location`

| Campo | Tipo | Descripción | Rango | Ejemplo |
|-------|------|-------------|-------|---------|
| `latitude` | Float | Latitud (grados) | -90 a 90 | 40.4823 |
| `longitude` | Float | Longitud (grados) | -180 a 180 | -3.3618 |
| `altitude_meters` | Float | Altitud sobre el nivel del mar (metros) | 0 a 5000 | 588.0 |
| `district` | String | Distrito/Ciudad | - | "Alcalá de Henares" |
| `neighborhood` | String | Barrio/Zona | - | "Centro" |

### Objeto `data` (Datos Meteorológicos)

| Campo | Tipo | Descripción | Rango | Unidad | Ejemplo |
|-------|------|-------------|-------|--------|---------|
| `temperature_celsius` | Float | Temperatura ambiente | -40 a 60 | °C | 22.3 |
| `humidity_percent` | Float | Humedad relativa | 0 a 100 | % | 65.4 |
| `air_quality_index` | Integer | Índice de calidad del aire (AQI) | 0 a 500 | AQI | 45 |
| `wind_speed_kmh` | Float | Velocidad del viento | 0 a 200 | km/h | 12.5 |
| `wind_direction_degrees` | Integer | Dirección del viento | 0 a 360 | ° | 180 |
| `atmospheric_pressure_hpa` | Float | Presión atmosférica | 800 a 1200 | hPa | 1013.2 |
| `uv_index` | Integer | Índice UV | 0 a 11+ | - | 3 |
| `fan_active` | Boolean | Estado del ventilador (extra) | true/false | - | false |
| `heater_active` | Boolean | Estado del calefactor (extra) | true/false | - | false |

---

## 📥 Mensajes de Control (Suscripción)

### 1. Reiniciar Dispositivo

```json
{
  "command": "reset"
}
```

**Efecto:** Reinicia el ESP32.

---

### 2. Forzar Lectura Inmediata

```json
{
  "command": "read_now"
}
```

**Efecto:** Lee los sensores y publica datos inmediatamente.

---

### 3. Control de Ventilador

```json
{
  "command": "fan_on",
  "value": true
}
```

**Parámetros:**
- `value`: Boolean (true = ON, false = OFF)

**Efecto:** Activa o desactiva el ventilador manualmente.

---

### 4. Control de Calefactor

```json
{
  "command": "heater_on",
  "value": true
}
```

**Parámetros:**
- `value`: Boolean (true = ON, false = OFF)

**Efecto:** Activa o desactiva el calefactor manualmente.

---

### 5. Control de LED RGB

```json
{
  "command": "led_rgb",
  "r": 255,
  "g": 0,
  "b": 0
}
```

**Parámetros:**
- `r`: Integer (0-255) - Componente rojo
- `g`: Integer (0-255) - Componente verde
- `b`: Integer (0-255) - Componente azul

**Efecto:** Cambia el color del LED RGB.

**Colores predefinidos:**
- Rojo: (255, 0, 0)
- Verde: (0, 255, 0)
- Azul: (0, 0, 255)
- Amarillo: (255, 255, 0)
- Cian: (0, 255, 255)
- Magenta: (255, 0, 255)
- Blanco: (255, 255, 255)
- Apagado: (0, 0, 0)

---

## 🔍 Validación de Datos

### Schema JSON (JSON Schema Draft-07)

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Weather Station Data",
  "type": "object",
  "required": ["sensor_id", "sensor_type", "street_id", "timestamp", "location", "data"],
  "properties": {
    "sensor_id": {
      "type": "string",
      "pattern": "^WS_[A-Z]+_[0-9]+$",
      "description": "Identificador único de la estación"
    },
    "sensor_type": {
      "type": "string",
      "enum": ["weather"],
      "description": "Tipo de estación"
    },
    "street_id": {
      "type": "string",
      "pattern": "^ST_[A-Z]+_[0-9]+$",
      "description": "Identificador de la calle"
    },
    "timestamp": {
      "type": "string",
      "format": "date-time",
      "description": "Marca temporal ISO 8601"
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
          "minimum": 0,
          "maximum": 5000
        },
        "district": {
          "type": "string"
        },
        "neighborhood": {
          "type": "string"
        }
      }
    },
    "data": {
      "type": "object",
      "required": [
        "temperature_celsius",
        "humidity_percent",
        "air_quality_index",
        "wind_speed_kmh",
        "wind_direction_degrees",
        "atmospheric_pressure_hpa",
        "uv_index"
      ],
      "properties": {
        "temperature_celsius": {
          "type": "number",
          "minimum": -40,
          "maximum": 60
        },
        "humidity_percent": {
          "type": "number",
          "minimum": 0,
          "maximum": 100
        },
        "air_quality_index": {
          "type": "integer",
          "minimum": 0,
          "maximum": 500
        },
        "wind_speed_kmh": {
          "type": "number",
          "minimum": 0,
          "maximum": 200
        },
        "wind_direction_degrees": {
          "type": "integer",
          "minimum": 0,
          "maximum": 360
        },
        "atmospheric_pressure_hpa": {
          "type": "number",
          "minimum": 800,
          "maximum": 1200
        },
        "uv_index": {
          "type": "integer",
          "minimum": 0,
          "maximum": 15
        },
        "fan_active": {
          "type": "boolean"
        },
        "heater_active": {
          "type": "boolean"
        }
      }
    }
  }
}
```

---

## 🧪 Ejemplos de Validación

### Python - Validar JSON

```python
import json
import jsonschema
from jsonschema import validate

# Schema de validación
schema = {
    "type": "object",
    "required": ["sensor_id", "sensor_type", "data"],
    "properties": {
        "sensor_id": {"type": "string"},
        "sensor_type": {"type": "string", "enum": ["weather"]},
        "data": {
            "type": "object",
            "required": ["temperature_celsius", "humidity_percent"],
            "properties": {
                "temperature_celsius": {"type": "number", "minimum": -40, "maximum": 60},
                "humidity_percent": {"type": "number", "minimum": 0, "maximum": 100}
            }
        }
    }
}

# JSON a validar
data = {
    "sensor_id": "WS_ALC_01",
    "sensor_type": "weather",
    "data": {
        "temperature_celsius": 22.3,
        "humidity_percent": 65.4
    }
}

try:
    validate(instance=data, schema=schema)
    print("✓ JSON válido")
except jsonschema.exceptions.ValidationError as err:
    print(f"❌ JSON inválido: {err}")
```

---

## 📊 Interpretación de Valores

### Índice de Calidad del Aire (AQI)

| Rango | Categoría | Color | Descripción |
|-------|-----------|-------|-------------|
| 0-50 | Buena | 🟢 Verde | Calidad del aire satisfactoria |
| 51-100 | Moderada | 🟡 Amarillo | Aceptable para la mayoría |
| 101-150 | Dañina para grupos sensibles | 🟠 Naranja | Personas sensibles pueden verse afectadas |
| 151-200 | Dañina | 🔴 Rojo | Todos pueden empezar a experimentar efectos |
| 201-300 | Muy dañina | 🟣 Púrpura | Alerta de salud |
| 301-500 | Peligrosa | 🟤 Marrón | Emergencia de salud |

### Índice UV

| Rango | Categoría | Color | Protección |
|-------|-----------|-------|------------|
| 0-2 | Bajo | 🟢 Verde | No se necesita protección |
| 3-5 | Moderado | 🟡 Amarillo | Se necesita protección |
| 6-7 | Alto | 🟠 Naranja | Se requiere protección |
| 8-10 | Muy Alto | 🔴 Rojo | Se requiere protección extra |
| 11+ | Extremo | 🟣 Púrpura | Tome todas las precauciones |

### Dirección del Viento (Grados)

| Grados | Dirección | Símbolo |
|--------|-----------|---------|
| 0° / 360° | Norte | N ↑ |
| 45° | Noreste | NE ↗ |
| 90° | Este | E → |
| 135° | Sureste | SE ↘ |
| 180° | Sur | S ↓ |
| 225° | Suroeste | SW ↙ |
| 270° | Oeste | W ← |
| 315° | Noroeste | NW ↖ |

### Escala de Viento (Beaufort)

| km/h | Beaufort | Descripción |
|------|----------|-------------|
| <1 | 0 | Calma |
| 1-5 | 1 | Ventolina |
| 6-11 | 2 | Brisa muy débil |
| 12-19 | 3 | Brisa débil |
| 20-28 | 4 | Brisa moderada |
| 29-38 | 5 | Brisa fresca |
| 39-49 | 6 | Brisa fuerte |
| 50-61 | 7 | Viento fuerte |
| 62-74 | 8 | Temporal |
| 75-88 | 9 | Temporal fuerte |
| 89-102 | 10 | Temporal muy fuerte |
| 103-117 | 11 | Tempestad |
| >117 | 12 | Huracán |

### Presión Atmosférica

| hPa | Condición |
|-----|-----------|
| < 1000 | Baja presión (mal tiempo posible) |
| 1000-1020 | Normal |
| > 1020 | Alta presión (buen tiempo probable) |

---

## 🔄 Frecuencia de Actualización

- **Lectura de sensores:** Cada 30 segundos (configurable)
- **Publicación MQTT:** Cada 30 segundos
- **Timeout de conexión:** 60 segundos
- **Reintentos de conexión:** Cada 5 segundos

---

## 📐 Precisión de Sensores

### Especificaciones Típicas

| Sensor | Parámetro | Rango | Precisión |
|--------|-----------|-------|-----------|
| DHT22 | Temperatura | -40 a 80°C | ±0.5°C |
| DHT22 | Humedad | 0-100% | ±2-5% |
| BMP280 | Presión | 300-1100 hPa | ±1 hPa |
| BMP280 | Altitud | 0-9000m | ±1m |
| GUVA-S12SD | UV | 0-11+ | ±1 índice |
| MQ-135 | Calidad aire | 10-1000ppm | ±10% |

---

## 🛡️ Manejo de Errores

### Valores por Defecto en Caso de Error

```cpp
// Si un sensor falla, se usan valores seguros
temperature = 20.0;        // Temperatura ambiente típica
humidity = 60.0;           // Humedad media
pressure = 1013.25;        // Presión estándar al nivel del mar
uvIndex = 0;               // Sin radiación UV
windSpeed = 0.0;           // Sin viento
windDirection = 0;         // Norte
airQuality = 50;           // Calidad del aire buena
```

### Mensajes de Error en Serial Monitor

```
⚠ Error leyendo temperatura DHT22
⚠ Lectura de presión fuera de rango
⚠ BMP280 no encontrado. Usando valores simulados.
```

---

## 📈 Tamaño del Mensaje

- **Tamaño típico del JSON:** ~450 bytes
- **Buffer configurado:** 1024 bytes
- **Margen de seguridad:** >100%

---

## 🔗 Referencias

### Estándares

- **ISO 8601:** Formato de fecha y hora
- **JSON RFC 8259:** Especificación JSON
- **MQTT v3.1.1:** Protocolo de mensajería

### APIs Compatibles

Este formato es compatible con:
- OpenWeatherMap API
- Weather Underground API
- APIXU API
- Dark Sky API (parcial)

---

## ✅ Checklist de Validación

Verifica que tu mensaje JSON cumple con:

- [ ] Tiene todos los campos requeridos
- [ ] `sensor_type` es "weather"
- [ ] Timestamp en formato ISO 8601
- [ ] Coordenadas dentro de rangos válidos
- [ ] Temperatura entre -40 y 60°C
- [ ] Humedad entre 0 y 100%
- [ ] Presión entre 800 y 1200 hPa
- [ ] Índice UV entre 0 y 15
- [ ] Dirección del viento entre 0 y 360°
- [ ] Tamaño del mensaje < 1024 bytes
- [ ] JSON bien formado (sin errores de sintaxis)

---

**Documento de Especificación - Versión 1.0**  
*Última actualización: 10 de octubre de 2025*
