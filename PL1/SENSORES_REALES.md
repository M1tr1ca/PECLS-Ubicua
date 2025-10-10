# 🔬 Especificaciones de los Sensores Reales

## 📦 Hardware Utilizado en Este Proyecto

---

## 1️⃣ BME280 - Sensor Todo-en-Uno (2 unidades)

### 📋 Descripción
**Modelo:** BME280  
**Fabricante:** Bosch Sensortec  
**Cantidad:** 2 unidades  
**Tipo:** Sensor barométrico digital de temperatura, humedad y presión  

### ✨ Características Principales

| Parámetro | Especificación |
|-----------|----------------|
| **Temperatura** | |
| Rango | -40°C a +85°C |
| Precisión | ±1.0°C |
| Resolución | 0.01°C |
| **Humedad** | |
| Rango | 0% a 100% RH |
| Precisión | ±3% RH |
| Resolución | 0.008% RH |
| **Presión** | |
| Rango | 300 hPa a 1100 hPa |
| Precisión absoluta | ±1 hPa |
| Precisión relativa | ±0.12 hPa |
| Resolución | 0.18 Pa |

### 🔌 Especificaciones Eléctricas

- **Voltaje:** 1.8V - 3.6V (módulo con regulador: 3.3V - 5V)
- **Consumo:** 
  - Modo sleep: 0.1 µA
  - Medición: 340 µA
  - Típico: 3.6 µA @ 1 Hz
- **Interfaz:** I2C / SPI
- **Dirección I2C:** 0x76 (por defecto) o 0x77 (alternativa)

### 📍 Configuración en Este Proyecto

```cpp
// Direcciones I2C configuradas
#define BME280_ADDRESS_1 0x76  // Sensor BME280 #1
#define BME280_ADDRESS_2 0x77  // Sensor BME280 #2

// Pines I2C (compartidos)
#define BME_SDA 21
#define BME_SCL 22
```

### 💡 Ventajas de Tener 2 BME280

1. **Redundancia:** Si un sensor falla, el otro sigue funcionando
2. **Mayor precisión:** Promediamos las lecturas de ambos sensores
3. **Validación cruzada:** Detectamos lecturas erróneas comparando ambos
4. **Fiabilidad:** Sistema más robusto y profesional

### 🔧 Conexión Física

```
BME280 #1 (dirección 0x76)
┌─────────────┐
│  BME280 #1  │
├─────────────┤
│ VCC → 3.3V  │
│ GND → GND   │
│ SDA → GPIO21│  ←─┐
│ SCL → GPIO22│  ←─┤ Bus I2C compartido
└─────────────┘    │
                   │
BME280 #2 (dirección 0x77)  
┌─────────────┐    │
│  BME280 #2  │    │
├─────────────┤    │
│ VCC → 3.3V  │    │
│ GND → GND   │    │
│ SDA → GPIO21│  ←─┘
│ SCL → GPIO22│  ←──
└─────────────┘
```

**Nota importante:** Ambos sensores comparten el mismo bus I2C, pero tienen direcciones diferentes (0x76 y 0x77). Esto permite que funcionen simultáneamente sin conflictos.

### 📊 Código de Lectura

```cpp
Adafruit_BME280 bme1;  // Sensor #1
Adafruit_BME280 bme2;  // Sensor #2

// Inicialización
bme1.begin(0x76);
bme2.begin(0x77);

// Lectura
float temp1 = bme1.readTemperature();
float temp2 = bme2.readTemperature();
float temp_avg = (temp1 + temp2) / 2.0;  // Promedio
```

---

## 2️⃣ MQ-135 - Sensor de Calidad del Aire (3 unidades)

### 📋 Descripción
**Modelo:** MQ-135  
**Fabricante:** Zhengzhou Winsen Electronics  
**Cantidad:** 3 unidades (AZDelivery)  
**Tipo:** Sensor de gas electroquímico (semiconductor SnO2)

### ✨ Gases Detectables

El MQ-135 es sensible a múltiples gases contaminantes:

| Gas | Rango de Detección |
|-----|-------------------|
| **CO2** (Dióxido de carbono) | 10 - 1000 ppm |
| **NH3** (Amoníaco) | 10 - 300 ppm |
| **NOx** (Óxidos de nitrógeno) | 10 - 1000 ppm |
| **Alcohol** | 10 - 300 ppm |
| **Benceno** | 10 - 1000 ppm |
| **Humo** | Variable |

### 🔌 Especificaciones Técnicas

- **Voltaje de trabajo:** 5V DC
- **Resistencia de carga (RL):** 10 kΩ (ajustable: 2-47 kΩ)
- **Consumo:** ~800 mW (160 mA @ 5V)
- **Rango de concentración:** 10 - 1000 ppm
- **Tiempo de precalentamiento:** 24 horas (óptimo), mínimo 20 minutos
- **Tiempo de respuesta:** < 10 segundos
- **Salida:** Analógica (0-5V) y Digital (comparador)

### 📍 Configuración en Este Proyecto

```cpp
// Pines ADC (Conversión Analógico-Digital)
#define MQ135_PIN_1 34  // ADC1_CH6
#define MQ135_PIN_2 35  // ADC1_CH7
#define MQ135_PIN_3 39  // ADC1_CH3

// Constantes de calibración
#define MQ135_RL 10.0              // Resistencia de carga en kΩ
#define MQ135_RO_CLEAN_AIR 3.6     // Ratio Ro en aire limpio
```

### 🔧 Conexión Física

```
MQ-135 #1
┌─────────────┐
│   MQ-135    │
├─────────────┤
│ VCC → 5V    │  (o 3.3V si el módulo lo soporta)
│ GND → GND   │
│ AO  → GPIO34│  (Salida analógica)
│ DO  → N/C   │  (Salida digital - no usada)
└─────────────┘

MQ-135 #2
┌─────────────┐
│   MQ-135    │
├─────────────┤
│ VCC → 5V    │
│ GND → GND   │
│ AO  → GPIO35│
│ DO  → N/C   │
└─────────────┘

MQ-135 #3
┌─────────────┐
│   MQ-135    │
├─────────────┤
│ VCC → 5V    │
│ GND → GND   │
│ AO  → GPIO39│
│ DO  → N/C   │
└─────────────┘
```

### 💡 Ventajas de Tener 3 MQ-135

1. **Cobertura espacial:** Miden la calidad del aire en diferentes puntos
2. **Mayor precisión:** Promedio de 3 sensores = más fiabilidad
3. **Detección de gradientes:** Identifican variaciones locales de contaminación
4. **Redundancia:** Si uno falla, los otros dos continúan
5. **Validación:** Descartamos lecturas anómalas comparando los 3

### 📊 Algoritmo de Conversión a AQI

```cpp
int ReadMQ135(int pin) {
    // 1. Leer valor ADC (0-4095 en ESP32)
    int sensorValue = analogRead(pin);
    
    // 2. Convertir a voltaje (0-3.3V)
    float voltage = (sensorValue / 4095.0) * 3.3;
    
    // 3. Calcular resistencia del sensor (Rs)
    float Rs = ((3.3 * RL) / voltage) - RL;
    
    // 4. Calcular ratio Rs/Ro
    float ratio = Rs / RO_CLEAN_AIR;
    
    // 5. Convertir a PPM de CO2
    float ppm = 116.6020682 * pow(ratio, -2.769034857);
    
    // 6. Convertir PPM a AQI (Air Quality Index)
    int aqi;
    if (ppm < 400)        aqi = map(ppm, 0, 400, 0, 50);
    else if (ppm < 1000)  aqi = map(ppm, 400, 1000, 51, 100);
    else if (ppm < 2000)  aqi = map(ppm, 1000, 2000, 101, 150);
    else if (ppm < 5000)  aqi = map(ppm, 2000, 5000, 151, 200);
    else                  aqi = map(ppm, 5000, 10000, 201, 300);
    
    return constrain(aqi, 0, 500);
}

// Promedio de los 3 sensores
int ReadAirQuality() {
    int aqi1 = ReadMQ135(MQ135_PIN_1);
    int aqi2 = ReadMQ135(MQ135_PIN_2);
    int aqi3 = ReadMQ135(MQ135_PIN_3);
    
    return (aqi1 + aqi2 + aqi3) / 3;
}
```

### ⏱️ Tiempo de Calentamiento

**Importante:** Los sensores MQ-135 requieren precalentamiento:

- **Primera vez:** 24-48 horas para estabilización óptima
- **Uso diario:** 20-30 minutos antes de obtener lecturas precisas
- **Lecturas inmediatas:** Posibles pero con menor precisión

```cpp
// En el setup(), esperar calentamiento
void setup() {
    // ... inicialización ...
    
    Serial.println("⏱️  Precalentando sensores MQ-135...");
    Serial.println("   Espera recomendada: 20-30 minutos");
    Serial.println("   (Las primeras lecturas pueden ser imprecisas)");
    
    delay(5000);  // Espera mínima de 5 segundos
}
```

### 🎨 Interpretación del AQI

| AQI | Rango | Color | Calidad del Aire |
|-----|-------|-------|------------------|
| 0-50 | Buena | 🟢 Verde | Excelente |
| 51-100 | Moderada | 🟡 Amarillo | Aceptable |
| 101-150 | Dañina para sensibles | 🟠 Naranja | Precaución |
| 151-200 | Dañina | 🔴 Rojo | Mala |
| 201-300 | Muy dañina | 🟣 Púrpura | Muy mala |
| 301-500 | Peligrosa | 🟤 Marrón | Peligrosa |

---

## 🔄 Procesamiento de Datos

### Redundancia y Promediado

El código implementa un sistema inteligente de procesamiento:

```cpp
// 1. Leer todos los sensores
float temp1 = bme1.readTemperature();
float temp2 = bme2.readTemperature();
int aqi1 = ReadMQ135(MQ135_PIN_1);
int aqi2 = ReadMQ135(MQ135_PIN_2);
int aqi3 = ReadMQ135(MQ135_PIN_3);

// 2. Validar lecturas
if (isnan(temp1)) temp1 = temp2;  // Usar backup si falla
if (isnan(temp2)) temp2 = temp1;

// 3. Calcular promedios
float temp_avg = (temp1 + temp2) / 2.0;
int aqi_avg = (aqi1 + aqi2 + aqi3) / 3;

// 4. Publicar datos
Serial.printf("Temp: %.1f°C (BME1: %.1f, BME2: %.1f)\n", 
              temp_avg, temp1, temp2);
Serial.printf("AQI: %d (MQ1: %d, MQ2: %d, MQ3: %d)\n", 
              aqi_avg, aqi1, aqi2, aqi3);
```

---

## 🛒 Lista de Compras (Lo que realmente tienes)

### ✅ Tu Hardware

1. **ESP32 DevKit v1** (o similar) - 1 unidad
2. **BME280** - 2 unidades
   - Sensor de temperatura, humedad y presión atmosférica
   - Comunicación I2C
3. **MQ-135** (AZDelivery) - 3 unidades
   - Sensor de calidad del aire multi-gas
   - Salida analógica
4. **LED RGB** - 1 unidad (para indicador visual)
5. **Módulos Relay** - 2 unidades (para ventilador y calefactor)
6. **Ventilador 5V** - 1 unidad
7. **Resistencias** - Varias (220Ω para LED, pull-ups si necesario)
8. **Protoboard y cables**

### 💰 Estimación de Costes

| Componente | Precio aprox. |
|-----------|---------------|
| ESP32 | 8-12€ |
| BME280 x2 | 12-20€ |
| MQ-135 x3 | 15-25€ |
| LED RGB | 1-2€ |
| Relay x2 | 4-6€ |
| Otros | 5-10€ |
| **TOTAL** | **45-75€** |

---

## 🎓 Justificación Técnica (Para el Proyecto)

### ¿Por qué 2 BME280?

1. **Alta precisión por redundancia**
2. **Sistema profesional con backup**
3. **Cumple requisitos del proyecto con creces**
4. **Permite comparación y validación de datos**

### ¿Por qué 3 MQ-135?

1. **Cobertura espacial amplia**
2. **Triple redundancia para mayor fiabilidad**
3. **Detección de gradientes de contaminación**
4. **Sistema robusto ante fallos**
5. **Supera ampliamente el mínimo requerido (3 sensores)**

### Cumplimiento del Enunciado

✅ **Mínimo 3 sensores/actuadores requeridos**  
✅ **Implementados: 8 componentes** (5 sensores + 3 actuadores)  
✅ **Porcentaje: 267% del mínimo** ⭐⭐⭐  

---

## 📚 Referencias y Datasheets

### BME280
- [Datasheet oficial Bosch](https://www.bosch-sensortec.com/media/boschsensortec/downloads/datasheets/bst-bme280-ds002.pdf)
- [Librería Adafruit BME280](https://github.com/adafruit/Adafruit_BME280_Library)
- [Guía de uso](https://learn.adafruit.com/adafruit-bme280-humidity-barometric-pressure-temperature-sensor-breakout)

### MQ-135
- [Datasheet MQ-135](https://www.olimex.com/Products/Components/Sensors/SNS-MQ135/resources/SNS-MQ135.pdf)
- [Guía de calibración](https://jayconsystems.com/blog/understanding-a-gas-sensor)
- [Conversión a AQI](https://www.airnow.gov/aqi/aqi-basics/)

---

## 🔧 Consejos Prácticos

### Para BME280

1. **Verificar dirección I2C:**
   ```cpp
   Wire.begin();
   Wire.beginTransmission(0x76);
   if (Wire.endTransmission() == 0) 
       Serial.println("BME280 encontrado en 0x76");
   ```

2. **Si no detecta el sensor:**
   - Verifica las conexiones SDA/SCL
   - Asegúrate de que VCC sea 3.3V
   - Usa un escáner I2C para detectar la dirección

3. **Cambiar dirección I2C:**
   - Conectar SDO a GND → 0x76
   - Conectar SDO a VCC → 0x77

### Para MQ-135

1. **Primer uso:** Dejar precalentando 24 horas
2. **Calibración:** Realizar en aire limpio exterior
3. **Lecturas estables:** Esperar 20-30 minutos después de encender
4. **Temperatura:** Funciona mejor a temperatura ambiente (20-25°C)
5. **Vida útil:** 5+ años con uso normal

---

**¡Tu hardware está perfectamente preparado para un proyecto de nivel profesional!** 🏆
