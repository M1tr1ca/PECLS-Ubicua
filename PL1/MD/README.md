# 🌦️ Estación Meteorológica IoT - ESP32# 🌦️ Estación Meteorológica IoT - ESP32# 🌦️ Estación Meteorológica IoT - Ciudad 4.0



## 📋 Descripción



Sistema IoT que monitoriza condiciones ambientales en tiempo real y transmite datos vía MQTT. Proyecto PECL1 - Computación Ubicua (UAH).## 📋 Descripción## 📋 Descripción General



**Autores:** Grupo 5 - Juan Pérez Resa y David Nicolás Mitrica  

**Ubicación:** Calle Pepe Hillo, Hortaleza, Madrid

Sistema IoT que monitoriza condiciones ambientales en tiempo real y transmite datos vía MQTT. Proyecto PECL1 - Computación Ubicua (UAH).Este proyecto implementa una **Estación Meteorológica IoT** para el proyecto PECL1 de Computación Ubicua de la Universidad de Alcalá de Henares. El sistema captura datos meteorológicos en tiempo real y los transmite mediante protocolo MQTT a un broker central, formando parte de una red de sensores para una ciudad inteligente.

## 🔧 Componentes



### Hardware

## 🔧 Componentes### 🎯 Objetivo

- **ESP32** - Microcontrolador WiFi

- **BME280** - Sensor 3 en 1: Temperatura/Humedad/Presión (I2C)

- **3x LEDs de Alerta** - Indicadores visuales (control remoto vía MQTT)

- **Display 7 Segmentos** - Muestra nivel de alerta (0-4)### HardwareDesarrollar un dispositivo IoT que monitorice condiciones ambientales y atmosféricas en tiempo real, proporcionando datos precisos para:

- **1x LED Rojo** - Indicador de condiciones anormales locales

- **ESP32** - Microcontrolador WiFi- Monitoreo de calidad del aire urbana

### Software

- **BME280** - Temp/Humedad/Presión (I2C)- Predicción meteorológica local

- Lenguaje: C++ (Arduino)

- Protocolo: MQTT- **MQ-135** - Calidad del aire (Analógico)- Alertas de condiciones anormales

- Broker: Servidor local (172.20.10.11)

- Formato: JSON- **LED Rojo** - Indicador de alertas (x3 LEDs en sistema actual)- Análisis de patrones ambientales

- RTOS: FreeRTOS (gestión tareas LED)

- Display: Control manual por GPIO



## 📊 Datos Monitorizados### Software## 🔧 Componentes del Sistema



| Parámetro | Sensor | Rango | Unidad |- Lenguaje: C++ (Arduino)

|-----------|--------|-------|--------|

| Temperatura | BME280 | -40 a 85 | °C |- Protocolo: MQTT### Hardware Implementado

| Humedad | BME280 | 0 a 100 | % |

| Presión | BME280 | 300 a 1100 | hPa |- Broker: test.mosquitto.org

| Altitud | BME280 (calculado) | Variable | metros |

- Formato: JSON#### **Sensores (2)**

**Total:** 1 sensor (BME280) + 2 actuadores (LEDs + Display)

- RTOS: FreeRTOS (gestión tareas LED)1. **BME280** - Sensor I2C multifunción

## 📡 Comunicación MQTT

   - 🌡️ Temperatura (-40°C a 85°C)

**Broker:** 172.20.10.11:1883 (Servidor local)  

**Cliente:** LAB08JAV-G5## 📊 Datos Monitorizados   - 💧 Humedad relativa (0-100%)



**Tópicos:**   - 📏 Presión atmosférica (300-1100 hPa)



- Publicación: `uah/alcala/weather/data`| Parámetro | Sensor | Rango | Unidad |   - Comunicación: I2C (dirección 0x76)

- Control: `uah/alcala/weather/control`

|-----------|--------|-------|--------|   - Precisión: ±1°C, ±3% RH, ±1 hPa

**Ejemplo JSON:**

| Temperatura | BME280 | -10 a 40 | °C |

```json

{| Humedad | BME280 | 20 a 90 | % |2. **MQ-135** - Sensor de calidad del aire

  "sensor_id": "ST_1617",

  "sensor_type": "Estación metereológica IoT",| Presión | BME280 | 980 a 1050 | hPa |   - 🏭 Detección de gases: CO2, NH3, NOx, alcohol, benceno, humo

  "street_id": "ST_1617",

  "timestamp": "2025-11-03 14:30:45:123",| CAQI | MQ-135 | 0 a 150 | índice |   - Salida: Analógica (0-3.3V)

  "location": {

    "latitude": 40.4513367,   - Índice CAQI: 0-150 (Common Air Quality Index)

    "longitude": -3.6409307,

    "altitude_meters": 658.5,## 📡 Comunicación MQTT

    "district": "Hortaleza",

    "neighborhood": "Hortaleza"#### **Actuadores (1)**

  },

  "data": {**Tópicos:**1. **LED Rojo** - Indicador visual de alertas

    "temperature_celsius": 23.5,

    "humidity_percent": 65.3,- Publicación: `uah/alcala/weather/data`   - Alerta de temperatura alta (>35°C)

    "atmospheric_pressure_hpa": 1013.2,

    "wind_speed_kmh": 0.0,- Control: `uah/alcala/weather/control`   - Alerta de humedad excesiva (>80%)

    "wind_direction_degrees": 0,

    "uv_index": 0   - Alerta de calidad del aire peligrosa (CAQI >75)

  }

}**Ejemplo JSON:**

```

```json### Software

## ⚙️ Sistema de Alertas

{

### Control Local (Automático)

  "sensor_id": "WS_ALC_01",- **Plataforma**: ESP32 (Wokwi Simulator / Hardware Real)

- **LED Rojo (GPIO 12)**: Se activa cuando Temp > 35°C o Humedad > 80%

  "timestamp": "2025-10-13T14:30:45.123",- **Lenguaje**: C++ (Arduino Framework)

### Control Remoto (vía MQTT)

  "data": {- **Protocolo IoT**: MQTT

**Niveles de Alerta (0-4):**

    "temperature_celsius": 23.5,- **Formato de datos**: JSON

- **Nivel 0**: Sin alerta - LEDs apagados - Display '0'

- **Nivel 1**: Alerta BAJA - 1 LED parpadeando lento (700ms) - Display '1'    "humidity_percent": 65.3,- **Broker MQTT**: test.mosquitto.org (público)

- **Nivel 2**: Alerta MEDIA - 2 LEDs parpadeando medio (300ms) - Display '2'

- **Nivel 3**: Alerta ALTA - 3 LEDs parpadeando rápido (150ms) - Display '3'    "air_quality_index": 45,

- **Nivel 4**: Alerta CRÍTICA - 3 LEDs muy rápido (50ms) - Display '4'

    "atmospheric_pressure_hpa": 1013.2## 📊 Datos Monitorizados

**Comandos MQTT:**

  }

```json

{"alert_level": 2}}| Parámetro | Sensor | Rango | Unidad |

{"command": "reset"}

``````|-----------|--------|-------|--------|



**Display:** Muestra el nivel de alerta (0-4) mediante control manual de GPIO (sin librería SevSeg)| Temperatura | BME280 | -10 a 40 | °C |



## 📁 Estructura## ⚙️ Sistema de Alertas| Humedad | BME280 | 20 a 90 | % |



```text| Presión Atmosférica | BME280 | 980 a 1050 | hPa |

PL1/

├── ComputacionUbicua.ino       # Programa principal**Niveles de Alerta (0-4):**| Calidad del Aire (CAQI) | MQ-135 | 0 a 150 | índice |

├── config.h                    # Configuración WiFi/MQTT

├── ESP32_UTILS.hpp            # Gestión WiFi- **Nivel 0**: Sin alerta - LEDs apagados - Display '0'

├── ESP32_Utils_MQTT.hpp       # Gestión MQTT + FreeRTOS

├── ej.json                    # Ejemplos mensajes MQTT- **Nivel 1**: Alerta BAJA - 1 LED parpadeando lento (700ms) - Display '1'## 📡 Comunicación MQTT

└── MD/                        # Documentación

    ├── ARQUITECTURA.md        # Detalles técnicos- **Nivel 2**: Alerta MEDIA - 2 LEDs parpadeando medio (300ms) - Display '2'

    ├── INSTALACION.md         # Guía de instalación

    └── API_MQTT.md            # Especificación API- **Nivel 3**: Alerta ALTA - 3 LEDs parpadeando rápido (150ms) - Display '3'### Tópicos

```

- **Nivel 4**: Alerta CRÍTICA - 3 LEDs muy rápido (50ms) - Display '4'

## 🚀 Inicio Rápido

- **Publicación de datos**: `uah/alcala/weather/data`

1. **Configurar WiFi y MQTT** en `config.h`:

   - SSID y contraseña WiFi**Control remoto vía MQTT:**- **Control remoto**: `uah/alcala/weather/control`

   - Broker MQTT (172.20.10.11)

   - Identificación del sensor (ST_1617)```json

   - Ubicación GPS (Hortaleza)

2. **Instalar librerías**:{"alert_level": 2}### Configuración del Broker

   - PubSubClient

   - ArduinoJson{"command": "reset"}

   - Adafruit BME280

   - Adafruit Unified Sensor``````

   - Wire (integrada)

3. **Conectar hardware**: BME280 (I2C) + 4 LEDs + Display 7-seg (cátodo común)Broker: test.mosquitto.org

4. **Compilar y cargar** en ESP32

5. **Publicación automática** cada 5 segundos## 📁 EstructuraPuerto: 1883



## 📚 Documentación AdicionalQoS: 1



- [ARQUITECTURA.md](ARQUITECTURA.md) - Detalles técnicos y FreeRTOS```Cliente: ESP32_WS_ALC_01

- [INSTALACION.md](INSTALACION.md) - Guía completa de instalación

- [API_MQTT.md](API_MQTT.md) - Especificación MQTT y comandosPL1/```



---├── ComputacionUbicua.ino       # Programa principal



*Universidad de Alcalá de Henares - Computación Ubicua - PECL1*  ├── config.h                    # Configuración WiFi/MQTT### Formato JSON de Datos

*Grupo 5 - Hortaleza, Madrid*

├── ESP32_UTILS.hpp            # Gestión WiFi

├── ESP32_Utils_MQTT.hpp       # Gestión MQTT + FreeRTOS```json

└── MD/                        # Documentación{

    ├── ARQUITECTURA.md        # Detalles técnicos  "sensor_id": "WS_ALC_01",

    ├── INSTALACION.md         # Guía de instalación  "sensor_type": "weather",

    └── API_MQTT.md            # Especificación API  "street_id": "ST_ALC_001",

```  "timestamp": "2025-10-13T14:30:45.123",

  "location": {

## 🚀 Inicio Rápido    "latitude": 40.4823,

    "longitude": -3.3618,

1. **Configurar WiFi** en `config.h`    "altitude_meters": 588.0,

2. **Instalar librerías**: PubSubClient, ArduinoJson, Adafruit BME280    "district": "Alcalá de Henares",

3. **Conectar hardware** según esquema    "neighborhood": "Centro"

4. **Compilar y cargar** en ESP32  },

  "data": {

## 📚 Documentación Adicional    "temperature_celsius": 23.5,

    "humidity_percent": 65.3,

- [ARQUITECTURA.md](ARQUITECTURA.md) - Detalles técnicos    "air_quality_index": 45,

- [INSTALACION.md](INSTALACION.md) - Guía completa    "atmospheric_pressure_hpa": 1013.2

- [API_MQTT.md](API_MQTT.md) - Especificación MQTT  }

}

---```



*Universidad de Alcalá de Henares - Computación Ubicua - PECL1*## 📍 Ubicación


- **Ciudad**: Alcalá de Henares, España
- **Coordenadas**: 40.4823°N, 3.3618°W
- **Altitud**: 588 metros
- **Zona**: Centro

## ⚙️ Configuración

### Intervalos de Operación

- **Lectura de sensores**: 30 segundos
- **Publicación MQTT**: 30 segundos
- **Verificación WiFi**: Continua

### Umbrales de Alerta

- **Temperatura alta**: >35°C → LED Rojo ON
- **Humedad alta**: >80% → LED Rojo ON
- **CAQI peligroso**: >75 → LED Rojo ON

## 🚀 Características Principales

✅ **Monitoreo en tiempo real** de 4 parámetros ambientales  
✅ **Transmisión MQTT** con formato JSON estandarizado  
✅ **Sistema de alertas visuales** mediante LED  
✅ **Reconexión automática** WiFi y MQTT  
✅ **Validación de datos** con rangos esperados  
✅ **Cálculo de CAQI** según normativa europea  
✅ **Control remoto** mediante comandos MQTT  

## 📁 Estructura del Proyecto

```
PL1/
├── main.ino                    # Programa principal
├── config.h                    # Configuración del sistema
├── config.json                 # Parámetros en JSON
├── ESP32_UTILS.hpp            # Utilidades WiFi
├── ESP32_Utils_MQTT.hpp       # Utilidades MQTT
├── MD/                        # Documentación
│   ├── README.md              # Este archivo
│   ├── ARQUITECTURA.md        # Documentación técnica
│   ├── INSTALACION.md         # Guía de instalación
│   └── API_MQTT.md            # Especificación MQTT/JSON
└── drawio/                    # Diagramas
    ├── arquitectura.drawio    # Diagrama de arquitectura
    ├── flujo_datos.drawio     # Diagrama de flujo
    └── comunicacion_mqtt.drawio # Diagrama MQTT
```

## 👥 Información del Proyecto

- **Universidad**: Universidad de Alcalá de Henares
- **Asignatura**: Computación Ubicua
- **Práctica**: PECL1
- **Tipo de dispositivo**: Estación Meteorológica (Weather Station)
- **ID del sensor**: WS_ALC_01

## 📚 Documentación Adicional

Para información detallada, consulta:

- [Arquitectura del Sistema](ARQUITECTURA.md) - Detalles técnicos y componentes
- [Guía de Instalación](INSTALACION.md) - Instrucciones de configuración
- [API MQTT](API_MQTT.md) - Especificación de mensajes y protocolo

## 🔗 Enlaces Útiles

- [Datasheet BME280](https://www.bosch-sensortec.com/products/environmental-sensors/humidity-sensors-bme280/)
- [Datasheet MQ-135](https://www.winsen-sensor.com/sensors/voc-sensor/mq135.html)
- [Mosquitto MQTT Broker](https://test.mosquitto.org/)
- [Wokwi ESP32 Simulator](https://wokwi.com/)

---

*Desarrollado para el proyecto Ciudad 4.0 - Smart City UAH*

