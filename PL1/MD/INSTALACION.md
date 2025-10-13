# 📦 Guía de Instalación - Estación Meteorológica IoT

## 📋 Requisitos Previos

### Software Necesario

1. **Arduino IDE** (versión 1.8.19 o superior) o **PlatformIO**
   - Descarga: https://www.arduino.cc/en/software

2. **Soporte para ESP32**
   - Board Manager URL: `https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json`

3. **Drivers USB** (para ESP32 físico)
   - CP2102/CH340: Según el modelo de tu ESP32

### Bibliotecas Requeridas

| Biblioteca | Versión | Función |
|------------|---------|---------|
| WiFi | (integrada ESP32) | Conectividad WiFi |
| PubSubClient | 2.8.0+ | Cliente MQTT |
| ArduinoJson | 6.21.0+ | Manejo de JSON |
| Wire | (integrada) | Comunicación I2C |
| Adafruit BME280 | 2.2.2+ | Sensor BME280 |
| Adafruit Unified Sensor | 1.1.9+ | Librería base sensores |

## 🔧 Instalación Paso a Paso

### Opción 1: Simulador Wokwi (Recomendado para pruebas)

#### 1. Acceder a Wokwi

```
1. Ve a: https://wokwi.com/
2. Crea una cuenta o inicia sesión
3. Crea un nuevo proyecto ESP32
```

#### 2. Cargar el Código

```
1. Copia el contenido de main.ino en el editor
2. Agrega los archivos de cabecera:
   - ESP32_UTILS.hpp
   - ESP32_Utils_MQTT.hpp
   - config.h
3. Clic en "Add file" para cada uno
```

#### 3. Configurar diagram.json

Crea o edita `diagram.json` con las conexiones:

```json
{
  "version": 1,
  "author": "UAH - Estación Meteorológica",
  "editor": "wokwi",
  "parts": [
    { "type": "wokwi-esp32-devkit-v1", "id": "esp", "top": 0, "left": 0 },
    { "type": "wokwi-bme280", "id": "bme", "top": 100, "left": 150 },
    { "type": "wokwi-mq135", "id": "mq135", "top": 100, "left": 300 },
    { "type": "wokwi-led", "id": "led", "top": 50, "left": 450, "attrs": { "color": "red" } },
    { "type": "wokwi-resistor", "id": "r1", "top": 80, "left": 450, "attrs": { "value": "220" } }
  ],
  "connections": [
    [ "esp:3V3", "bme:VCC", "red" ],
    [ "esp:GND.1", "bme:GND", "black" ],
    [ "esp:21", "bme:SDA", "green" ],
    [ "esp:22", "bme:SCL", "yellow" ],
    [ "esp:5V", "mq135:VCC", "red" ],
    [ "esp:GND.2", "mq135:GND", "black" ],
    [ "esp:34", "mq135:AO", "blue" ],
    [ "esp:25", "r1:1", "orange" ],
    [ "r1:2", "led:A", "orange" ],
    [ "led:C", "esp:GND.3", "black" ]
  ]
}
```

#### 4. Ejecutar la Simulación

```
1. Clic en el botón verde "Start Simulation"
2. Abre el Monitor Serial (abajo)
3. Observa las lecturas y mensajes MQTT
```

### Opción 2: Hardware Real (ESP32 Físico)

#### 1. Instalar Arduino IDE

```bash
# Windows
Descarga el instalador desde arduino.cc y ejecuta

# macOS
brew install --cask arduino

# Linux (Ubuntu/Debian)
sudo apt update
sudo apt install arduino
```

#### 2. Configurar Soporte ESP32

1. Abrir Arduino IDE
2. Ir a **Archivo → Preferencias**
3. En "Gestor de URLs Adicionales de Tarjetas":
   ```
   https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json
   ```
4. Ir a **Herramientas → Placa → Gestor de Tarjetas**
5. Buscar "ESP32" e instalar "ESP32 by Espressif Systems"

#### 3. Instalar Bibliotecas

**Método A: Gestor de Bibliotecas (Recomendado)**

1. **Herramientas → Administrar Bibliotecas**
2. Buscar e instalar cada una:

```
- PubSubClient (by Nick O'Leary)
- ArduinoJson (by Benoit Blanchon)
- Adafruit BME280 Library
- Adafruit Unified Sensor
```

**Método B: Línea de Comandos (PlatformIO)**

```ini
# platformio.ini
[env:esp32dev]
platform = espressif32
board = esp32dev
framework = arduino
lib_deps = 
    knolleary/PubSubClient@^2.8
    bblanchon/ArduinoJson@^6.21.3
    adafruit/Adafruit BME280 Library@^2.2.2
    adafruit/Adafruit Unified Sensor@^1.1.9
monitor_speed = 115200
```

#### 4. Conexión del Hardware

**Lista de Componentes:**

- 1x ESP32 DevKit v1
- 1x Sensor BME280 (I2C)
- 1x Sensor MQ-135
- 1x LED Rojo (5mm)
- 1x Resistencia 220Ω
- 2x Resistencias 4.7kΩ (pull-up I2C)
- Cables Dupont
- Protoboard

**Conexiones BME280:**

```
ESP32          BME280
─────────────────────
3.3V     ──→   VCC
GND      ──→   GND
GPIO21   ──→   SDA (con pull-up 4.7kΩ a 3.3V)
GPIO22   ──→   SCL (con pull-up 4.7kΩ a 3.3V)
```

**Conexiones MQ-135:**

```
ESP32          MQ-135
─────────────────────
5V       ──→   VCC
GND      ──→   GND
GPIO34   ──→   AOUT
```

> ⚠️ **Importante**: El MQ-135 requiere calentamiento de 24-48 horas para lecturas precisas.

**Conexión LED:**

```
ESP32          LED Rojo
─────────────────────────
GPIO25   ──→   Resistencia 220Ω ──→ LED(+)
GND      ──→   LED(-)
```

**Esquema en Protoboard:**

```
    BME280                MQ-135              LED
      │                     │                  │
    ┌─┴─────────────────────┴──────────────────┴─┐
    │  [Protoboard]                               │
    │                                             │
    │  Pull-up    Pull-up                         │
    │   4.7kΩ     4.7kΩ                          │
    │     │         │                             │
    └─────┴─────────┴─────────────────────────────┘
          │         │
       ESP32 GPIO 21 & 22
```

#### 5. Configurar el Proyecto

1. **Descargar los archivos del proyecto:**

```bash
git clone <repositorio>
cd PL1
```

2. **Editar config.h con tus credenciales:**

```cpp
// CONFIGURACIÓN WIFI
const char* ssid = "TU_WIFI";           // ← Cambia esto
const char* password = "TU_PASSWORD";    // ← Cambia esto

// CONFIGURACIÓN MQTT (opcional)
const char* MQTT_BROKER_ADRESS = "test.mosquitto.org";
const uint16_t MQTT_PORT = 1883;
```

3. **Verificar pines en config.h:**

```cpp
#define BME_SDA 21
#define BME_SCL 22
#define MQ135_PIN 34
#define LED_RED_PIN 25
```

#### 6. Compilar y Cargar

1. **Configurar la placa:**
   - **Herramientas → Placa** → "ESP32 Dev Module"
   - **Herramientas → Puerto** → Seleccionar puerto COM/ttyUSB

2. **Configuración adicional:**
   ```
   Upload Speed: 115200
   CPU Frequency: 240MHz
   Flash Size: 4MB
   Partition Scheme: Default 4MB with spiffs
   ```

3. **Compilar:**
   - **Sketch → Verificar/Compilar** (Ctrl+R)

4. **Cargar:**
   - **Sketch → Subir** (Ctrl+U)

5. **Monitor Serial:**
   - **Herramientas → Monitor Serie** (Ctrl+Shift+M)
   - Velocidad: **115200 baud**

## 🔍 Verificación de la Instalación

### 1. Verificar Conexión WiFi

Al abrir el monitor serial, deberías ver:

```
═══════════════════════════════════════════
  ESTACIÓN METEOROLÓGICA IoT
  Universidad de Alcalá de Henares
  PECL1 - Computación Ubicua
═══════════════════════════════════════════

Configurando pines...
✓ Pines configurados
===========================================
Conectando a WiFi...
===========================================
..........
✓ WiFi Conectado
  SSID: TU_WIFI
  IP: 192.168.1.XXX
  Señal: -45 dBm
===========================================
```

### 2. Verificar Sensores

```
Inicializando sensores...
✓ BME280 inicializado correctamente
✓ Sensor MQ-135 configurado y disponible
✓ Inicialización de sensores completada
```

### 3. Verificar MQTT

```
===========================================
Inicializando MQTT...
===========================================
  Broker: test.mosquitto.org
  Puerto: 1883
===========================================
→ Conectando a MQTT... ✓ Conectado
✓ Suscrito a: uah/alcala/weather/control
```

### 4. Verificar Lecturas

```
===========================================
📊 Leyendo sensores...
===========================================
Lecturas de sensores:
  🌡️  Temperatura: 23.5°C
  💧 Humedad: 65.3%
  📏 Presión: 1013.2 hPa
  🏭 Calidad del Aire (CAQI): 45
===========================================
```

### 5. Verificar Publicación MQTT

```
===========================================
📤 Publicando datos...
===========================================
JSON generado:
{"sensor_id":"WS_ALC_01","sensor_type":"weather",...}
-------------------------------------------
✓ Mensaje #1 enviado
===========================================
```

## 🐛 Solución de Problemas

### Problema: No conecta a WiFi

**Síntomas:**
```
✗ Error: No se pudo conectar a WiFi
  Verifica las credenciales en config.h
```

**Soluciones:**
1. Verificar SSID y contraseña en `config.h`
2. Asegurar que la red es 2.4GHz (ESP32 no soporta 5GHz)
3. Verificar que no hay caracteres especiales en la contraseña
4. Intentar con IP estática: `ConnectWifi_STA(true)`

### Problema: BME280 no detectado

**Síntomas:**
```
⚠ BME280 no encontrado. Verifica las conexiones.
```

**Soluciones:**
1. Verificar conexiones I2C (SDA → GPIO21, SCL → GPIO22)
2. Añadir resistencias pull-up de 4.7kΩ en SDA y SCL
3. Verificar dirección I2C con scanner I2C:

```cpp
// Scanner I2C (cargar en ESP32)
#include <Wire.h>
void setup() {
  Serial.begin(115200);
  Wire.begin(21, 22);
  Serial.println("Escaneando I2C...");
  for(byte i = 1; i < 127; i++) {
    Wire.beginTransmission(i);
    if (Wire.endTransmission() == 0) {
      Serial.printf("Dispositivo encontrado: 0x%02X\n", i);
    }
  }
}
void loop() {}
```

4. Cambiar dirección en config.h si es necesario:
```cpp
#define BME280_ADDRESS 0x77  // Probar 0x77 en vez de 0x76
```

### Problema: MQ-135 valores extraños

**Síntomas:**
```
⚠ MQ-135: Señal inusual. Verifica las conexiones.
```

**Soluciones:**
1. Verificar que AOUT está conectado a GPIO34 (pin ADC)
2. Alimentar con 5V (no 3.3V)
3. Calentar el sensor 24-48 horas antes de usar
4. Verificar que la resistencia de carga (RL) es correcta

### Problema: No conecta a MQTT

**Síntomas:**
```
✗ Error, rc=-2 | Reintentando en 5s...
```

**Códigos de error MQTT:**
- `-4`: Timeout de conexión → Verificar broker y puerto
- `-3`: Conexión perdida → Verificar WiFi
- `-2`: Conexión fallida → Verificar broker accesible
- `-1`: Desconectado → Estado normal antes de conectar
- `5`: No autorizado → Verificar credenciales (si aplica)

**Soluciones:**
1. Verificar que `test.mosquitto.org` es accesible
2. Probar con otro broker público: `broker.hivemq.com`
3. Verificar firewall/antivirus
4. Usar broker local:
```cpp
const char* MQTT_BROKER_ADRESS = "192.168.1.XXX";  // Tu PC con Mosquitto
```

### Problema: LED no enciende

**Soluciones:**
1. Verificar polaridad del LED (pata larga → +)
2. Verificar resistencia 220Ω en serie
3. Probar manualmente:
```cpp
digitalWrite(LED_RED_PIN, HIGH);
delay(2000);
digitalWrite(LED_RED_PIN, LOW);
```
4. Cambiar GPIO si es necesario

### Problema: Compilación fallida

**Error: "Biblioteca no encontrada"**
```
fatal error: Adafruit_BME280.h: No such file or directory
```
→ Instalar biblioteca faltante desde el gestor

**Error: "ESP32 board not found"**
```
Error compiling for board ESP32 Dev Module
```
→ Reinstalar soporte ESP32 en gestor de tarjetas

**Error: "Multiple libraries found"**
```
Multiple libraries were found for "WiFi.h"
```
→ Seleccionar la biblioteca de ESP32 en el menú

## 📊 Configuración del Broker MQTT

### Opción A: Usar Broker Público (test.mosquitto.org)

Ya configurado por defecto en `config.h`:

```cpp
const char* MQTT_BROKER_ADRESS = "test.mosquitto.org";
const uint16_t MQTT_PORT = 1883;
```

**Ventajas:** Sin configuración, acceso inmediato  
**Desventajas:** Público, sin seguridad, puede estar saturado

### Opción B: Broker Local (Mosquitto)

**Instalación en Windows:**
```powershell
# Descargar de mosquitto.org/download
# Instalar y ejecutar
net start mosquitto
```

**Instalación en Linux:**
```bash
sudo apt update
sudo apt install mosquitto mosquitto-clients
sudo systemctl start mosquitto
sudo systemctl enable mosquitto
```

**Instalación en macOS:**
```bash
brew install mosquitto
brew services start mosquitto
```

**Configurar en config.h:**
```cpp
const char* MQTT_BROKER_ADRESS = "192.168.1.XXX";  // IP de tu PC
const uint16_t MQTT_PORT = 1883;
```

## 🧪 Pruebas del Sistema

### 1. Prueba de Sensores

Ejecuta lecturas individuales:

```cpp
// En setup(), después de InitSensors()
Serial.println("=== PRUEBA DE SENSORES ===");
Serial.printf("Temp: %.2f°C\n", ReadTemperature());
Serial.printf("Hum: %.2f%%\n", ReadHumidity());
Serial.printf("Press: %.2f hPa\n", ReadPressure());
Serial.printf("CAQI: %d\n", ReadAirQuality());
```

### 2. Prueba de LED

```cpp
// Parpadeo de prueba
for(int i = 0; i < 5; i++) {
    digitalWrite(LED_RED_PIN, HIGH);
    delay(500);
    digitalWrite(LED_RED_PIN, LOW);
    delay(500);
}
```

### 3. Prueba de MQTT (Publicación)

Usa MQTT Explorer o mosquitto_sub:

```bash
# Instalar mosquitto-clients
sudo apt install mosquitto-clients  # Linux
brew install mosquitto              # macOS

# Suscribirse al tópico
mosquitto_sub -h test.mosquitto.org -t "uah/alcala/weather/data" -v
```

Deberías ver los mensajes JSON cada 30 segundos.

### 4. Prueba de Control Remoto

Envía comando de reset:

```bash
mosquitto_pub -h test.mosquitto.org \
  -t "uah/alcala/weather/control" \
  -m '{"command":"reset"}'
```

El ESP32 debería reiniciarse.

## 📱 Herramientas de Monitoreo

### MQTT Explorer (Recomendado)

1. Descargar: http://mqtt-explorer.com/
2. Conectar a `test.mosquitto.org:1883`
3. Ver tópicos `uah/alcala/weather/#`
4. Inspeccionar mensajes JSON

### Mosquitto Clients (Línea de comandos)

```bash
# Suscribirse a todos los tópicos
mosquitto_sub -h test.mosquitto.org -t "#" -v

# Suscribirse solo a datos
mosquitto_sub -h test.mosquitto.org -t "uah/alcala/weather/data"

# Publicar comando
mosquitto_pub -h test.mosquitto.org \
  -t "uah/alcala/weather/control" \
  -m '{"command":"status"}'
```

### Node-RED (Dashboard Visual)

1. Instalar Node-RED: `npm install -g node-red`
2. Ejecutar: `node-red`
3. Abrir: http://localhost:1880
4. Importar flow para visualizar datos

## 🔄 Actualización del Firmware

### OTA (Over-The-Air) - Futuro

Para actualizar remotamente sin cable USB:

```cpp
// Añadir en setup()
#include <ArduinoOTA.h>

ArduinoOTA.setHostname("weather-station");
ArduinoOTA.begin();

// Añadir en loop()
ArduinoOTA.handle();
```

### USB (Método Actual)

1. Modificar código
2. Compilar
3. Conectar ESP32
4. Subir sketch

## 📝 Checklist de Instalación

- [ ] Arduino IDE instalado
- [ ] Soporte ESP32 configurado
- [ ] Todas las bibliotecas instaladas
- [ ] Hardware conectado correctamente
- [ ] config.h editado con credenciales WiFi
- [ ] Código compilado sin errores
- [ ] ESP32 programado correctamente
- [ ] WiFi conectado exitosamente
- [ ] Sensores inicializados
- [ ] MQTT conectado al broker
- [ ] Lecturas de sensores válidas
- [ ] Mensajes JSON publicados
- [ ] LED funcionando correctamente
- [ ] Monitor MQTT recibiendo datos

## 📚 Recursos Adicionales

- [Documentación ESP32](https://docs.espressif.com/projects/esp-idf/en/latest/esp32/)
- [PubSubClient Library](https://pubsubclient.knolleary.net/)
- [ArduinoJson Documentation](https://arduinojson.org/)
- [BME280 Datasheet](https://www.bosch-sensortec.com/products/environmental-sensors/humidity-sensors-bme280/)
- [MQ-135 Datasheet](https://www.winsen-sensor.com/sensors/voc-sensor/mq135.html)
- [Mosquitto MQTT Broker](https://mosquitto.org/)

---

*Si encuentras algún problema no documentado aquí, consulta los logs del monitor serial y verifica las conexiones físicas.*

