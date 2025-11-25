package logic;

import Database.ConectionDDBB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Lógica de negocio para las lecturas de sensores
 */
public class SensorLogic {

    /**
     * Guarda una lectura de sensor en la base de datos
     */
    public static boolean saveSensorReading(SensorReading reading) {
        ConectionDDBB conector = new ConectionDDBB();
        Connection con = null;
        boolean success = false;

        try {
            con = conector.obtainConnection(true);
            Log.log.info("Base de datos conectada para guardar lectura");

            // Verificar si el sensor existe
            if (!sensorExists(con, reading.getSensorId())) {
                Log.log.warn("Sensor no encontrado: {}. Intentando crear...", reading.getSensorId());
                // Intentar crear el sensor si no existe
                if (!createSensor(con, reading)) {
                    Log.log.error("No se pudo crear el sensor: {}", reading.getSensorId());
                    return false;
                }
            }

            // Insertar la lectura
            String sql = "INSERT INTO sensor_readings (sensor_id, timestamp, temperature_celsius, " +
                        "humidity_percent, atmospheric_pressure_hpa, altitude_meters) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, reading.getSensorId());
            ps.setTimestamp(2, reading.getTimestamp());

            // Datos del sensor
            if (reading.getData() != null) {
                setNullableDouble(ps, 3, reading.getData().getTemperatureCelsius());
                setNullableDouble(ps, 4, reading.getData().getHumidityPercent());
                setNullableDouble(ps, 5, reading.getData().getAtmosphericPressureHpa());
            } else {
                ps.setNull(3, java.sql.Types.DOUBLE);
                ps.setNull(4, java.sql.Types.DOUBLE);
                ps.setNull(5, java.sql.Types.DOUBLE);
            }

            // Altitud de la ubicación
            if (reading.getLocation() != null && reading.getLocation().getAltitudeMeters() != null) {
                ps.setDouble(6, reading.getLocation().getAltitudeMeters());
            } else {
                ps.setNull(6, java.sql.Types.DOUBLE);
            }

            int rows = ps.executeUpdate();
            success = rows > 0;

            Log.log.info("Lectura guardada para sensor: {} - Éxito: {}", reading.getSensorId(), success);

        } catch (SQLException e) {
            Log.log.error("Error SQL guardando lectura: {}", e.getMessage());
        } catch (Exception e) {
            Log.log.error("Error guardando lectura: {}", e.getMessage());
        } finally {
            conector.closeConnection(con);
        }

        return success;
    }

    /**
     * Verifica si un sensor existe en la base de datos
     */
    private static boolean sensorExists(Connection con, String sensorId) throws SQLException {
        String sql = "SELECT 1 FROM sensors WHERE sensor_id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, sensorId);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    /**
     * Crea un nuevo sensor en la base de datos
     */
    private static boolean createSensor(Connection con, SensorReading reading) {
        try {
            // Primero verificar si la calle existe
            if (!streetExists(con, reading.getStreetId())) {
                Log.log.warn("Calle no encontrada: {}. Creando...", reading.getStreetId());
                createStreet(con, reading);
            }

            String sql = "INSERT INTO sensors (sensor_id, sensor_type, street_id) VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, reading.getSensorId());
            ps.setString(2, reading.getSensorType() != null ? reading.getSensorType() : "weather");
            ps.setString(3, reading.getStreetId());
            ps.executeUpdate();
            Log.log.info("Sensor creado: {}", reading.getSensorId());
            return true;
        } catch (SQLException e) {
            Log.log.error("Error creando sensor: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si una calle existe en la base de datos
     */
    private static boolean streetExists(Connection con, String streetId) throws SQLException {
        String sql = "SELECT 1 FROM streets WHERE street_id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, streetId);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    /**
     * Crea una nueva calle en la base de datos
     */
    private static void createStreet(Connection con, SensorReading reading) throws SQLException {
        String sql = "INSERT INTO streets (street_id, street_name, district, neighborhood, " +
                    "latitude_start, latitude_end, longitude_start, longitude_end) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, reading.getStreetId());
        ps.setString(2, "Calle " + reading.getStreetId()); // Nombre por defecto

        Location loc = reading.getLocation();
        if (loc != null) {
            ps.setString(3, loc.getDistrict() != null ? loc.getDistrict() : "Desconocido");
            ps.setString(4, loc.getNeighborhood() != null ? loc.getNeighborhood() : "Desconocido");
            ps.setDouble(5, loc.getLatitudeStart() != null ? loc.getLatitudeStart() : 
                           (loc.getLatitude() != null ? loc.getLatitude() : 0.0));
            ps.setDouble(6, loc.getLatitudeEnd() != null ? loc.getLatitudeEnd() : 
                           (loc.getLatitude() != null ? loc.getLatitude() : 0.0));
            ps.setDouble(7, loc.getLongitudeStart() != null ? loc.getLongitudeStart() : 
                           (loc.getLongitude() != null ? loc.getLongitude() : 0.0));
            ps.setDouble(8, loc.getLongitudeEnd() != null ? loc.getLongitudeEnd() : 
                           (loc.getLongitude() != null ? loc.getLongitude() : 0.0));
        } else {
            ps.setString(3, "Desconocido");
            ps.setString(4, "Desconocido");
            ps.setDouble(5, 0.0);
            ps.setDouble(6, 0.0);
            ps.setDouble(7, 0.0);
            ps.setDouble(8, 0.0);
        }

        ps.executeUpdate();
        Log.log.info("Calle creada: {}", reading.getStreetId());
    }

    /**
     * Obtiene las últimas lecturas de todos los sensores
     */
    public static List<SensorReading> getLatestReadings(int limit) {
        List<SensorReading> readings = new ArrayList<>();
        ConectionDDBB conector = new ConectionDDBB();
        Connection con = null;

        try {
            con = conector.obtainConnection(true);
            Log.log.info("Obteniendo últimas {} lecturas", limit);

            String sql = "SELECT sr.sensor_id, sr.timestamp, sr.temperature_celsius, " +
                        "sr.humidity_percent, sr.atmospheric_pressure_hpa, sr.altitude_meters, " +
                        "s.sensor_type, s.street_id, " +
                        "st.street_name, st.district, st.neighborhood, " +
                        "st.latitude_start, st.latitude_end, st.longitude_start, st.longitude_end " +
                        "FROM sensor_readings sr " +
                        "JOIN sensors s ON sr.sensor_id = s.sensor_id " +
                        "JOIN streets st ON s.street_id = st.street_id " +
                        "ORDER BY sr.timestamp DESC LIMIT ?";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                readings.add(mapResultSetToSensorReading(rs));
            }

            Log.log.info("Obtenidas {} lecturas", readings.size());

        } catch (SQLException e) {
            Log.log.error("Error SQL obteniendo lecturas: {}", e.getMessage());
        } catch (Exception e) {
            Log.log.error("Error obteniendo lecturas: {}", e.getMessage());
        } finally {
            conector.closeConnection(con);
        }

        return readings;
    }

    /**
     * Obtiene las últimas lecturas de un sensor específico
     */
    public static List<SensorReading> getLatestReadingsBySensor(String sensorId, int limit) {
        List<SensorReading> readings = new ArrayList<>();
        ConectionDDBB conector = new ConectionDDBB();
        Connection con = null;

        try {
            con = conector.obtainConnection(true);
            Log.log.info("Obteniendo últimas {} lecturas del sensor {}", limit, sensorId);

            String sql = "SELECT sr.sensor_id, sr.timestamp, sr.temperature_celsius, " +
                        "sr.humidity_percent, sr.atmospheric_pressure_hpa, sr.altitude_meters, " +
                        "s.sensor_type, s.street_id, " +
                        "st.street_name, st.district, st.neighborhood, " +
                        "st.latitude_start, st.latitude_end, st.longitude_start, st.longitude_end " +
                        "FROM sensor_readings sr " +
                        "JOIN sensors s ON sr.sensor_id = s.sensor_id " +
                        "JOIN streets st ON s.street_id = st.street_id " +
                        "WHERE sr.sensor_id = ? " +
                        "ORDER BY sr.timestamp DESC LIMIT ?";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, sensorId);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                readings.add(mapResultSetToSensorReading(rs));
            }

            Log.log.info("Obtenidas {} lecturas para sensor {}", readings.size(), sensorId);

        } catch (SQLException e) {
            Log.log.error("Error SQL obteniendo lecturas por sensor: {}", e.getMessage());
        } catch (Exception e) {
            Log.log.error("Error obteniendo lecturas por sensor: {}", e.getMessage());
        } finally {
            conector.closeConnection(con);
        }

        return readings;
    }

    /**
     * Mapea un ResultSet a un objeto SensorReading
     */
    private static SensorReading mapResultSetToSensorReading(ResultSet rs) throws SQLException {
        SensorReading reading = new SensorReading();
        reading.setSensorId(rs.getString("sensor_id"));
        reading.setTimestamp(rs.getTimestamp("timestamp"));
        reading.setSensorType(rs.getString("sensor_type"));
        reading.setStreetId(rs.getString("street_id"));
        reading.setStreetName(rs.getString("street_name"));
        reading.setDistrict(rs.getString("district"));
        reading.setNeighborhood(rs.getString("neighborhood"));

        // Datos del sensor
        SensorData data = new SensorData();
        data.setTemperatureCelsius(getNullableDouble(rs, "temperature_celsius"));
        data.setHumidityPercent(getNullableDouble(rs, "humidity_percent"));
        data.setAtmosphericPressureHpa(getNullableDouble(rs, "atmospheric_pressure_hpa"));
        reading.setData(data);

        // Ubicación
        Location loc = new Location();
        loc.setLatitudeStart(getNullableDouble(rs, "latitude_start"));
        loc.setLatitudeEnd(getNullableDouble(rs, "latitude_end"));
        loc.setLongitudeStart(getNullableDouble(rs, "longitude_start"));
        loc.setLongitudeEnd(getNullableDouble(rs, "longitude_end"));
        loc.setAltitudeMeters(getNullableDouble(rs, "altitude_meters"));
        loc.setDistrict(rs.getString("district"));
        loc.setNeighborhood(rs.getString("neighborhood"));
        reading.setLocation(loc);

        return reading;
    }

    /**
     * Helper para manejar valores Double nulos en PreparedStatement
     */
    private static void setNullableDouble(PreparedStatement ps, int index, Double value) throws SQLException {
        if (value != null) {
            ps.setDouble(index, value);
        } else {
            ps.setNull(index, java.sql.Types.DOUBLE);
        }
    }

    /**
     * Helper para obtener valores Double que pueden ser nulos
     */
    private static Double getNullableDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }
}

