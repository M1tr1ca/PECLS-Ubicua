# 🌦️ Estación Meteorológica IoT - Ciudad 4.0

## 📋 Descripción General

Este proyecto implementa una **Estación Meteorológica IoT** para el proyecto PECL1 de Computación Ubicua de la Universidad de Alcalá de Henares. El sistema captura datos meteorológicos en tiempo real y los transmite mediante protocolo MQTT a un broker central, formando parte de una red de sensores para una ciudad inteligente.

### 🎯 Objetivo

Desarrollar un dispositivo IoT que monitorice condiciones ambientales y atmosféricas en tiempo real, proporcionando datos precisos para:
- Monitoreo de calidad del aire urbana
- Predicción meteorológica local
- Alertas de condiciones anormales
- Análisis de patrones ambientales

## 🔧 Componentes del Sistema

### Hardware Implementado

#### **Sensores (2)**
1. **BME280** - Sensor I2C multifunción
   - 🌡️ Temperatura (-40°C a 85°C)
   - 💧 Humedad relativa (0-100%)
   - 📏 Presión atmosférica (300-1100 hPa)
   - Comunicación: I2C (dirección 0x76)
   - Precisión: ±1°C, ±3% RH, ±1 hPa

2. **MQ-135** - Sensor de calidad del aire
   - 🏭 Detección de gases: CO2, NH3, NOx, alcohol, benceno, humo
   - Salida: Analógica (0-3.3V)
   - Índice CAQI: 0-150 (Common Air Quality Index)

#### **Actuadores (1)**
1. **LED Rojo** - Indicador visual de alertas
   - Alerta de temperatura alta (>35°C)
   - Alerta de humedad excesiva (>80%)
   - Alerta de calidad del aire peligrosa (CAQI >75)

### Software

- **Plataforma**: ESP32 (Wokwi Simulator / Hardware Real)
- **Lenguaje**: C++ (Arduino Framework)
- **Protocolo IoT**: MQTT
- **Formato de datos**: JSON
- **Broker MQTT**: test.mosquitto.org (público)

## 📊 Datos Monitorizados

| Parámetro | Sensor | Rango | Unidad |
|-----------|--------|-------|--------|
| Temperatura | BME280 | -10 a 40 | °C |
| Humedad | BME280 | 20 a 90 | % |
| Presión Atmosférica | BME280 | 980 a 1050 | hPa |
| Calidad del Aire (CAQI) | MQ-135 | 0 a 150 | índice |

## 📡 Comunicación MQTT

### Tópicos

- **Publicación de datos**: `uah/alcala/weather/data`
- **Control remoto**: `uah/alcala/weather/control`

### Configuración del Broker

```
Broker: test.mosquitto.org
Puerto: 1883
QoS: 1
Cliente: ESP32_WS_ALC_01
```

### Formato JSON de Datos

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

## 📍 Ubicación

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

