package com.example.apppecl3;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo para representar una calle con sus coordenadas y sensores asociados
 */
public class StreetWithSensors {
    private String streetId;
    private String streetName;
    private String district;
    private String neighborhood;
    private double latitudeStart;
    private double latitudeEnd;
    private double longitudeStart;
    private double longitudeEnd;
    private List<Sensor> sensors;

    public StreetWithSensors() {
        this.sensors = new ArrayList<>();
    }

    public StreetWithSensors(String streetId, String streetName, String district, String neighborhood,
                             double latitudeStart, double latitudeEnd, double longitudeStart, double longitudeEnd) {
        this.streetId = streetId;
        this.streetName = streetName;
        this.district = district;
        this.neighborhood = neighborhood;
        this.latitudeStart = latitudeStart;
        this.latitudeEnd = latitudeEnd;
        this.longitudeStart = longitudeStart;
        this.longitudeEnd = longitudeEnd;
        this.sensors = new ArrayList<>();
    }

    // Obtener el punto central de la calle
    public double getCenterLatitude() {
        return (latitudeStart + latitudeEnd) / 2;
    }

    public double getCenterLongitude() {
        return (longitudeStart + longitudeEnd) / 2;
    }

    // Getters y Setters
    public String getStreetId() { return streetId; }
    public void setStreetId(String streetId) { this.streetId = streetId; }

    public String getStreetName() { return streetName; }
    public void setStreetName(String streetName) { this.streetName = streetName; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getNeighborhood() { return neighborhood; }
    public void setNeighborhood(String neighborhood) { this.neighborhood = neighborhood; }

    public double getLatitudeStart() { return latitudeStart; }
    public void setLatitudeStart(double latitudeStart) { this.latitudeStart = latitudeStart; }

    public double getLatitudeEnd() { return latitudeEnd; }
    public void setLatitudeEnd(double latitudeEnd) { this.latitudeEnd = latitudeEnd; }

    public double getLongitudeStart() { return longitudeStart; }
    public void setLongitudeStart(double longitudeStart) { this.longitudeStart = longitudeStart; }

    public double getLongitudeEnd() { return longitudeEnd; }
    public void setLongitudeEnd(double longitudeEnd) { this.longitudeEnd = longitudeEnd; }

    public List<Sensor> getSensors() { return sensors; }
    public void setSensors(List<Sensor> sensors) { this.sensors = sensors; }

    public void addSensor(Sensor sensor) {
        this.sensors.add(sensor);
    }

    @Override
    public String toString() {
        return streetName + " (" + district + ")";
    }
}
