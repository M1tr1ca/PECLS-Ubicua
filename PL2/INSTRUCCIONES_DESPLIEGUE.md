# Sistema de Gestión Urbana - PECL2
## Instrucciones de Despliegue

### Requisitos Previos
- Docker y Docker Compose instalados
- Puerto 3000 libre (MQTT Broker)
- Puerto 3001 libre (PostgreSQL)
- Puerto 8080 libre (Tomcat Server)

### Estructura del Sistema

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   ESP32/PL1     │────▶│   MQTT Broker   │────▶│  Servidor Java  │
│  (Sensores)     │◀────│   (Mosquitto)   │◀────│    (Tomcat)     │
└─────────────────┘     └─────────────────┘     └────────┬────────┘
                                                         │
                                                         ▼
┌─────────────────┐                             ┌─────────────────┐
│  App Android    │────────────────────────────▶│   PostgreSQL    │
│                 │◀────────────────────────────│                 │
└─────────────────┘                             └─────────────────┘
```

### Despliegue con Docker

1. **Navegar a la carpeta Docker:**
   ```bash
   cd PL2/Docker
   ```

2. **Construir e iniciar todos los servicios:**
   ```bash
   docker-compose up --build -d
   ```

3. **Verificar que los servicios están corriendo:**
   ```bash
   docker-compose ps
   ```

4. **Ver logs del servidor:**
   ```bash
   docker-compose logs -f servidor
   ```

### Endpoints Disponibles

#### API REST (Puerto 8080)

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/ServerExampleUbicomp/GetData` | GET | Obtiene las últimas lecturas de sensores |
| `/ServerExampleUbicomp/GetData?sensor_id=XXX` | GET | Lecturas de un sensor específico |
| `/ServerExampleUbicomp/GetData?limit=N` | GET | Limitar número de resultados |
| `/ServerExampleUbicomp/GetSensors` | GET | Lista de sensores disponibles |
| `/ServerExampleUbicomp/GetStreets` | GET | Lista de calles |
| `/ServerExampleUbicomp/GetStreets?district=XXX` | GET | Calles por distrito |
| `/ServerExampleUbicomp/SendAlert` | POST | Enviar alerta a un sensor |

#### Ejemplo de envío de alerta (POST /SendAlert):
```json
{
  "street_id": "ST_1617",
  "alert_level": 2
}
```

#### MQTT Topics

| Topic | Descripción |
|-------|-------------|
| `sensors/{street_id}/weather_station/{sensor_id}` | Publicar datos de sensores |
| `sensors/{street_id}/alerts` | Recibir alertas (suscripción ESP32) |
| `sensors/#` | Suscripción del servidor (wildcard) |

### Formato JSON de los Sensores

```json
{
  "sensor_id": "LAB08JAV-G5",
  "sensor_type": "weather",
  "street_id": "ST_1617",
  "timestamp": "2025-11-24T10:30:00.000",
  "location": {
    "latitude_start": 40.4513367,
    "latitude_end": 40.4515721,
    "longitude_start": -3.6391751,
    "longitude_end": -3.6409307,
    "altitude_meters": 667.3,
    "district": "Hortaleza",
    "neighborhood": "Hortaleza"
  },
  "data": {
    "temperature_celsius": 22.4,
    "humidity_percent": 58.7,
    "atmospheric_pressure_hpa": 1013.2
  }
}
```

### Configuración del ESP32 (PL1)

Para conectar el ESP32 al servidor, actualizar `config.h`:

```cpp
// Configuración MQTT para Docker local
const char* MQTT_BROKER_ADRESS = "IP_DE_TU_PC";  // IP de la máquina con Docker
const uint16_t MQTT_PORT = 3000;  // Puerto mapeado del broker
const char* MQTT_USER = "ubicua";
const char* MQTT_PASS = "ubicua";

// Tópicos
const char* TOPIC_PUBLISH = "sensors/ST_1617/weather_station/LAB08JAV-G5";
const char* TOPIC_SUBSCRIBE = "sensors/ST_1617/alerts";
```

### Niveles de Alerta

| Nivel | Descripción | Comportamiento LED |
|-------|-------------|-------------------|
| 0 | Sin alerta | LED apagado |
| 1 | Alerta baja | Parpadeo lento |
| 2 | Alerta media | Parpadeo medio |
| 3 | Alerta alta | Parpadeo rápido |
| 4 | Alerta crítica | LED continuo |

### Conexión desde Android

**Base URL:** `http://IP_DEL_SERVIDOR:8080/ServerExampleUbicomp`

Ejemplo con Retrofit/OkHttp:
```java
// Obtener datos
GET http://192.168.1.100:8080/ServerExampleUbicomp/GetData?limit=50

// Enviar alerta
POST http://192.168.1.100:8080/ServerExampleUbicomp/SendAlert
Content-Type: application/json
{
  "street_id": "ST_1617",
  "alert_level": 2
}
```

### Comandos Útiles

```bash
# Detener servicios
docker-compose down

# Ver logs de todos los servicios
docker-compose logs -f

# Reconstruir solo el servidor
docker-compose up --build -d servidor

# Acceder a la base de datos
docker exec -it base_datos psql -U admin -d ubicua

# Ver tablas
\dt

# Consultar lecturas
SELECT * FROM sensor_readings ORDER BY timestamp DESC LIMIT 10;
```

### Solución de Problemas

1. **El servidor no conecta a MQTT:**
   - Verificar que el broker está corriendo: `docker-compose logs mqtt-broker`
   - Comprobar configuración de red en docker-compose.yml

2. **Error de conexión a base de datos:**
   - Esperar a que PostgreSQL esté healthy antes de iniciar el servidor
   - Verificar credenciales en context.xml

3. **ESP32 no envía datos:**
   - Verificar IP del broker en config.h
   - Comprobar que el puerto 3000 está accesible
   - Revisar logs del broker: `docker-compose logs mqtt-broker`

