package com.example.apppecl3;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo para representar una calle obtenida del servidor (API GetStreets)
 * El JSON usa snake_case y las coordenadas están en un objeto location
 */
public class Street {
    @SerializedName("street_id")
    private String streetId;

    @SerializedName("street_name")
    private String streetName;

    @SerializedName("district")
    private String district;

    @SerializedName("neighborhood")
    private String neighborhood;

    @SerializedName("location")
    private Location location;

    // Clase interna para las coordenadas
    public static class Location {
        @SerializedName("latitude_start")
        private double latitudeStart;

        @SerializedName("latitude_end")
        private double latitudeEnd;

        @SerializedName("longitude_start")
        private double longitudeStart;

        @SerializedName("longitude_end")
        private double longitudeEnd;

        public double getLatitudeStart() { return latitudeStart; }
        public double getLatitudeEnd() { return latitudeEnd; }
        public double getLongitudeStart() { return longitudeStart; }
        public double getLongitudeEnd() { return longitudeEnd; }
    }

    // Getters
    public String getStreetId() { return streetId; }
    public String getStreetName() { return streetName; }
    public String getDistrict() { return district; }
    public String getNeighborhood() { return neighborhood; }
    public Location getLocation() { return location; }

    // Métodos de conveniencia para obtener el centro
    public double getCenterLatitude() {
        return location != null ? (location.latitudeStart + location.latitudeEnd) / 2 : 0;
    }

    public double getCenterLongitude() {
        return location != null ? (location.longitudeStart + location.longitudeEnd) / 2 : 0;
    }

    /**
     * Convierte este Street (API) a StreetWithSensors (modelo interno)
     */
    public StreetWithSensors toStreetWithSensors() {
        StreetWithSensors sws = new StreetWithSensors(
                streetId,
                streetName,
                district,
                neighborhood,
                location != null ? location.latitudeStart : 0,
                location != null ? location.latitudeEnd : 0,
                location != null ? location.longitudeStart : 0,
                location != null ? location.longitudeEnd : 0
        );
        return sws;
    }

    @Override
    public String toString() {
        return streetName + " (" + district + ")";
    }
}
