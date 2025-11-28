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

-- Tabla para contadores de tráfico
CREATE TABLE traffic_counter_readings (
  sensor_id TEXT NOT NULL,
  timestamp TIMESTAMP NOT NULL,
  vehicle_count INTEGER,
  pedestrian_count INTEGER,
  bicycle_count INTEGER,
  direction TEXT,
  counter_type TEXT,
  technology TEXT,
  average_speed_kmh DOUBLE PRECISION,
  occupancy_percentage DOUBLE PRECISION,
  traffic_density TEXT,
  FOREIGN KEY (sensor_id) REFERENCES sensors(sensor_id)
);

-- Tabla para semáforos
CREATE TABLE traffic_light_readings (
  sensor_id TEXT NOT NULL,
  timestamp TIMESTAMP NOT NULL,
  current_state TEXT,
  cycle_position_seconds INTEGER,
  time_remaining_seconds INTEGER,
  cycle_duration_seconds INTEGER,
  traffic_light_type TEXT,
  circulation_direction TEXT,
  pedestrian_waiting BOOLEAN,
  pedestrian_button_pressed BOOLEAN,
  malfunction_detected BOOLEAN,
  cycle_count INTEGER,
  state_changed BOOLEAN,
  last_state_change TIMESTAMP,
  FOREIGN KEY (sensor_id) REFERENCES sensors(sensor_id)
);

-- Tabla para pantallas de información
CREATE TABLE information_display_readings (
  sensor_id TEXT NOT NULL,
  timestamp TIMESTAMP NOT NULL,
  display_status TEXT,
  current_message TEXT,
  content_type TEXT,
  brightness_level INTEGER,
  display_type TEXT,
  display_size_inches DOUBLE PRECISION,
  supports_color BOOLEAN,
  temperature_celsius DOUBLE PRECISION,
  energy_consumption_watts DOUBLE PRECISION,
  last_content_update TIMESTAMP,
  FOREIGN KEY (sensor_id) REFERENCES sensors(sensor_id)
);

-- Índice para mejorar consultas por timestamp
CREATE INDEX idx_sensor_readings_timestamp ON sensor_readings(timestamp DESC);
CREATE INDEX idx_sensor_readings_sensor_timestamp ON sensor_readings(sensor_id, timestamp DESC);
CREATE INDEX idx_traffic_counter_timestamp ON traffic_counter_readings(timestamp DESC);
CREATE INDEX idx_traffic_light_timestamp ON traffic_light_readings(timestamp DESC);
CREATE INDEX idx_information_display_timestamp ON information_display_readings(timestamp DESC);

\copy streets FROM 'docker-entrypoint-initdb.d/tabla_calles.csv' WITH (FORMAT CSV, HEADER, DELIMITER ',');
\copy sensors FROM 'docker-entrypoint-initdb.d/tabla_sensores.csv' WITH (FORMAT CSV, HEADER, DELIMITER ',');



