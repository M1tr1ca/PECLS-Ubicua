SET CLIENT_ENCODING = 'UTF8';
CREATE TABLE streets (
  street_id TEXT PRIMARY KEY,
  street_name TEXT,
  district TEXT,
  neighborhood TEXT,
  latitude_start DOUBLE PRECISION NOT NULL,
  latitude_end DOUBLE PRECISION NOT NULL,
  longitude_start DOUBLE PRECISION NOT NULL,
  longitude_end DOUBLE PRECISION NOT NULL
);

CREATE TABLE sensors (
  sensor_id TEXT PRIMARY KEY,
  sensor_type TEXT NOT NULL,
  street_id TEXT NOT NULL,
  FOREIGN KEY (street_id) REFERENCES streets(street_id)
);

CREATE TABLE sensor_readings (
  sensor_id TEXT NOT NULL,
  timestamp TIMESTAMP NOT NULL,
  temperature_celsius DOUBLE PRECISION,
  humidity_percent DOUBLE PRECISION,
  atmospheric_pressure_hpa DOUBLE PRECISION,
  altitude_meters DOUBLE PRECISION,
  FOREIGN KEY (sensor_id) REFERENCES sensors(sensor_id)
);

-- Índice para mejorar consultas por timestamp
CREATE INDEX idx_sensor_readings_timestamp ON sensor_readings(timestamp DESC);
CREATE INDEX idx_sensor_readings_sensor_timestamp ON sensor_readings(sensor_id, timestamp DESC);
\copy streets FROM 'docker-entrypoint-initdb.d/tabla_calles.csv' WITH (FORMAT CSV, HEADER, DELIMITER ',');
\copy sensors FROM 'docker-entrypoint-initdb.d/tabla_sensores.csv' WITH (FORMAT CSV, HEADER, DELIMITER ',');



