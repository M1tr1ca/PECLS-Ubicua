# 🏗️ Arquitectura del Sistema

## 📐 Arquitectura en 3 Capas

```
┌─────────────────────────────┐
│  CAPA COMUNICACIÓN (MQTT)   │
└─────────────────────────────┘
              ↕ WiFi
┌─────────────────────────────┐
│  CAPA PROCESAMIENTO (ESP32) │
│  + FreeRTOS (Tareas LED)    │
└─────────────────────────────┘
              ↕ I2C/ADC
┌─────────────────────────────┐
│  CAPA SENSORES & ACTUADORES │
└─────────────────────────────┘
```

## 🔌 Conexiones Hardware

| Componente | Pin ESP32 | Tipo | Notas |
|------------|-----------|------|-------|
| BME280 SDA | GPIO 21 | I2C | Pull-up 4.7kΩ, Addr 0x76 |
| BME280 SCL | GPIO 22 | I2C | Pull-up 4.7kΩ |
| LED Rojo | GPIO 12 | Digital | Alerta local, 220Ω |
| LED Alarma 1 | GPIO 4 | Digital | Control remoto, 220Ω |
| LED Alarma 2 | GPIO 5 | Digital | Control remoto, 220Ω |
| LED Alarma 3 | GPIO 13 | Digital | Control remoto, 220Ω |
| Display A | GPIO 26 | Digital | Cátodo común |
| Display B | GPIO 25 | Digital | Cátodo común |
| Display C | GPIO 19 | Digital | Cátodo común |
| Display D | GPIO 18 | Digital | Cátodo común |
| Display E | GPIO 23 | Digital | Cátodo común |
| Display F | GPIO 27 | Digital | Cátodo común |
| Display G | GPIO 14 | Digital | Cátodo común |

## 🧩 Componentes Software

### 1. ComputacionUbicua.ino
- Inicialización del sistema
- Bucle principal (5 segundos)
- Control display 7 segmentos (manual por GPIO)
- Tabla de patrones de dígitos 0-9
- Generación JSON con timestamp NTP
- Control LED local (Temp > 35°C, Hum > 80%)

### 2. ESP32_UTILS.hpp
- Conexión WiFi
- Reconexión automática
- Eventos de red

### 3. ESP32_Utils_MQTT.hpp
- Cliente MQTT
- Publicación de datos
- Procesamiento de comandos
- **Tareas FreeRTOS para LEDs** (4 tareas)
- Gestión de niveles de alerta

### 4. config.h
- Credenciales WiFi (iPhone, 12345678)
- Parámetros MQTT (Broker: 172.20.10.11)
- Identificación sensor (ST_1617, Calle Pepe Hillo)
- Ubicación GPS (Hortaleza, Madrid)
- Pines GPIO (4 LEDs + 7 segmentos display)
- Umbrales alertas (Temp > 35°C, Hum > 80%)

## 🔄 Flujo de Datos

```
1. INICIALIZACIÓN
   → Configurar GPIO
   → Conectar WiFi
   → Conectar MQTT
   → Inicializar sensores
   → Configurar display

2. CICLO (cada 5s)
   → Leer BME280 (Temp, Hum, Presión)
   → Calcular altitud
   → Controlar LED rojo (alerta local)
   → Crear JSON con timestamp NTP
   → Publicar MQTT

3. CONTROL REMOTO (vía MQTT)
   → Recibir alert_level (0-4)
   → Eliminar tarea LED anterior
   → Crear nueva tarea FreeRTOS
   → Actualizar display

4. MONITOREO
   → Verificar WiFi
   → Verificar MQTT
   → Escuchar comandos
```

## 📊 Sistema de Alertas con FreeRTOS

```cpp
// Gestión de tareas
TaskHandle_t ledTaskHandle = NULL;

// Al recibir alert_level:
if(ledTaskHandle != NULL){
    vTaskDelete(ledTaskHandle);  // Eliminar tarea anterior
    // Apagar LEDs
}

// Crear nueva tarea según nivel
switch(alertLevel) {
    case 1: xTaskCreatePinnedToCore(TareaControlLed_Uno, ...);
    case 2: xTaskCreatePinnedToCore(TareaControlLed_Dos, ...);
    case 3: xTaskCreatePinnedToCore(TareaControlLed_Tres, ...);
    case 4: xTaskCreatePinnedToCore(TareaControlLed_Cuatro, ...);
}
```

### Patrones de Parpadeo

| Nivel | LEDs | Frecuencia | Período |
|-------|------|------------|---------|
| 0 | Ninguno | - | - |
| 1 | 1 LED | 1 Hz | 700ms ON/OFF |
| 2 | 2 LEDs | 2 Hz | 300ms ON/OFF |
| 3 | 3 LEDs | 4 Hz | 150ms ON/OFF |
| 4 | 3 LEDs | 10 Hz | 50ms ON/OFF |

## 📐 Display 7 Segmentos

**Tipo:** Cátodo común (SA52-11EWA)  
**Control:** Manual por GPIO (sin librería)

```cpp
// Tabla de patrones para dígitos 0-9
// Orden: A, B, C, D, E, F, G
const byte DIGIT_PATTERNS[10][7] = {
  {0,0,0,0,0,0,1}, // 0
  {1,0,0,1,1,1,1}, // 1
  ...
};

// Función DisplayNumber(int number)
// Activa/desactiva segmentos según patrón
void DisplayNumber(int number) {
    for (int i = 0; i < 7; i++) {
        digitalWrite(SEGMENT_PINS[i], DIGIT_PATTERNS[number][i]);
    }
}
```

## 🔐 Validación de Datos

```cpp
// Rangos válidos
Temperatura: -40°C a 85°C
Humedad: 0% a 100%
Presión: 800 hPa a 1200 hPa
CAQI: 0 a 150
Alert Level: 0 a 4
```

## 📈 Optimizaciones

- **BME280**: Oversampling 16x en presión
- **Buffer MQTT**: 1024 bytes
- **FreeRTOS**: Gestión eficiente de tareas LED
- **Intervalo lectura**: 30s (configurable)
- **Reconexión**: Automática WiFi/MQTT

---

*Arquitectura modular con gestión de tareas en tiempo real*
