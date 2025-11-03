# 📚 Índice de Documentación

## 🗂️ Documentos Disponibles

### [README.md](README.md)

**Descripción general del proyecto**

- Sistema de alertas con 4 LEDs
- Sensor BME280 (3 en 1)
- Display 7 segmentos (control manual)
- Configuración MQTT local
- Autores: Grupo 5
- Ubicación: Hortaleza, Madrid

### [ARQUITECTURA.md](ARQUITECTURA.md)

**Detalles técnicos del sistema**

- Conexiones hardware completas (BME280 + 4 LEDs + Display)
- FreeRTOS: 4 tareas para LEDs
- Display manual por GPIO (sin SevSeg)
- Patrones de parpadeo por nivel
- Timestamp NTP sincronizado
- Control local + remoto

### [INSTALACION.md](INSTALACION.md)

**Guía de instalación completa**

- Sin librería SevSeg (control manual)
- Configuración broker local (172.20.10.11)
- Conexión BME280 + 4 LEDs + Display
- Pines GPIO detallados
- Troubleshooting actualizado
- Publicación cada 5 segundos

### [API_MQTT.md](API_MQTT.md)

**Especificación API MQTT**

- Broker local: 172.20.10.11
- Cliente: LAB08JAV-G5
- Sensor ID: ST_1617
- Formato JSON actualizado
- Comandos alert_level (0-4)
- Ejemplos con broker local

## 🎯 Rutas de Aprendizaje

### 👤 Nuevo en el Proyecto

1. README.md → Entender componentes
2. INSTALACION.md → Configurar broker local
3. API_MQTT.md → Probar comandos

### 🔧 Instalador

1. INSTALACION.md → Hardware BME280 + 4 LEDs
2. ARQUITECTURA.md → Verificar conexiones GPIO
3. API_MQTT.md → Test publicación/suscripción

### 💻 Desarrollador

1. ARQUITECTURA.md → FreeRTOS + Display manual
2. ComputacionUbicua.ino → Tabla patrones display
3. API_MQTT.md → Integrar con broker local

### 🎓 Estudiante

1. README.md → Descripción proyecto
2. ARQUITECTURA.md → Análisis técnico
3. Todos los docs → Entrega completa

## 📊 Mapa del Proyecto

```text
Estación Meteorológica IoT - Grupo 5
│
├── 📖 Documentación
│   ├── README.md (Grupo 5, Hortaleza)
│   ├── ARQUITECTURA.md (BME280 + Display manual)
│   ├── INSTALACION.md (Broker 172.20.10.11)
│   └── API_MQTT.md (ST_1617)
│
├── 💾 Código
│   ├── ComputacionUbicua.ino (Main + Patrones display)
│   ├── ESP32_UTILS.hpp (WiFi iPhone)
│   ├── ESP32_Utils_MQTT.hpp (4 tareas FreeRTOS)
│   └── config.h (Hortaleza, GPIO completos)
│
└── ⚙️ Hardware
    ├── ESP32 DevKit
    ├── BME280 (I2C, 0x76)
    ├── 4x LEDs (GPIO 12, 4, 5, 13)
    └── Display 7-seg (GPIO 26,25,19,18,23,27,14)
```

## 🔍 Búsqueda Rápida

| Tema | Documento |
|------|-----------|
| Grupo 5, Hortaleza | README.md |
| Broker 172.20.10.11 | API_MQTT.md, INSTALACION.md |
| Sensor ST_1617 | README.md, API_MQTT.md |
| BME280 único sensor | README.md, ARQUITECTURA.md |
| Display manual (sin SevSeg) | ARQUITECTURA.md, INSTALACION.md |
| 4 LEDs (1 local + 3 remotos) | ARQUITECTURA.md |
| Pines GPIO display | ARQUITECTURA.md, config.h |
| Timestamp NTP | ARQUITECTURA.md |
| Publicación 5s | README.md, API_MQTT.md |
| FreeRTOS 4 tareas | ARQUITECTURA.md |

## 📝 Glosario

| Término | Significado |
|---------|-------------|
| ST_1617 | ID del sensor (Calle Pepe Hillo) |
| LAB08JAV-G5 | Cliente MQTT (Grupo 5) |
| BME280 | Sensor 3 en 1 (Temp/Hum/Presión) |
| GPIO 12 | LED rojo (alerta local) |
| GPIO 4,5,13 | LEDs alarma (control remoto) |
| Cátodo común | Tipo display 7-seg (HIGH=ON) |
| 172.20.10.11 | IP broker MQTT local |
| Hortaleza | Distrito Madrid (ubicación) |
| NTP | Protocolo sincronización tiempo |
| 0x76 | Dirección I2C del BME280 |



---

*Universidad de Alcalá - Computación Ubicua - PECL1*  
*Grupo 5 - Juan Pérez Resa y David Nicolás Mitrica*
