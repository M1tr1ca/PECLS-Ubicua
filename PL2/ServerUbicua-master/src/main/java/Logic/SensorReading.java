package logic;

import java.sql.Timestamp;

/**
 * Modelo para representar una lectura de sensor
 * Compatible con el formato JSON de la PL1
 */
public class SensorReading {
    
    private String sensorId;
    private String sensorType;
    private String streetId;
    private Timestamp timestamp;
    private Location location;
    private SensorData data;
    
    // Campos adicionales para la vista completa
    private String streetName;
    private String district;
    private String neighborhood;

    public SensorReading() {
    }

    public SensorReading(String sensorId, String sensorType, String streetId, 
                         Timestamp timestamp, Location location, SensorData data) {
        this.sensorId = sensorId;
        this.sensorType = sensorType;
        this.streetId = streetId;
        this.timestamp = timestamp;
        this.location = location;
        this.data = data;
    }

    // Getters y Setters
    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public String getStreetId() {
        return streetId;
    }

    public void setStreetId(String streetId) {
        this.streetId = streetId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public SensorData getData() {
        return data;
    }

    public void setData(SensorData data) {
        this.data = data;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }
}

