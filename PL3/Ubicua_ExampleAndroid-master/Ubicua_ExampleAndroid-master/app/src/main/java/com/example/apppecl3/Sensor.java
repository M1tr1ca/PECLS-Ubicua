package com.example.apppecl3;

/**
 * Modelo para representar un sensor
 */
public class Sensor {
    private String sensorId;
    private String sensorType;
    private String streetId;

    public Sensor() {}

    public Sensor(String sensorId, String sensorType) {
        this.sensorId = sensorId;
        this.sensorType = sensorType;
    }

    public Sensor(String sensorId, String sensorType, String streetId) {
        this.sensorId = sensorId;
        this.sensorType = sensorType;
        this.streetId = streetId;
    }

    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }

    public String getSensorType() { return sensorType; }
    public void setSensorType(String sensorType) { this.sensorType = sensorType; }

    public String getStreetId() { return streetId; }
    public void setStreetId(String streetId) { this.streetId = streetId; }

    @Override
    public String toString() {
        return sensorId + " (" + sensorType + ")";
    }
}
