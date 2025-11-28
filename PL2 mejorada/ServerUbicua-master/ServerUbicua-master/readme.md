
1. MQTT Explorer - Conectar
Campo	Valor
Host	localhost
Puerto	3000
Usuario	(vacío o admin)
Contraseña	(vacío o admin)


2. Enviar JSON desde MQTT Explorer
Campo	Valor
Topic	sensores/temperatura/lab08
Payload (JSON)	↓

{"sensor_id":"LAB08JAV-G1","timestamp":"2025-11-27 18:30:00:000","location":{"altitude_meters":667.3},"data":{"temperature_celsius":22.4,"humidity_percent":58.7,"atmospheric_pressure_hpa":1013.2}}


docker exec -it base-datos-ubicua psql -U admin -d ubicua -c "SELECT * FROM sensor_readings ORDER BY timestamp DESC LIMIT 10;"

4. Abrir Web UI
URL	Descripción
http://localhost:3002/Server/	Página principal
http://localhost:3002/Server/GetData	API JSON directa

docker-compose down



mvn clean package -DskipTests


docker-compose build --no-cache