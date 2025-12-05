package com.example.apppecl3;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo para representar la información de un sensor desde el servidor (API GetSensors)
 * Contiene el sensor_id real de la base de datos
 */
public class SensorInfo {
    @SerializedName("sensor_id")
    private String sensorId;

    @SerializedName("sensor_type")
    private String sensorType;

    @SerializedName("street_id")
    private String streetId;

    @SerializedName("street_name")
    private String streetName;

    @SerializedName("district")
    private String district;

    @SerializedName("neighborhood")
    private String neighborhood;

    public SensorInfo() {}

    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }

    public String getSensorType() { return sensorType; }
    public void setSensorType(String sensorType) { this.sensorType = sensorType; }

    public String getStreetId() { return streetId; }
    public void setStreetId(String streetId) { this.streetId = streetId; }

    public String getStreetName() { return streetName; }
    public void setStreetName(String streetName) { this.streetName = streetName; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getNeighborhood() { return neighborhood; }
    public void setNeighborhood(String neighborhood) { this.neighborhood = neighborhood; }

    @Override
    public String toString() {
        return sensorId + " (" + sensorType + ") - " + streetId;
    }
}
