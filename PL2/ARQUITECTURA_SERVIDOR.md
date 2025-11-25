# Arquitectura del Servidor Ubicua - PL2

## Índice
1. [Visión General](#visión-general)
2. [Arquitectura del Sistema](#arquitectura-del-sistema)
3. [Descripción de Clases](#descripción-de-clases)
4. [Flujo de Ejecución Completo](#flujo-de-ejecución-completo)
5. [Diagramas](#diagramas)

---

## Visión General

El servidor Ubicua (PECL2) es una aplicación **Java/Tomcat** que actúa como intermediario entre los sensores ESP32 (a través de MQTT) y una base de datos PostgreSQL. Su objetivo es:

- ✅ **Recibir datos** de sensores vía MQTT
- ✅ **Procesar y validar** los mensajes JSON
- ✅ **Guardar en la base de datos** las lecturas
- ✅ **Detectar alertas** por condiciones anormales
- ✅ **Exponer APIs REST** para consultar datos

---

## Arquitectura del Sistema

```
┌──────────────────────────────────────────────────────────────────────────┐
│                        SERVIDOR UBICUA (TOMCAT)                         │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────┐    │
│  │                    CAPA DE PRESENTACIÓN                        │    │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐           │    │
│  │  │  GetData     │ │GetSensors    │ │GetStreets    │ SendAlert │    │
│  │  │  (REST API)  │ │(REST API)    │ │ (REST API)   │(REST API) │    │
│  │  └──────────────┘ └──────────────┘ └──────────────┘           │    │
│  └────────────────────────────────────────────────────────────────┘    │
│                                   ▲                                     │
│                                   │ HTTP                                │
│  ┌────────────────────────────────────────────────────────────────┐    │
│  │                    CAPA DE LÓGICA DE NEGOCIO                   │    │
│  │  ┌──────────────────┐ ┌──────────────────┐                     │    │
│  │  │   SensorLogic    │ │  Projectinit...  │                     │    │
│  │  │ (Guardar datos)  │ │ (Inicializar)    │                     │    │
│  │  └──────────────────┘ └──────────────────┘                     │    │
│  └────────────────────────────────────────────────────────────────┘    │
│                                   ▲                                     │
│                                   │                                     │
│  ┌────────────────────────────────────────────────────────────────┐    │
│  │                    CAPA DE MQTT                                │    │
│  │  ┌───────────────────┐ ┌──────────────────┐                   │    │
│  │  │  MQTTSuscriber    │ │  MQTTPublisher   │                   │    │
│  │  │ (Recibe mensajes) │ │ (Envía alertas)  │                   │    │
│  │  └───────────────────┘ └──────────────────┘                   │    │
│  │           ▲                                    ▼                │    │
│  └───────────┼────────────────────────────────────┼───────────────┘    │
│              │                                    │                     │
│              │ MQTT (sensors/#)                   │ MQTT (sensors/.../  │
│              │ (puerto 1883 interno)              │  alerts)            │
│              │                                    │                     │
└──────────────┼────────────────────────────────────┼──────────────────────┘
               │                                    │
               ▼                                    │
          ┌─────────────┐                          │
          │ MQTT Broker │◄─────────────────────────┘
          │ (Mosquitto) │
          │ Puerto 3000 │
          └─────────────┘
               ▲
               │ MQTT
               │
           ┌───────┐
           │ ESP32 │
           │Sensor │
           └───────┘

┌──────────────────────────────────────────────────────────────┐
│              BASE DE DATOS (PostgreSQL)                      │
│  ┌────────────────┐ ┌────────────┐ ┌──────────────────┐    │
│  │ sensor_readings│ │ sensors    │ │ streets          │    │
│  │                │ │            │ │                  │    │
│  │ - sensor_id   │ │ - sensor_id│ │ - street_id      │    │
│  │ - timestamp    │ │ - type     │ │ - street_name    │    │
│  │ - temperature  │ │ - street_id│ │ - district       │    │
│  │ - humidity     │ │            │ │ - neighborhood   │    │
│  │ - pressure     │ │            │ │ - coordinates    │    │
│  │ - altitude     │ │            │ │                  │    │
│  └────────────────┘ └────────────┘ └──────────────────┘    │
│       Puerto 5432 interno (3001 externo)                   │
└──────────────────────────────────────────────────────────────┘
```

---

## Descripción de Clases

### 1. **Projectinitializer** (Inicialización del Sistema)
**Ubicación:** `Logic/Projectinitializer.java`

**Responsabilidad:** Inicializar el sistema MQTT al arrancar Tomcat

**Métodos clave:**
- `contextInitialized()` - Se ejecuta al arrancar el servidor
  - Crea una instancia del broker MQTT
  - Crea un suscriptor MQTT
  - Se suscribe al topic `sensors/#` (recibe todos los mensajes de sensores)
  - Publica un mensaje de estado `server/status`

**Flujo de ejecución:**
```
Tomcat Inicia
    ↓
contextInitialized() se ejecuta
    ↓
Crear MQTTBroker (obtiene configuración de variables de entorno)
    ↓
Crear MQTTSuscriber
    ↓
suscribeTopic(broker, "sensors/#")
    ↓
Se carga el callback de MQTTSuscriber
    ↓
Servidor listo para recibir mensajes MQTT
```

---

### 2. **MQTTBroker** (Configuración del Broker)
**Ubicación:** `Mqtt/MQTTBroker.java`

**Responsabilidad:** Gestionar la configuración del broker MQTT

**Atributos estáticos:**
```java
broker = "tcp://mqtt-broker:1883"          // URL del broker MQTT
clientId = "ServerUbicuaUAH"               // ID único del cliente
username = "ubicua"                        // Usuario MQTT
password = "ubicua"                        // Contraseña MQTT
qos = 2                                    // Quality of Service (garantía de entrega)
```

**Métodos:**
- `getBroker()` - Retorna la URL del broker (lee env var MQTT_BROKER)
- `getUsername()` - Retorna usuario (lee env var MQTT_USERNAME)
- `getPassword()` - Retorna contraseña (lee env var MQTT_PASSWORD)

---

### 3. **MQTTSuscriber** (Recepción de Mensajes)
**Ubicación:** `Mqtt/MQTTSuscriber.java`

**Responsabilidad:** Escuchar y procesar mensajes MQTT de los sensores

**Métodos clave:**

#### `suscribeTopic(MQTTBroker broker, String topic)`
- Crea un cliente MQTT
- Configura opciones de conexión (usuario, contraseña, reconexión automática)
- Se conecta al broker
- Se suscribe al topic `sensors/#`

#### `messageArrived(String topic, MqttMessage message)`
- **Se ejecuta automáticamente** cuando llega un mensaje
- Extrae el payload (contenido del mensaje)
- Llama a `parseMessage()` para convertir JSON → objeto `SensorReading`
- Llama a `SensorLogic.saveSensorReading()` para guardar en BD
- Llama a `checkAndSendAlerts()` para detectar alertas

#### `parseMessage(String json)`
- Parsea el JSON usando GSON
- Extrae campos obligatorios: `sensor_id`, `street_id`
- Extrae datos anidados en `data`: temperatura, humedad, presión, altitud
- Extrae ubicación en `location`
- Valida que `sensor_id` y `street_id` no sean nulos
- Parsea el timestamp en varios formatos
- Retorna objeto `SensorReading`

#### `checkAndSendAlerts(SensorReading reading)`
- Verifica si temperatura > 28°C (alerta)
- Verifica si humedad > 90% (alerta)
- Si hay alerta, publica en topic `sensors/{street_id}/alerts`

---

### 4. **SensorReading** (Modelo de Datos)
**Ubicación:** `Logic/SensorReading.java`

**Responsabilidad:** Representar una lectura de sensor

**Atributos:**
```java
String sensorId;              // ID único del sensor (ej: LAB08JAV-G1)
String sensorType;            // Tipo de sensor (ej: "weather", "generic")
String streetId;              // ID de la calle (ej: ST_0686)
Timestamp timestamp;          // Fecha y hora de la lectura
Location location;            // Datos de ubicación (lat/lon, altitud)
SensorData data;              // Valores medidos (temp, humedad, presión)
String streetName;            // Nombre de la calle
String district;              // Distrito
String neighborhood;          // Barrio
```

---

### 5. **SensorData** (Datos del Sensor)
**Ubicación:** `Logic/SensorData.java`

**Responsabilidad:** Encapsular los valores medidos por el sensor

**Atributos:**
```java
Double temperatureCelsius;         // Temperatura en °C
Double humidityPercent;            // Humedad en %
Double atmosphericPressureHpa;     // Presión en hPa
Double altitudeMeters;             // Altitud en metros
```

---

### 6. **Location** (Datos de Ubicación)
**Ubicación:** `Logic/Location.java`

**Responsabilidad:** Encapsular datos geográficos

**Atributos:**
```java
Double latitude;              // Latitud
Double longitude;             // Longitud
Double latitudeStart;         // Lat inicial de la calle
Double latitudeEnd;           // Lat final de la calle
Double longitudeStart;        // Lon inicial de la calle
Double longitudeEnd;          // Lon final de la calle
Double altitudeMeters;        // Altitud
String district;              // Distrito
String neighborhood;          // Barrio
```

---

### 7. **SensorLogic** (Lógica de Base de Datos)
**Ubicación:** `Logic/SensorLogic.java`

**Responsabilidad:** Operaciones de lectura/escritura en la base de datos

**Métodos clave:**

#### `saveSensorReading(SensorReading reading)` ⭐ CRÍTICO
- Abre conexión a BD
- Verifica si el sensor existe en tabla `sensors`
  - Si NO existe → crea el sensor con `createSensor()`
  - Si NO existe la calle → crea la calle con `createStreet()`
- Ejecuta INSERT en tabla `sensor_readings`:
  ```sql
  INSERT INTO sensor_readings 
  (sensor_id, timestamp, temperature_celsius, humidity_percent, 
   atmospheric_pressure_hpa, altitude_meters)
  VALUES (?, ?, ?, ?, ?, ?)
  ```
- Cierra conexión
- Retorna `true` si se guardó, `false` si hubo error

#### `getLatestReadings(int limit)`
- JOIN entre 3 tablas:
  - `sensor_readings` (las mediciones)
  - `sensors` (información del sensor)
  - `streets` (información de la calle)
- Retorna las últimas `limit` lecturas ordenadas por timestamp DESC
- Cada registro incluye datos completos: sensor, calle, ubicación, mediciones

#### `getLatestReadingsBySensor(String sensorId, int limit)`
- Similar a anterior pero filtrando por un sensor específico

#### Helper Methods:
- `sensorExists()` - Verifica si existe un sensor
- `createSensor()` - Crea sensor nuevo
- `streetExists()` - Verifica si existe una calle
- `createStreet()` - Crea calle nueva
- `mapResultSetToSensorReading()` - Convierte resultado SQL → SensorReading

---

### 8. **ConectionDDBB** (Gestión de Conexiones)
**Ubicación:** `Database/ConectionDDBB.java`

**Responsabilidad:** Gestionar conexiones a PostgreSQL

**Métodos clave:**

#### `obtainConnection(boolean autoCommit)`
- Busca la fuente de datos JNDI configurada en Tomcat
- Intenta conectar hasta 5 veces
- Retorna una `Connection` a PostgreSQL
- Establece modo autoCommit

#### `closeConnection(Connection con)`
- Cierra la conexión de forma segura
- Registra en logs

#### `closeTransaction()` / `cancelTransaction()`
- Commit o rollback de transacciones

**Configuración JNDI:**
```xml
<!-- En WEB-INF/context.xml -->
<Resource name="jdbc/ubicomp" 
          type="javax.sql.DataSource"
          driverClassName="org.postgresql.Driver"
          url="jdbc:postgresql://base_datos:5432/ubicua"
          username="admin"
          password="admin"
          maxActive="20"/>
```

---

### 9. **Servlets REST (APIs)**

#### **GetData** (`Servlets/GetData.java`)
- **URL:** `GET /ServerExampleUbicomp/GetData`
- **Parámetros:**
  - `sensor_id` (opcional) - Filtrar por sensor
  - `limit` (opcional, default 100) - Número de lecturas
- **Respuesta:** JSON array de lecturas
- **Lógica:**
  1. Si hay `sensor_id` → llama `SensorLogic.getLatestReadingsBySensor()`
  2. Si no → llama `SensorLogic.getLatestReadings()`
  3. Convierte a JSON con GSON
  4. Retorna en response

#### **GetSensors** (`Servlets/GetSensors.java`)
- **URL:** `GET /ServerExampleUbicomp/GetSensors`
- **Respuesta:** Lista JSON de todos los sensores

#### **GetStreets** (`Servlets/GetStreets.java`)
- **URL:** `GET /ServerExampleUbicomp/GetStreets`
- **Respuesta:** Lista JSON de todas las calles

#### **SendAlert** (`Servlets/SendAlert.java`)
- **URL:** `POST /ServerExampleUbicomp/SendAlert`
- **Parámetros:** alert data en JSON
- **Acción:** Publica alerta en MQTT topic `sensors/{street_id}/alerts`

---

## Flujo de Ejecución Completo

### **FASE 1: INICIO DEL SERVIDOR**

```
1. Docker inicia contenedor Tomcat
   ↓
2. Tomcat carga la aplicación WAR
   ↓
3. Tomcat ejecuta @WebListener Projectinitializer.contextInitialized()
   ↓
4. Se crea MQTTBroker (lee variables de entorno)
   MQTT_BROKER="tcp://mqtt-broker:1883"
   MQTT_USERNAME="ubicua"
   MQTT_PASSWORD="ubicua"
   ↓
5. Se crea MQTTSuscriber
   ↓
6. MQTTSuscriber.suscribeTopic(broker, "sensors/#")
   ↓
7. Conectar a mqtt-broker:1883
   ↓
8. Suscribirse a "sensors/#"
   ↓
9. Registrar callback MQTTSuscriber para recibir mensajes
   ↓
10. Publicar mensaje de estado en "server/status"
    ↓
11. ✅ SERVIDOR LISTO - Escuchando en mqtt-broker
```

**Logs esperados:**
```
===========================================
  SERVIDOR UBICUA - PECL2 INICIADO
===========================================
-->Suscribiéndose a tópicos MQTT<--
Suscrito a sensors/#
===========================================
  SISTEMA LISTO PARA RECIBIR DATOS
===========================================
```

---

### **FASE 2: ESP32 PUBLICA UN MENSAJE**

```
1. ESP32 publica en topic "sensors/LAB08JAV-G1/data"
   
   Mensaje JSON:
   {
     "sensor_id": "LAB08JAV-G1",
     "street_id": "ST_0686",
     "sensor_type": "generic",
     "timestamp": "2025-11-25T09:44:42.868",
     "location": {
       "latitude_start": 40.3971536,
       "latitude_end": 40.3977451,
       "longitude_start": -3.6734246,
       "longitude_end": -3.6731276,
       "altitude_meters": 650.0,
       "district": "Arganzuela",
       "neighborhood": "Imperial"
     },
     "data": {
       "temperature_celsius": 25.5,
       "humidity_percent": 60.0,
       "atmospheric_pressure_hpa": 1013.25,
       "altitude_meters": 650.0
     }
   }
   ↓
2. MQTT Broker recibe el mensaje
   ↓
3. MQTT Broker distribuye a todos los suscriptores de "sensors/#"
   ↓
4. ✅ MQTTSuscriber recibe el mensaje
```

---

### **FASE 3: PROCESAMIENTO DEL MENSAJE**

```
1. MQTTSuscriber.messageArrived(topic, message) se ejecuta
   
   topic = "sensors/LAB08JAV-G1/data"
   message.toString() = JSON anterior
   ↓
2. Log: "Mensaje recibido en sensors/LAB08JAV-G1/data: {...}"
   ↓
3. parseMessage(json)
   ├─ JsonParser.parseString(json)
   ├─ Extrae: sensor_id = "LAB08JAV-G1" ✓
   ├─ Extrae: street_id = "ST_0686" ✓
   ├─ Extrae: sensor_type = "generic"
   ├─ Extrae: timestamp = parsea a java.sql.Timestamp
   ├─ Extrae: location = crea objeto Location
   ├─ Extrae: data = crea objeto SensorData
   ├─ Valida que sensor_id y street_id NO sean null
   └─ Retorna objeto SensorReading completo
   ↓
4. if (reading != null)
   ↓
5. SensorLogic.saveSensorReading(reading) ⭐ PUNTO CRÍTICO
```

---

### **FASE 4: GUARDADO EN BASE DE DATOS**

```
SensorLogic.saveSensorReading(SensorReading reading)
│
├─ 1. ConectionDDBB.obtainConnection(true)
│     └─ JNDI lookup("java:/comp/env/jdbc/ubicomp")
│     └─ Obtiene DataSource → Connection a PostgreSQL
│     └─ setAutoCommit(true)
│
├─ 2. sensorExists(connection, "LAB08JAV-G1")?
│     └─ SELECT 1 FROM sensors WHERE sensor_id = ?
│     └─ Si NO existe:
│         ├─ streetExists(connection, "ST_0686")?
│         │  └─ SELECT 1 FROM streets WHERE street_id = ?
│         │  └─ Si NO existe:
│         │      └─ createStreet(connection, reading)
│         │         └─ INSERT INTO streets (street_id, street_name, 
│         │            district, neighborhood, lat_start, lat_end, 
│         │            lon_start, lon_end)
│         │         └─ Log: "Calle creada: ST_0686"
│         │
│         └─ createSensor(connection, reading)
│            └─ INSERT INTO sensors (sensor_id, sensor_type, street_id)
│            └─ Log: "Sensor creado: LAB08JAV-G1"
│
├─ 3. INSERT INTO sensor_readings
│     (sensor_id, timestamp, temperature_celsius, humidity_percent,
│      atmospheric_pressure_hpa, altitude_meters)
│     VALUES
│     ("LAB08JAV-G1", 2025-11-25 09:44:42.868, 25.5, 60.0, 1013.25, 650.0)
│
├─ 4. executeUpdate() retorna filas insertadas
│
├─ 5. if (rows > 0) success = true
│
├─ 6. ConectionDDBB.closeConnection(connection)
│     └─ connection.close()
│
└─ return true ✅
```

**En la Base de Datos:**
```sql
-- Tabla streets (si se creó)
INSERT INTO streets VALUES
('ST_0686', 'Calle de Luis Peidró', 'Arganzuela', 'Imperial', 
 40.3971536, 40.3977451, -3.6734246, -3.6731276);

-- Tabla sensors (si se creó)
INSERT INTO sensors VALUES
('LAB08JAV-G1', 'generic', 'ST_0686');

-- Tabla sensor_readings ✅ SIEMPRE se inserta
INSERT INTO sensor_readings VALUES
('LAB08JAV-G1', '2025-11-25 09:44:42.868', 25.5, 60.0, 1013.25, 650.0);
```

**Logs esperados:**
```
Base de datos conectada para guardar lectura
Lectura guardada para sensor: LAB08JAV-G1 - Éxito: true
Lectura guardada exitosamente para sensor: LAB08JAV-G1
```

---

### **FASE 5: DETECCIÓN DE ALERTAS**

```
SensorLogic.saveSensorReading() retorna true
   ↓
MQTTSuscriber.messageArrived() continúa
   ↓
checkAndSendAlerts(reading)
│
├─ if (reading.getData() != null)
│
├─ if (temperature >= 40) alertLevel = 4 ⚠️ CRÍTICA
├─ if (temperature >= 35) alertLevel = 3 ⚠️ ALTA
├─ if (temperature >= 30) alertLevel = 2 ⚠️ MEDIA
├─ if (temperature >= 28) alertLevel = 1 ⚠️ BAJA
│
├─ if (humidity >= 90) alertLevel = max(alertLevel, 2)
│
└─ if (alertLevel > 0)
   └─ MQTTPublisher.publish(broker, 
        "sensors/ST_0686/alerts",
        "{\"alert_level\":2}")
      └─ Publica alerta en el broker MQTT
```

**Ejemplo:**
```
En nuestro caso: temperatura = 25.5°C, humedad = 60%
└─ alertLevel = 0
└─ NO se publica alerta ✓
```

---

### **FASE 6: CONSULTA DE DATOS VÍA API REST**

```
Usuario/Navegador hace petición HTTP:
GET http://localhost:8080/ServerExampleUbicomp/GetData
   ↓
Tomcat enruta a GetData.doGet()
   ↓
GetData.doGet(request, response)
│
├─ Extrae parámetros:
│  ├─ sensorId = null (no especificado)
│  └─ limit = 100 (default)
│
├─ SensorLogic.getLatestReadings(100)
│
├─ Ejecuta SQL con JOIN:
│  └─ SELECT sr.*, s.sensor_type, s.street_id, 
│           st.street_name, st.district, st.neighborhood,
│           st.lat_start, st.lat_end, st.lon_start, st.lon_end
│     FROM sensor_readings sr
│     JOIN sensors s ON sr.sensor_id = s.sensor_id
│     JOIN streets st ON s.street_id = st.street_id
│     ORDER BY sr.timestamp DESC LIMIT 100
│
├─ Itera ResultSet → mapResultSetToSensorReading()
│  └─ Crea lista de SensorReading con datos completos
│
├─ Gson.toJson(readings)
│  └─ Convierte a JSON array
│
└─ response.println(jsonReadings)
   └─ Retorna JSON al cliente
```

**Respuesta JSON:**
```json
[
  {
    "sensorId": "LAB08JAV-G1",
    "sensorType": "generic",
    "streetId": "ST_0686",
    "timestamp": "Nov 25, 2025, 9:44:42 AM",
    "location": {
      "latitudeStart": 40.3971536,
      "latitudeEnd": 40.3977451,
      "longitudeStart": -3.6734246,
      "longitudeEnd": -3.6731276,
      "altitudeMeters": 650.0,
      "district": "Arganzuela",
      "neighborhood": "Imperial"
    },
    "data": {
      "temperatureCelsius": 25.5,
      "humidityPercent": 60.0,
      "atmosphericPressureHpa": 1013.25
    },
    "streetName": "Calle de Luis Peidró",
    "district": "Arganzuela",
    "neighborhood": "Imperial"
  }
]
```

---

## Diagramas

### Diagrama de Secuencia Completo

```
ESP32              MQTT Broker         Servidor Tomcat           PostgreSQL
  │                    │                   │                         │
  │──── PUBLISH ──────→ │                   │                         │
  │ sensors/LAB.../data│                   │                         │
  │                    │                   │                         │
  │                    │── FORWARD ───────→│                         │
  │                    │ messageArrived()  │                         │
  │                    │                   │                         │
  │                    │                   ├─ parseMessage()         │
  │                    │                   │                         │
  │                    │                   ├─ saveSensorReading()    │
  │                    │                   │                         │
  │                    │                   ├─ obtainConnection() ───→│
  │                    │                   │                         │
  │                    │                   ├─ sensorExists() ───────→│
  │                    │                   │ ← SELECT response       │
  │                    │                   │                         │
  │                    │                   ├─ INSERT sensor_readings→│
  │                    │                   │                         │
  │                    │                   ├─ closeConnection() ────→│
  │                    │                   │ ← Connection closed     │
  │                    │                   │                         │
  │                    │                   ├─ checkAndSendAlerts()  │
  │                    │                   │                         │
  └────────────────────┴───────────────────┴─────────────────────────┘

Cliente HTTP         Servidor Tomcat         PostgreSQL
  │                     │                        │
  │─ GET /GetData ─────→│                        │
  │                     │                        │
  │                     ├─ obtainConnection() ──→│
  │                     │ ← Connection           │
  │                     │                        │
  │                     ├─ getLatestReadings() ──→
  │                     │                        │
  │                     ├─ Ejecuta SQL JOIN ────→
  │                     │ ← ResultSet            │
  │                     │                        │
  │                     ├─ mapResultSet() ─────→│
  │                     │                        │
  │                     ├─ Gson.toJson() ─────→ (en memoria)
  │                     │                        │
  │                     ├─ closeConnection() ───→
  │                     │ ← Connection closed    │
  │                     │                        │
  │ ← JSON Response ────│
  │                     │
```

### Diagrama de Clases

```
┌─────────────────────────────────────────────────────────────┐
│                      PROYECT INITIALIZER                    │
│                                                             │
│  - suscriber: MQTTSuscriber                                │
│  + contextInitialized(): void                              │
│  + contextDestroyed(): void                                │
└────────────────────────────┬────────────────────────────────┘
                             │ creates
                             ↓
┌──────────────────────────────────────────────────────────────────┐
│                       MQTT SUSCRIBER                             │
│                                                                  │
│  - client: MqttClient                                           │
│  + suscribeTopic(broker, topic): void                           │
│  + messageArrived(topic, message): void  ⭐ CALLBACK            │
│  - parseMessage(json): SensorReading                            │
│  - checkAndSendAlerts(reading): void                            │
└────────┬──────────────┬──────────────────┬─────────────────────┘
         │              │                  │
      uses           creates           uses
         ↓              ↓                  ↓
    ┌──────────┐  ┌──────────────┐  ┌──────────────────┐
    │MQTT      │  │SensorReading │  │SensorLogic       │
    │Broker    │  │              │  │                  │
    │          │  │- sensorId    │  │+ saveSensorRead()│
    │- broker  │  │- sensorType  │  │+ getLatestRead..│
    │- username│  │- streetId    │  │                  │
    │- password│  │- timestamp   │  │uses              │
    └──────────┘  │- location    │  └────────┬─────────┘
                  │- data        │           │
                  │              │        uses
                  └─────┬────────┘           │
                        │                   ↓
                        ├─→ ┌──────────────────────────┐
                        │   │Connection DDBB           │
                        │   │                          │
                        │   │+ obtainConnection()     │
                        │   │+ closeConnection()      │
                        │   └────────┬────────────────┘
                        │            │
                        │         uses
                        │            ↓
                        │    ┌───────────────────┐
                        │    │PostgreSQL         │
                        │    │Database           │
                        │    │                   │
                        └───→│- sensor_readings │
                             │- sensors         │
                             │- streets         │
                             └───────────────────┘
        
        ├─→ ┌────────────────┐
        │   │SensorData      │
        │   │                │
        │   │- temperature   │
        │   │- humidity      │
        │   │- pressure      │
        │   └────────────────┘
        
        ├─→ ┌────────────────┐
            │Location        │
            │                │
            │- latitude      │
            │- longitude     │
            │- altitude      │
            │- district      │
            └────────────────┘


REST SERVLETS
┌─────────────────┬──────────────────┬──────────────────┬─────────────────┐
│   GetData       │  GetSensors      │  GetStreets      │  SendAlert      │
│                 │                  │                  │                 │
│ + doGet()       │ + doGet()        │ + doGet()        │ + doPost()      │
│                 │                  │                  │                 │
│ calls:          │ calls:           │ calls:           │ calls:          │
│ SensorLogic.    │ SensorLogic.     │ SensorLogic.     │ MQTTPublisher.  │
│ getLatestRead...│ getAllSensors()  │ getAllStreets()  │ publish()       │
└─────────────────┴──────────────────┴──────────────────┴─────────────────┘
        ↓                  ↓                  ↓                  ↓
        └──────────────────┴──────────────────┴──────────────────┘
                           ↓
                    ┌──────────────────┐
                    │MQTT Publisher    │
                    │                  │
                    │+ publish()       │
                    └──────────────────┘
                           ↓
                    ┌──────────────────┐
                    │MQTT Broker       │
                    │(Mosquitto)       │
                    └──────────────────┘
```

---

## Resumen: Flujo Completo en 10 Pasos

```
1️⃣  ESP32 publica JSON en "sensors/LAB08JAV-G1/data"
                        ↓
2️⃣  MQTT Broker recibe y distribuye a suscriptores
                        ↓
3️⃣  MQTTSuscriber.messageArrived() se dispara
                        ↓
4️⃣  parseMessage() convierte JSON → SensorReading
                        ↓
5️⃣  SensorLogic.saveSensorReading() se ejecuta
                        ↓
6️⃣  Conecta a PostgreSQL (ConectionDDBB)
                        ↓
7️⃣  Verifica y crea (si necesario) sensor y calle
                        ↓
8️⃣  INSERT en tabla sensor_readings ✅
                        ↓
9️⃣  checkAndSendAlerts() detecta condiciones anormales
                        ↓
🔟 Cliente consulta GET /GetData → APIs devuelven datos guardados
```

---

## Configuración Necesaria

### `docker-compose.yml`
```yaml
servidor:
  environment:
    MQTT_BROKER: "tcp://mqtt-broker:1883"
    MQTT_USERNAME: "ubicua"
    MQTT_PASSWORD: "ubicua"
```

### `WEB-INF/context.xml`
```xml
<Resource name="jdbc/ubicomp" 
          type="javax.sql.DataSource"
          driverClassName="org.postgresql.Driver"
          url="jdbc:postgresql://base_datos:5432/ubicua"
          username="admin"
          password="admin"
          maxActive="20"/>
```

### `WEB-INF/web.xml`
```xml
<resource-ref>
  <description>PostgreSQL Datasource</description>
  <res-ref-name>jdbc/ubicomp</res-ref-name>
  <res-type>javax.sql.DataSource</res-type>
  <res-auth>Container</res-auth>
</resource-ref>
```

---

## Puntos Clave 🎯

| Concepto | Detalles |
|----------|----------|
| **Iniciación** | Projectinitializer.contextInitialized() → MQTTSuscriber.suscribeTopic() |
| **Recepción** | MQTTSuscriber.messageArrived() se dispara automáticamente |
| **Parsing** | parseMessage() convierte JSON → SensorReading (incluye validación) |
| **Guardado** | SensorLogic.saveSensorReading() → INSERT en sensor_readings |
| **Consulta** | GetData servlet → SensorLogic.getLatestReadings() → JSON |
| **Alertas** | checkAndSendAlerts() detecta condiciones y publica en MQTT |
| **BD** | 3 tablas: sensor_readings (datos), sensors (info), streets (ubicación) |
| **Concurrencia** | Cada mensaje se procesa en thread separado del MQTT callback |

---

## Errores Comunes y Soluciones

| Error | Causa | Solución |
|-------|-------|----------|
| "Conexión MQTT perdida" | Broker no disponible | Verificar que MQTT está en línea: `docker ps` |
| "Mensaje sin sensor_id" | JSON incompleto | Incluir `sensor_id` en el JSON |
| "Mensaje sin street_id" | JSON incompleto | Incluir `street_id` en el JSON |
| "Base de datos conectada... 0 intentos sin exito" | Conexión BD falla | Verificar PostgreSQL está corriendo |
| No aparecen datos en BD | INSERT falla silenciosamente | Revisar logs: `docker logs servidor-ubicua` |
| API retorna array vacío | No hay datos guardados | Verificar que se publicó en MQTT correctamente |

---

**Última actualización:** 25 de Noviembre de 2025
**Versión:** 1.0
