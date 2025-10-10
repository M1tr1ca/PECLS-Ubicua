# 📦 Guía de Instalación Detallada

## 🔌 Esquema de Conexión Detallado

### Diagrama de Bloques

```
                    ESP32
        ┌───────────────────────────┐
        │                           │
        │    ┌─────────────┐       │
        │    │   WiFi      │       │
        │    └─────────────┘       │
        │                           │
VCC ────┤ 3.3V                     │
GND ────┤ GND                      │
        │                           │
        │  SENSORES:               │
        │                           │
DHT22 ──┤ GPIO 4                   │
BMP_SDA─┤ GPIO 21 (I2C)            │
BMP_SCL─┤ GPIO 22 (I2C)            │
UV ─────┤ GPIO 34 (ADC)            │
WIND_S ─┤ GPIO 35 (ADC)            │
WIND_D ─┤ GPIO 36 (ADC)            │
AIR_Q ──┤ GPIO 39 (ADC)            │
        │                           │
        │  ACTUADORES:              │
        │                           │
LED_R ──┤ GPIO 25 (PWM)            │
LED_G ──┤ GPIO 26 (PWM)            │
LED_B ──┤ GPIO 27 (PWM)            │
FAN ────┤ GPIO 32 → Relay          │
HEAT ───┤ GPIO 33 → Relay          │
        │                           │
        └───────────────────────────┘
```

---

## 🛠️ Instalación Paso a Paso

### PASO 1: Preparar el Entorno de Desarrollo

#### 1.1 Instalar Arduino IDE

1. Descarga Arduino IDE desde: https://www.arduino.cc/en/software
2. Instala la versión 2.x o superior

#### 1.2 Instalar Soporte para ESP32

1. Abre Arduino IDE
2. Ve a **File → Preferences**
3. En "Additional Board Manager URLs", añade:
   ```
   https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json
   ```
4. Ve a **Tools → Board → Boards Manager**
5. Busca "esp32" y instala **ESP32 by Espressif Systems**

#### 1.3 Instalar Librerías

Ve a **Sketch → Include Library → Manage Libraries** e instala:

| Librería | Autor | Versión |
|----------|-------|---------|
| DHT sensor library | Adafruit | 1.4.4+ |
| Adafruit Unified Sensor | Adafruit | 1.1.9+ |
| Adafruit BMP280 Library | Adafruit | 2.6.6+ |
| PubSubClient | Nick O'Leary | 2.8+ |
| ArduinoJson | Benoit Blanchon | 6.21.3+ |

---

### PASO 2: Conexiones del Hardware

#### 2.1 Sensor DHT22 (Temperatura y Humedad)

```
DHT22          ESP32
━━━━━━━━━━━━━━━━━━━━━
VCC (+)   →    3.3V
DATA      →    GPIO 4
GND (-)   →    GND
```

**Nota:** Añade una resistencia pull-up de 10kΩ entre VCC y DATA.

#### 2.2 Sensor BMP280 (Presión Atmosférica)

```
BMP280         ESP32
━━━━━━━━━━━━━━━━━━━━━
VCC       →    3.3V
GND       →    GND
SDA       →    GPIO 21
SCL       →    GPIO 22
```

#### 2.3 Sensor UV (Analógico)

```
UV Sensor      ESP32
━━━━━━━━━━━━━━━━━━━━━
VCC       →    3.3V
OUT       →    GPIO 34
GND       →    GND
```

#### 2.4 Anemómetro (Velocidad del Viento)

```
Anemómetro     ESP32
━━━━━━━━━━━━━━━━━━━━━
VCC       →    3.3V
OUT       →    GPIO 35
GND       →    GND
```

#### 2.5 Veleta (Dirección del Viento)

```
Veleta         ESP32
━━━━━━━━━━━━━━━━━━━━━
VCC       →    3.3V
OUT       →    GPIO 36
GND       →    GND
```

#### 2.6 Sensor MQ-135 (Calidad del Aire)

```
MQ-135         ESP32
━━━━━━━━━━━━━━━━━━━━━
VCC       →    5V (si tiene regulador)
             o 3.3V
AOUT      →    GPIO 39
GND       →    GND
```

#### 2.7 LED RGB

```
LED RGB        ESP32
━━━━━━━━━━━━━━━━━━━━━
R         →    GPIO 25 (con resistencia 220Ω)
G         →    GPIO 26 (con resistencia 220Ω)
B         →    GPIO 27 (con resistencia 220Ω)
Cátodo    →    GND
```

#### 2.8 Ventilador (con Módulo Relay)

```
Ventilador + Relay     ESP32
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Relay VCC         →    5V
Relay GND         →    GND
Relay IN          →    GPIO 32
Relay COM         →    VCC Ventilador
Relay NO          →    Ventilador (+)
Ventilador (-)    →    GND
```

#### 2.9 Calefactor (con Módulo Relay)

```
Calefactor + Relay     ESP32
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Relay VCC         →    5V
Relay GND         →    GND
Relay IN          →    GPIO 33
Relay COM         →    VCC Calefactor
Relay NO          →    Calefactor (+)
Calefactor (-)    →    GND
```

---

### PASO 3: Configuración del Software

#### 3.1 Modificar config.h

Abre `config.h` y configura tus credenciales:

```cpp
// Tu red WiFi
const char* ssid = "TU_RED_WIFI";
const char* password = "TU_CONTRASEÑA";

// Broker MQTT (puedes usar el público o tu propio broker)
const char* MQTT_BROKER_ADRESS = "test.mosquitto.org";  // o tu IP
const uint16_t MQTT_PORT = 1883;
```

#### 3.2 Verificar Configuración de Pines

Si tus conexiones son diferentes, modifica los defines en `config.h`:

```cpp
#define DHT_PIN 4
#define BMP_SDA 21
#define BMP_SCL 22
// ... etc
```

---

### PASO 4: Compilar y Cargar

#### 4.1 Seleccionar la Placa

1. En Arduino IDE: **Tools → Board → ESP32 Arduino → ESP32 Dev Module**
2. Configurar parámetros:
   - **Upload Speed:** 115200
   - **Flash Frequency:** 80MHz
   - **Flash Mode:** QIO
   - **Flash Size:** 4MB
   - **Partition Scheme:** Default 4MB with spiffs

#### 4.2 Seleccionar Puerto

1. Conecta el ESP32 al PC vía USB
2. **Tools → Port → COMx** (Windows) o **/dev/ttyUSBx** (Linux/Mac)

#### 4.3 Compilar

1. Haz clic en el botón **Verify** (✓)
2. Espera a que compile sin errores

#### 4.4 Cargar al ESP32

1. Haz clic en el botón **Upload** (→)
2. Espera a que termine la carga
3. Si aparece "Connecting...", presiona el botón **BOOT** del ESP32

---

### PASO 5: Verificar Funcionamiento

#### 5.1 Abrir Serial Monitor

1. **Tools → Serial Monitor**
2. Configurar velocidad: **115200 baud**
3. Deberías ver:

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
  SSID: TuRed
  IP: 192.168.1.200
  Señal: -45 dBm
===========================================

===========================================
Inicializando MQTT...
===========================================
  Broker: test.mosquitto.org
  Puerto: 1883
===========================================

→ Conectando a MQTT... ✓ Conectado
✓ Suscrito a: uah/alcala/weather/control

Inicializando sensores...
✓ DHT22 inicializado
✓ BMP280 inicializado
✓ Todos los sensores listos

===========================================
📊 Leyendo sensores...
===========================================
Lecturas:
  🌡️  Temperatura: 22.3°C
  💧 Humedad: 65.4%
  📏 Presión: 1013.2 hPa
  ☀️  Índice UV: 3
  💨 Viento: 12.5 km/h @ 180°
  🏭 Calidad aire (AQI): 45
===========================================

✓ Sistema inicializado correctamente
✓ Estación lista para operar
```

---

## 🧪 Pruebas del Sistema

### Prueba 1: Verificar Publicación MQTT

En tu PC, instala mosquitto client:

**Windows:**
```powershell
choco install mosquitto
```

**Linux/Mac:**
```bash
sudo apt-get install mosquitto-clients  # Ubuntu/Debian
brew install mosquitto                  # macOS
```

**Suscribirse al tópico:**
```bash
mosquitto_sub -h test.mosquitto.org -t "uah/alcala/weather/data" -v
```

Deberías ver mensajes JSON cada 30 segundos.

### Prueba 2: Enviar Comandos

#### Activar ventilador:
```bash
mosquitto_pub -h test.mosquitto.org \
  -t "uah/alcala/weather/control" \
  -m '{"command":"fan_on","value":true}'
```

#### Cambiar LED a rojo:
```bash
mosquitto_pub -h test.mosquitto.org \
  -t "uah/alcala/weather/control" \
  -m '{"command":"led_rgb","r":255,"g":0,"b":0}'
```

#### Forzar lectura inmediata:
```bash
mosquitto_pub -h test.mosquitto.org \
  -t "uah/alcala/weather/control" \
  -m '{"command":"read_now"}'
```

### Prueba 3: Verificar Actuadores Automáticos

1. **Ventilador:** Calienta el DHT22 con tu mano o secador (>30°C)
2. **Calefactor:** Enfría el DHT22 con hielo (<10°C)
3. **LED:** Observa los cambios de color según las condiciones

---

## 📱 Configurar tu Propio Broker MQTT (Opcional)

### Opción 1: Mosquitto en Windows

```powershell
# Instalar Mosquitto
choco install mosquitto

# Iniciar servicio
net start mosquitto

# Configurar en config.h
const char* MQTT_BROKER_ADRESS = "192.168.1.X";  // Tu IP local
```

### Opción 2: Mosquitto en Linux

```bash
# Instalar
sudo apt-get update
sudo apt-get install mosquitto mosquitto-clients

# Iniciar servicio
sudo systemctl start mosquitto
sudo systemctl enable mosquitto

# Verificar
sudo systemctl status mosquitto
```

### Opción 3: Broker en la Nube

- **HiveMQ Cloud:** https://www.hivemq.com/mqtt-cloud-broker/
- **CloudMQTT:** https://www.cloudmqtt.com/
- **AWS IoT Core:** https://aws.amazon.com/iot-core/

---

## 🔧 Calibración de Sensores

### DHT22
No requiere calibración, pero puedes validar con un termómetro de referencia.

### BMP280
```cpp
// En setup(), después de bmp.begin():
float seaLevelPressure = 1013.25;  // Ajusta según tu ubicación
float altitude = bmp.readAltitude(seaLevelPressure);
```

### Sensor UV
Calibra según tu modelo específico:
```cpp
// En ReadUVIndex():
float voltage = (sensorValue / 4095.0) * 3.3;
int uvIndex = voltage / 0.1;  // Ajustar según datasheet
```

### Anemómetro
```cpp
// Calibra con velocidades conocidas
float windSpeed = (sensorValue / 4095.0) * MAX_WIND_SPEED;
windSpeed = windSpeed * CALIBRATION_FACTOR;  // Tu factor
```

---

## 📊 Monitoreo en Dashboard (Opcional)

### Node-RED

1. Instala Node-RED:
```bash
npm install -g node-red
```

2. Inicia Node-RED:
```bash
node-red
```

3. Abre: http://localhost:1880

4. Instala el nodo MQTT:
   - Settings → Manage palette → Install → `node-red-dashboard`

5. Crea un flow:
   - MQTT In → Function → Dashboard Gauge

### Grafana + InfluxDB

Para visualización profesional con gráficos históricos:

1. Instala InfluxDB para almacenar datos
2. Instala Telegraf con plugin MQTT
3. Instala Grafana para visualización
4. Conecta todo y crea dashboards

---

## 🎓 Recursos Adicionales

### Tutoriales
- [ESP32 Getting Started](https://randomnerdtutorials.com/getting-started-with-esp32/)
- [MQTT Basics](https://mqtt.org/getting-started/)
- [ArduinoJson Guide](https://arduinojson.org/v6/doc/)

### Documentación
- [ESP32 Pinout](https://randomnerdtutorials.com/esp32-pinout-reference-gpios/)
- [DHT22 Guide](https://learn.adafruit.com/dht)
- [BMP280 Guide](https://learn.adafruit.com/adafruit-bmp280-barometric-pressure-plus-temperature-sensor-breakout)

### Herramientas
- [MQTT Explorer](http://mqtt-explorer.com/) - Cliente MQTT gráfico
- [MQTT.fx](https://mqttfx.jensd.de/) - Otro cliente MQTT
- [Fritzing](https://fritzing.org/) - Diseño de circuitos

---

## ✅ Checklist de Instalación

- [ ] Arduino IDE instalado
- [ ] Soporte ESP32 instalado
- [ ] Todas las librerías instaladas
- [ ] Hardware conectado según esquemas
- [ ] config.h configurado con WiFi
- [ ] Código compilado sin errores
- [ ] ESP32 conectado al PC
- [ ] Código cargado al ESP32
- [ ] Serial Monitor mostrando datos
- [ ] WiFi conectado
- [ ] MQTT conectado
- [ ] Sensores leyendo valores
- [ ] Actuadores funcionando
- [ ] LED indicando estado
- [ ] Datos publicados en MQTT
- [ ] Comandos recibidos correctamente

---

## 🆘 Troubleshooting Común

### Error: "Compilation error: config.h: No such file"
**Solución:** Asegúrate de que todos los archivos (.ino, .h, .hpp) estén en la misma carpeta.

### Error: "DHT sensor library not found"
**Solución:** Instala las librerías: DHT sensor library y Adafruit Unified Sensor.

### Error: "Failed to connect to MQTT broker"
**Solución:**
- Verifica que tengas conexión a Internet
- Prueba con otro broker: `broker.hivemq.com`
- Revisa el puerto (1883)

### Error: "WiFi disconnected repeatedly"
**Solución:**
- Verifica SSID y contraseña
- Asegúrate de usar WiFi 2.4 GHz
- Acércate al router

### Warning: "BMP280 not found"
**Solución:**
- Verifica conexiones I2C (SDA, SCL)
- Revisa la dirección I2C (0x76 o 0x77)
- El código funcionará con valores simulados

---

¡Instalación completada! Tu estación meteorológica IoT está lista para funcionar. 🎉
