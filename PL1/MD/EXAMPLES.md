# 🎯 Ejemplos de Uso y Comandos MQTT

## 📋 Índice
- [Comandos MQTT Básicos](#comandos-mqtt-básicos)
- [Ejemplos de Control](#ejemplos-de-control)
- [Scripts de Prueba](#scripts-de-prueba)
- [Integración con Otros Sistemas](#integración-con-otros-sistemas)
- [Casos de Uso](#casos-de-uso)

---

## 📡 Comandos MQTT Básicos

### Suscribirse a los Datos de la Estación

**Windows (PowerShell):**
```powershell
mosquitto_sub -h test.mosquitto.org -t "uah/alcala/weather/data" -v
```

**Linux/macOS:**
```bash
mosquitto_sub -h test.mosquitto.org -t "uah/alcala/weather/data" -v
```

### Publicar un Comando de Control

**Windows (PowerShell):**
```powershell
mosquitto_pub -h test.mosquitto.org -t "uah/alcala/weather/control" -m '{\"command\":\"read_now\"}'
```

**Linux/macOS:**
```bash
mosquitto_pub -h test.mosquitto.org -t "uah/alcala/weather/control" -m '{"command":"read_now"}'
```

---

## 🎮 Ejemplos de Control

### 1. Control del Ventilador

**Activar ventilador:**
```bash
mosquitto_pub -h test.mosquitto.org \
  -t "uah/alcala/weather/control" \
  -m '{"command":"fan_on","value":true}'
```

**Desactivar ventilador:**
```bash
mosquitto_pub -h test.mosquitto.org \
  -t "uah/alcala/weather/control" \
  -m '{"command":"fan_on","value":false}'
```

### 2. Control del Calefactor

**Activar calefactor:**
```bash
mosquitto_pub -h test.mosquitto.org \
  -t "uah/alcala/weather/control" \
  -m '{"command":"heater_on","value":true}'
```

**Desactivar calefactor:**
```bash
mosquitto_pub -h test.mosquitto.org \
  -t "uah/alcala/weather/control" \
  -m '{"command":"heater_on","value":false}'
```

### 3. Control del LED RGB

**Rojo (Alarma):**
```bash
mosquitto_pub -h test.mosquitto.org \
  -t "uah/alcala/weather/control" \
  -m '{"command":"led_rgb","r":255,"g":0,"b":0}'
```

**Verde (OK):**
```bash
mosquitto_pub -h test.mosquitto.org \
  -t "uah/alcala/weather/control" \
  -m '{"command":"led_rgb","r":0,"g":255,"b":0}'
```

**Azul (Info):**
```bash
mosquitto_pub -h test.mosquitto.org \
  -t "uah/alcala/weather/control" \
  -m '{"command":"led_rgb","r":0,"g":0,"b":255}'
```

**Amarillo (Advertencia):**
```bash
mosquitto_pub -h test.mosquitto.org \
  -t "uah/alcala/weather/control" \
  -m '{"command":"led_rgb","r":255,"g":255,"b":0}'
```

**Morado:**
```bash
mosquitto_pub -h test.mosquitto.org \
  -t "uah/alcala/weather/control" \
  -m '{"command":"led_rgb","r":128,"g":0,"b":128}'
```

**Cian:**
```bash
mosquitto_pub -h test.mosquitto.org \
  -t "uah/alcala/weather/control" \
  -m '{"command":"led_rgb","r":0,"g":255,"b":255}'
```

**Apagado:**
```bash
mosquitto_pub -h test.mosquitto.org \
  -t "uah/alcala/weather/control" \
  -m '{"command":"led_rgb","r":0,"g":0,"b":0}'
```

### 4. Comandos del Sistema

**Forzar lectura inmediata:**
```bash
mosquitto_pub -h test.mosquitto.org \
  -t "uah/alcala/weather/control" \
  -m '{"command":"read_now"}'
```

**Reiniciar ESP32:**
```bash
mosquitto_pub -h test.mosquitto.org \
  -t "uah/alcala/weather/control" \
  -m '{"command":"reset"}'
```

---

## 🐍 Scripts de Prueba

### Script Python - Monitor de Datos

Guarda como `mqtt_monitor.py`:

```python
#!/usr/bin/env python3
"""
Monitor de Estación Meteorológica
Muestra los datos en tiempo real de forma legible
"""

import paho.mqtt.client as mqtt
import json
from datetime import datetime

BROKER = "test.mosquitto.org"
PORT = 1883
TOPIC = "uah/alcala/weather/data"

def on_connect(client, userdata, flags, rc):
    print(f"✓ Conectado al broker (rc: {rc})")
    client.subscribe(TOPIC)
    print(f"✓ Suscrito a: {TOPIC}\n")
    print("=" * 60)
    print("Esperando datos de la estación meteorológica...")
    print("=" * 60 + "\n")

def on_message(client, userdata, msg):
    try:
        data = json.loads(msg.payload.decode())
        
        print(f"\n{'='*60}")
        print(f"📊 DATOS RECIBIDOS - {datetime.now().strftime('%H:%M:%S')}")
        print(f"{'='*60}")
        
        print(f"\n📍 Estación: {data['sensor_id']}")
        print(f"   Ubicación: {data['location']['district']}, {data['location']['neighborhood']}")
        print(f"   Coordenadas: {data['location']['latitude']}, {data['location']['longitude']}")
        
        weather = data['data']
        print(f"\n🌡️  Temperatura: {weather['temperature_celsius']}°C")
        print(f"💧 Humedad: {weather['humidity_percent']}%")
        print(f"📏 Presión: {weather['atmospheric_pressure_hpa']} hPa")
        print(f"☀️  Índice UV: {weather['uv_index']}")
        print(f"💨 Viento: {weather['wind_speed_kmh']} km/h @ {weather['wind_direction_degrees']}°")
        print(f"🏭 Calidad Aire (AQI): {weather['air_quality_index']}")
        
        if 'fan_active' in weather:
            print(f"\n🌀 Ventilador: {'ON' if weather['fan_active'] else 'OFF'}")
            print(f"🔥 Calefactor: {'ON' if weather['heater_active'] else 'OFF'}")
        
        # Alertas
        alerts = []
        if weather['temperature_celsius'] > 35:
            alerts.append("⚠️  TEMPERATURA ALTA")
        if weather['temperature_celsius'] < 0:
            alerts.append("❄️  TEMPERATURA BAJO CERO")
        if weather['humidity_percent'] > 80:
            alerts.append("💦 HUMEDAD ALTA")
        if weather['uv_index'] > 7:
            alerts.append("☀️  ÍNDICE UV EXTREMO")
        if weather['air_quality_index'] > 150:
            alerts.append("🏭 MALA CALIDAD DEL AIRE")
        
        if alerts:
            print(f"\n{'⚠️ '*10}")
            for alert in alerts:
                print(f"  {alert}")
            print(f"{'⚠️ '*10}")
        
        print(f"\n{'='*60}\n")
        
    except json.JSONDecodeError:
        print("❌ Error: No se pudo decodificar el JSON")
    except KeyError as e:
        print(f"❌ Error: Clave no encontrada: {e}")
    except Exception as e:
        print(f"❌ Error inesperado: {e}")

def main():
    client = mqtt.Client()
    client.on_connect = on_connect
    client.on_message = on_message
    
    print("🚀 Iniciando monitor de estación meteorológica...")
    print(f"📡 Broker: {BROKER}:{PORT}\n")
    
    try:
        client.connect(BROKER, PORT, 60)
        client.loop_forever()
    except KeyboardInterrupt:
        print("\n\n👋 Desconectando...")
        client.disconnect()
    except Exception as e:
        print(f"\n❌ Error de conexión: {e}")

if __name__ == "__main__":
    main()
```

**Instalar dependencias:**
```bash
pip install paho-mqtt
```

**Ejecutar:**
```bash
python mqtt_monitor.py
```

---

### Script Python - Control Remoto

Guarda como `mqtt_control.py`:

```python
#!/usr/bin/env python3
"""
Controlador de Estación Meteorológica
Envía comandos a la estación de forma interactiva
"""

import paho.mqtt.client as mqtt
import json
import sys

BROKER = "test.mosquitto.org"
PORT = 1883
TOPIC_CONTROL = "uah/alcala/weather/control"

def send_command(client, command):
    """Envía un comando a la estación"""
    payload = json.dumps(command)
    result = client.publish(TOPIC_CONTROL, payload)
    
    if result.rc == mqtt.MQTT_ERR_SUCCESS:
        print(f"✓ Comando enviado: {command}")
    else:
        print(f"❌ Error enviando comando (rc: {result.rc})")

def menu():
    """Muestra el menú de opciones"""
    print("\n" + "="*50)
    print("🎮 CONTROL DE ESTACIÓN METEOROLÓGICA")
    print("="*50)
    print("\n1. Activar ventilador")
    print("2. Desactivar ventilador")
    print("3. Activar calefactor")
    print("4. Desactivar calefactor")
    print("5. LED Rojo (Alarma)")
    print("6. LED Verde (OK)")
    print("7. LED Azul (Info)")
    print("8. LED Amarillo (Advertencia)")
    print("9. LED Apagado")
    print("10. Forzar lectura ahora")
    print("11. Reiniciar estación")
    print("0. Salir")
    print("="*50)

def main():
    client = mqtt.Client()
    
    try:
        print("🚀 Conectando al broker MQTT...")
        client.connect(BROKER, PORT, 60)
        client.loop_start()
        print(f"✓ Conectado a {BROKER}:{PORT}\n")
        
        while True:
            menu()
            choice = input("\nElige una opción: ").strip()
            
            if choice == "1":
                send_command(client, {"command": "fan_on", "value": True})
            elif choice == "2":
                send_command(client, {"command": "fan_on", "value": False})
            elif choice == "3":
                send_command(client, {"command": "heater_on", "value": True})
            elif choice == "4":
                send_command(client, {"command": "heater_on", "value": False})
            elif choice == "5":
                send_command(client, {"command": "led_rgb", "r": 255, "g": 0, "b": 0})
            elif choice == "6":
                send_command(client, {"command": "led_rgb", "r": 0, "g": 255, "b": 0})
            elif choice == "7":
                send_command(client, {"command": "led_rgb", "r": 0, "g": 0, "b": 255})
            elif choice == "8":
                send_command(client, {"command": "led_rgb", "r": 255, "g": 255, "b": 0})
            elif choice == "9":
                send_command(client, {"command": "led_rgb", "r": 0, "g": 0, "b": 0})
            elif choice == "10":
                send_command(client, {"command": "read_now"})
            elif choice == "11":
                confirm = input("⚠️  ¿Seguro que quieres reiniciar? (s/n): ")
                if confirm.lower() == 's':
                    send_command(client, {"command": "reset"})
            elif choice == "0":
                print("\n👋 ¡Hasta luego!")
                break
            else:
                print("❌ Opción no válida")
            
            input("\nPresiona Enter para continuar...")
        
    except KeyboardInterrupt:
        print("\n\n👋 Desconectando...")
    except Exception as e:
        print(f"\n❌ Error: {e}")
    finally:
        client.loop_stop()
        client.disconnect()

if __name__ == "__main__":
    main()
```

**Ejecutar:**
```bash
python mqtt_control.py
```

---

### Script Bash - Monitor Simple

Guarda como `monitor.sh`:

```bash
#!/bin/bash
# Monitor simple de la estación meteorológica

echo "=========================================="
echo "  Monitor de Estación Meteorológica"
echo "=========================================="
echo ""
echo "Suscribiéndose al tópico..."
echo "Broker: test.mosquitto.org"
echo "Tópico: uah/alcala/weather/data"
echo ""
echo "Presiona Ctrl+C para salir"
echo "=========================================="
echo ""

mosquitto_sub -h test.mosquitto.org \
              -t "uah/alcala/weather/data" \
              -F "@Y-@m-@d @H:@M:@S - %p" \
              | while read line; do
    echo "$line" | jq '.' 2>/dev/null || echo "$line"
done
```

**Dar permisos y ejecutar:**
```bash
chmod +x monitor.sh
./monitor.sh
```

---

## 🌐 Integración con Otros Sistemas

### Node.js - Guardar Datos en Base de Datos

```javascript
const mqtt = require('mqtt');
const mysql = require('mysql2');

const BROKER = 'mqtt://test.mosquitto.org';
const TOPIC = 'uah/alcala/weather/data';

// Conexión a base de datos
const db = mysql.createConnection({
    host: 'localhost',
    user: 'root',
    password: 'password',
    database: 'weather_db'
});

// Conexión MQTT
const client = mqtt.connect(BROKER);

client.on('connect', () => {
    console.log('✓ Conectado al broker MQTT');
    client.subscribe(TOPIC);
});

client.on('message', (topic, message) => {
    try {
        const data = JSON.parse(message.toString());
        
        const sql = `INSERT INTO weather_data 
            (sensor_id, temperature, humidity, pressure, uv_index, 
             wind_speed, wind_direction, air_quality, timestamp) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())`;
        
        const values = [
            data.sensor_id,
            data.data.temperature_celsius,
            data.data.humidity_percent,
            data.data.atmospheric_pressure_hpa,
            data.data.uv_index,
            data.data.wind_speed_kmh,
            data.data.wind_direction_degrees,
            data.data.air_quality_index
        ];
        
        db.execute(sql, values, (err, results) => {
            if (err) {
                console.error('❌ Error guardando datos:', err);
            } else {
                console.log('✓ Datos guardados en BD');
            }
        });
        
    } catch (err) {
        console.error('❌ Error procesando mensaje:', err);
    }
});
```

---

### PHP - API REST

```php
<?php
// api/weather.php
// API REST para obtener los últimos datos meteorológicos

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

$servername = "localhost";
$username = "root";
$password = "password";
$dbname = "weather_db";

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die(json_encode(['error' => 'Error de conexión']));
}

$sql = "SELECT * FROM weather_data ORDER BY timestamp DESC LIMIT 1";
$result = $conn->query($sql);

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    echo json_encode([
        'success' => true,
        'data' => $row
    ]);
} else {
    echo json_encode([
        'success' => false,
        'message' => 'No hay datos disponibles'
    ]);
}

$conn->close();
?>
```

---

## 🎯 Casos de Uso

### Caso 1: Alerta de Temperatura Alta

**Script que envía alerta si temperatura > 35°C:**

```python
import paho.mqtt.client as mqtt
import json
import smtplib
from email.mime.text import MIMEText

def send_email_alert(temp):
    msg = MIMEText(f"¡ALERTA! Temperatura alta: {temp}°C")
    msg['Subject'] = '⚠️ Alerta Meteorológica'
    msg['From'] = 'estacion@example.com'
    msg['To'] = 'admin@example.com'
    
    s = smtplib.SMTP('localhost')
    s.send_message(msg)
    s.quit()

def on_message(client, userdata, msg):
    data = json.loads(msg.payload.decode())
    temp = data['data']['temperature_celsius']
    
    if temp > 35:
        print(f"⚠️ ALERTA: Temperatura alta: {temp}°C")
        send_email_alert(temp)
        
        # Activar ventilador
        client.publish('uah/alcala/weather/control', 
                      json.dumps({"command": "fan_on", "value": True}))

client = mqtt.Client()
client.on_message = on_message
client.connect("test.mosquitto.org", 1883)
client.subscribe("uah/alcala/weather/data")
client.loop_forever()
```

### Caso 2: Dashboard Web en Tiempo Real

**HTML + JavaScript:**

```html
<!DOCTYPE html>
<html>
<head>
    <title>Estación Meteorológica - Dashboard</title>
    <script src="https://unpkg.com/mqtt/dist/mqtt.min.js"></script>
    <style>
        body {
            font-family: Arial, sans-serif;
            padding: 20px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }
        .card {
            background: white;
            border-radius: 10px;
            padding: 20px;
            margin: 10px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }
        .value {
            font-size: 2em;
            font-weight: bold;
            color: #667eea;
        }
    </style>
</head>
<body>
    <h1 style="color: white;">🌤️ Estación Meteorológica - Alcalá de Henares</h1>
    
    <div class="card">
        <h2>🌡️ Temperatura</h2>
        <div class="value" id="temp">--</div>
    </div>
    
    <div class="card">
        <h2>💧 Humedad</h2>
        <div class="value" id="humidity">--</div>
    </div>
    
    <div class="card">
        <h2>☀️ Índice UV</h2>
        <div class="value" id="uv">--</div>
    </div>

    <script>
        const client = mqtt.connect('ws://test.mosquitto.org:8080/mqtt');
        
        client.on('connect', () => {
            console.log('✓ Conectado');
            client.subscribe('uah/alcala/weather/data');
        });
        
        client.on('message', (topic, message) => {
            const data = JSON.parse(message.toString());
            document.getElementById('temp').innerText = 
                data.data.temperature_celsius + '°C';
            document.getElementById('humidity').innerText = 
                data.data.humidity_percent + '%';
            document.getElementById('uv').innerText = 
                data.data.uv_index;
        });
    </script>
</body>
</html>
```

---

## 🔐 Seguridad

### Usar MQTT con Autenticación

**Modificar config.h:**

```cpp
const char* MQTT_USER = "tu_usuario";
const char* MQTT_PASS = "tu_contraseña";
```

**Modificar ESP32_Utils_MQTT.hpp:**

```cpp
if (mqttClient.connect(MQTT_CLIENT_NAME, MQTT_USER, MQTT_PASS)) {
    // ...
}
```

---

¡Explora, experimenta y personaliza según tus necesidades! 🚀
