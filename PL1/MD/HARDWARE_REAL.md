# 🔧 Configuración de Hardware Real

## 📦 Hardware Utilizado

Este documento describe el hardware **real** que se utiliza en este proyecto.

### ✅ Lista de Componentes

| Componente | Cantidad | Modelo/Especificaciones |
|------------|----------|------------------------|
| **Microcontrolador** | 1 | ESP32 DevKit v1 |
| **Sensor Ambiental** | 1 | BME280 (Temperatura, Humedad, Presión) |
| **Sensor de Aire** | 1 | MQ-135 (Calidad del aire) |
| **LED RGB** | 1 | Cátodo común |
| **Ventilador** | 1 | 5V DC |
| **Relay** | 2 | 5V para control de actuadores |

**Total: 5 componentes principales** (2 sensores + 3 actuadores)

---

## 🌡️ BME280 - Sensor Ambiental Todo-en-Uno

### Especificaciones Técnicas

- **Fabricante**: Bosch Sensortec
- **Interfaz**: I2C (400 kHz máx)
- **Dirección I2C**: 0x76 (por defecto) o 0x77
- **Voltaje**: 1.71V - 3.6V
- **Consumo**: 3.6 µA @ 1Hz (modo normal)

### Mediciones

#### 🌡️ Temperatura
- **Rango**: -40°C a +85°C
- **Precisión**: ±1.0°C (0°C - 65°C)
- **Resolución**: 0.01°C

#### 💧 Humedad Relativa
- **Rango**: 0% - 100%
- **Precisión**: ±3% RH
- **Resolución**: 0.008% RH
- **Tiempo de respuesta**: 1 segundo (τ63%)

#### 📏 Presión Atmosférica
- **Rango**: 300 hPa - 1100 hPa
- **Precisión**: ±1 hPa (0°C - 65°C)
- **Resolución**: 0.18 Pa

### Conexión al ESP32

```
BME280          ESP32
------          -----
VCC    ------>  3.3V
GND    ------>  GND
SDA    ------>  GPIO 21 (SDA)
SCL    ------>  GPIO 22 (SCL)
SDO    ------>  GND (para dirección 0x76)
CSB    ------>  3.3V (modo I2C)
```

### Código de Inicialización

```cpp
#include <Wire.h>
#include <Adafruit_BME280.h>

Adafruit_BME280 bme;

void setup() {
    Wire.begin(21, 22);  // SDA, SCL
    
    if (!bme.begin(0x76)) {
        Serial.println("No se encontró el BME280");
        return;
    }
    
    // Configurar modo de muestreo
    bme.setSampling(Adafruit_BME280::MODE_NORMAL,
                    Adafruit_BME280::SAMPLING_X2,  // Temp
                    Adafruit_BME280::SAMPLING_X16, // Presión
                    Adafruit_BME280::SAMPLING_X1,  // Humedad
                    Adafruit_BME280::FILTER_X16,
                    Adafruit_BME280::STANDBY_MS_500);
}

void loop() {
    float temp = bme.readTemperature();        // °C
    float humidity = bme.readHumidity();       // %
    float pressure = bme.readPressure()/100.0; // hPa
    
    Serial.printf("T: %.1f°C, H: %.1f%%, P: %.1f hPa\n",
                  temp, humidity, pressure);
    delay(1000);
}
```

### 🔍 Verificación de Dirección I2C

Si el sensor no se detecta, verifica la dirección I2C:

```cpp
#include <Wire.h>

void setup() {
    Serial.begin(115200);
    Wire.begin(21, 22);
    Serial.println("Escaneando I2C...");
    
    for (byte addr = 1; addr < 127; addr++) {
        Wire.beginTransmission(addr);
        if (Wire.endTransmission() == 0) {
            Serial.printf("Dispositivo encontrado en: 0x%02X\n", addr);
        }
    }
}

void loop() {}
```

---

## 🏭 MQ-135 - Sensor de Calidad del Aire

### Especificaciones Técnicas

- **Fabricante**: Módulo AZDelivery o similar
- **Interfaz**: Salida analógica
- **Voltaje de operación**: 5V DC (algunos modelos 3.3V)
- **Consumo**: ~800 mW (150 mA @ 5V)
- **Resistencia de carga (RL)**: 10 kΩ (recomendado)

### Gases Detectados

| Gas | Rango de Detección | Aplicación |
|-----|-------------------|------------|
| **CO2** | 10 - 1000 ppm | Calidad del aire interior |
| **NH3** | 10 - 300 ppm | Amoniaco |
| **NOx** | 10 - 1000 ppm | Óxidos de nitrógeno |
| **Alcohol** | 10 - 300 ppm | Vapores de alcohol |
| **Benceno** | 10 - 1000 ppm | Compuestos orgánicos |
| **Humo** | - | Detección general |

### Conexión al ESP32

```
MQ-135          ESP32
------          -----
VCC    ------>  5V (o 3.3V según modelo)
GND    ------>  GND
AO     ------>  GPIO 34 (ADC1_CH6)
DO     ------>  (No usado en este proyecto)
```

**⚠️ IMPORTANTE**: 
- El MQ-135 requiere **20-30 minutos de precalentamiento** antes de dar lecturas estables.
- Si alimentas a 5V, la salida analógica podría exceder los 3.3V del ESP32. Usa un divisor de voltaje:

```
AO ─── 10kΩ ──┬── GPIO 34
              │
            10kΩ
              │
             GND
```

### Calibración y Conversión a AQI

#### Fórmula de Conversión

El sensor entrega una resistencia variable según la concentración de gases:

```cpp
#define MQ135_PIN 34
#define MQ135_RL 10.0           // Resistencia de carga en kΩ
#define MQ135_RO_CLEAN_AIR 3.6  // Ratio Rs/Ro en aire limpio

int ReadAirQuality() {
    // Leer valor analógico (0-4095 en ESP32)
    int sensorValue = analogRead(MQ135_PIN);
    
    // Convertir a voltaje (0-3.3V)
    float voltage = (sensorValue / 4095.0) * 3.3;
    
    // Calcular resistencia del sensor
    float Rs = ((3.3 * MQ135_RL) / voltage) - MQ135_RL;
    
    // Calcular ratio Rs/Ro
    float ratio = Rs / MQ135_RO_CLEAN_AIR;
    
    // Convertir a ppm de CO2
    float ppm = 116.6020682 * pow(ratio, -2.769034857);
    
    // Convertir a AQI
    int aqi;
    if (ppm < 400) {
        aqi = map(ppm, 0, 400, 0, 50);        // Bueno
    } else if (ppm < 1000) {
        aqi = map(ppm, 400, 1000, 51, 100);   // Moderado
    } else if (ppm < 2000) {
        aqi = map(ppm, 1000, 2000, 101, 150); // Dañino sensibles
    } else if (ppm < 5000) {
        aqi = map(ppm, 2000, 5000, 151, 200); // Dañino
    } else {
        aqi = map(ppm, 5000, 10000, 201, 300); // Muy dañino
    }
    
    return constrain(aqi, 0, 500);
}
```

#### Escala AQI (Air Quality Index)

| AQI | Categoría | Color | Descripción |
|-----|-----------|-------|-------------|
| 0-50 | Bueno | 🟢 Verde | Calidad del aire satisfactoria |
| 51-100 | Moderado | 🟡 Amarillo | Aceptable; sensibles precaución |
| 101-150 | Dañino (sensibles) | 🟠 Naranja | Grupos sensibles afectados |
| 151-200 | Dañino | 🔴 Rojo | Todos pueden experimentar efectos |
| 201-300 | Muy dañino | 🟣 Morado | Alerta de salud |
| 301-500 | Peligroso | 🟤 Marrón | Emergencia de salud |

### 🔧 Calibración del MQ-135

1. **Precalentamiento**: Deja el sensor encendido 24-48 horas en aire limpio
2. **Medir Ro**: Toma 50 lecturas en aire limpio y calcula el promedio
3. **Actualizar config.h**: Ajusta `MQ135_RO_CLEAN_AIR` con el valor calculado

```cpp
// Código de calibración (ejecutar una sola vez)
void calibrate_MQ135() {
    Serial.println("Calibrando MQ-135 en aire limpio...");
    float roSum = 0;
    
    for (int i = 0; i < 50; i++) {
        int sensorValue = analogRead(MQ135_PIN);
        float voltage = (sensorValue / 4095.0) * 3.3;
        float Rs = ((3.3 * MQ135_RL) / voltage) - MQ135_RL;
        roSum += Rs;
        delay(500);
    }
    
    float Ro = roSum / 50.0;
    Serial.printf("Ro calculado: %.2f kΩ\n", Ro);
    Serial.println("Actualiza MQ135_RO_CLEAN_AIR en config.h");
}
```

---

## 🚦 LED RGB - Indicador Visual

### Especificaciones

- **Tipo**: LED RGB de cátodo común
- **Voltaje**: 2.0V - 3.3V por canal
- **Corriente**: 20 mA máximo por canal
- **Control**: PWM (8 bits, 0-255)

### Conexión con Resistencias

```
ESP32                LED RGB
-----                -------
GPIO 25 ─── 220Ω ──> R (Rojo)
GPIO 26 ─── 220Ω ──> G (Verde)
GPIO 27 ─── 220Ω ──> B (Azul)
GND         -------> Cátodo común (-)
```

### Código de Control

```cpp
#define LED_RED_PIN   25
#define LED_GREEN_PIN 26
#define LED_BLUE_PIN  27

void setup() {
    pinMode(LED_RED_PIN, OUTPUT);
    pinMode(LED_GREEN_PIN, OUTPUT);
    pinMode(LED_BLUE_PIN, OUTPUT);
}

void SetLED(int r, int g, int b) {
    analogWrite(LED_RED_PIN, r);
    analogWrite(LED_GREEN_PIN, g);
    analogWrite(LED_BLUE_PIN, b);
}

// Indicadores de estado
void IndicateOK()      { SetLED(0, 255, 0); }    // Verde
void IndicateWarning() { SetLED(255, 100, 0); }  // Naranja
void IndicateError()   { SetLED(255, 0, 0); }    // Rojo
void IndicateInfo()    { SetLED(0, 0, 255); }    // Azul
```

---

## 🌀 Ventilador - Control de Temperatura

### Especificaciones

- **Voltaje**: 5V DC
- **Corriente**: 100-200 mA (típico)
- **Control**: Relay o MOSFET

### Conexión con Relay

```
ESP32           Relay          Ventilador
-----           -----          ----------
GPIO 32  --->   IN             
5V       --->   VCC            
GND      --->   GND            
                COM    --->    5V fuente
                NO     --->    Ventilador (+)
                               Ventilador (-) --> GND fuente
```

### Código de Control

```cpp
#define FAN_PIN 32
#define TEMP_FAN_THRESHOLD 30.0

void setup() {
    pinMode(FAN_PIN, OUTPUT);
    digitalWrite(FAN_PIN, LOW);  // Apagado inicialmente
}

void loop() {
    float temp = bme.readTemperature();
    
    if (temp > TEMP_FAN_THRESHOLD) {
        digitalWrite(FAN_PIN, HIGH);  // Encender ventilador
        Serial.println("🌀 Ventilador ENCENDIDO");
    } else {
        digitalWrite(FAN_PIN, LOW);   // Apagar ventilador
    }
}
```

---

## 🔥 Calefactor - Control de Temperatura Baja

### Especificaciones

- **Elemento**: Resistencia calefactora o lámpara incandescente
- **Voltaje**: 5V-12V DC (según elemento)
- **Corriente**: Variable (medir con multímetro)
- **Control**: Relay obligatorio

### Conexión con Relay

```
ESP32           Relay          Calefactor
-----           -----          ----------
GPIO 33  --->   IN             
5V       --->   VCC            
GND      --->   GND            
                COM    --->    12V fuente
                NO     --->    Calefactor (+)
                               Calefactor (-) --> GND fuente
```

### Código de Control

```cpp
#define HEATER_PIN 33
#define TEMP_HEATER_THRESHOLD 10.0

void setup() {
    pinMode(HEATER_PIN, OUTPUT);
    digitalWrite(HEATER_PIN, LOW);  // Apagado inicialmente
}

void loop() {
    float temp = bme.readTemperature();
    
    if (temp < TEMP_HEATER_THRESHOLD) {
        digitalWrite(HEATER_PIN, HIGH);  // Encender calefactor
        Serial.println("🔥 Calefactor ENCENDIDO");
    } else {
        digitalWrite(HEATER_PIN, LOW);   // Apagar calefactor
    }
}
```

---

## 📋 Lista de Comprobación de Hardware

Antes de ejecutar el código, verifica:

- [ ] **BME280**
  - [ ] Conectado a GPIO 21 (SDA) y GPIO 22 (SCL)
  - [ ] Alimentado a 3.3V
  - [ ] Se detecta en dirección I2C 0x76 o 0x77
  
- [ ] **MQ-135**
  - [ ] Conectado a GPIO 34
  - [ ] Alimentado correctamente (5V o 3.3V)
  - [ ] Precalentado al menos 20 minutos
  - [ ] Divisor de voltaje si usa 5V
  
- [ ] **LED RGB**
  - [ ] Resistencias de 220Ω en cada canal
  - [ ] Cátodo común a GND
  - [ ] Conectado a GPIO 25, 26, 27
  
- [ ] **Ventilador**
  - [ ] Relay conectado a GPIO 32
  - [ ] Fuente de alimentación adecuada (5V)
  
- [ ] **Calefactor**
  - [ ] Relay conectado a GPIO 33
  - [ ] Fuente de alimentación adecuada
  - [ ] Relay con capacidad suficiente

---

## 🔧 Solución de Problemas

### BME280 no detectado

1. Ejecuta el escáner I2C para verificar la dirección
2. Verifica las conexiones SDA/SCL
3. Asegúrate de usar 3.3V, no 5V
4. Algunos módulos tienen pull-ups, otros no (añade 4.7kΩ si es necesario)

### MQ-135 da lecturas erráticas

1. Verifica que lleve al menos 20 minutos encendido
2. Comprueba el divisor de voltaje si usas 5V
3. Añade un capacitor de 100nF entre AO y GND para filtrar ruido
4. Recalibra el sensor

### Actuadores no responden

1. Verifica que los relays tengan suficiente corriente
2. Comprueba que la fuente de alimentación sea adecuada
3. Mide con multímetro si hay voltaje en la salida del relay
4. Los relays activos en bajo (LOW) se activan con digitalWrite(pin, LOW)

---

## 📚 Referencias y Datasheets

- **BME280**: [Datasheet Bosch](https://www.bosch-sensortec.com/media/boschsensortec/downloads/datasheets/bst-bme280-ds002.pdf)
- **MQ-135**: [Datasheet Hanwei](https://www.winsen-sensor.com/d/files/air-quality/mq135-gas-sensor-by-winsen.pdf)
- **ESP32**: [Documentación Espressif](https://docs.espressif.com/projects/esp-idf/en/latest/esp32/)

---

✅ **Configuración de hardware completada y documentada**
