package com.example.apppecl3;

import java.util.List;

/**
 * Respuesta del servidor con calles y sensores
 */
public class StreetsResponse {
    private List<StreetData> streets;

    public List<StreetData> getStreets() { return streets; }
    public void setStreets(List<StreetData> streets) { this.streets = streets; }

    public static class StreetData {
        private String streetId;
        private String streetName;
        private String district;
        private String neighborhood;
        private double latitudeStart;
        private double latitudeEnd;
        private double longitudeStart;
        private double longitudeEnd;
        private List<SensorData> sensors;

        public String getStreetId() { return streetId; }
        public String getStreetName() { return streetName; }
        public String getDistrict() { return district; }
        public String getNeighborhood() { return neighborhood; }
        public double getLatitudeStart() { return latitudeStart; }
        public double getLatitudeEnd() { return latitudeEnd; }
        public double getLongitudeStart() { return longitudeStart; }
        public double getLongitudeEnd() { return longitudeEnd; }
        public List<SensorData> getSensors() { return sensors; }
    }

    public static class SensorData {
        private String sensorId;
        private String sensorType;

        public String getSensorId() { return sensorId; }
        public String getSensorType() { return sensorType; }
    }
}
