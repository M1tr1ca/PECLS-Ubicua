package logic;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo para la ubicación del sensor
 */
public class Location {
    
    @SerializedName("latitude_start")
    private Double latitudeStart;
    
    @SerializedName("latitude_end")
    private Double latitudeEnd;
    
    @SerializedName("longitude_start")
    private Double longitudeStart;
    
    @SerializedName("longitude_end")
    private Double longitudeEnd;
    
    // Campos alternativos para compatibilidad con formato simple
    private Double latitude;
    private Double longitude;
    
    @SerializedName("altitude_meters")
    private Double altitudeMeters;
    
    private String district;
    private String neighborhood;

    public Location() {
    }

    // Getters y Setters
    public Double getLatitudeStart() {
        return latitudeStart;
    }

    public void setLatitudeStart(Double latitudeStart) {
        this.latitudeStart = latitudeStart;
    }

    public Double getLatitudeEnd() {
        return latitudeEnd;
    }

    public void setLatitudeEnd(Double latitudeEnd) {
        this.latitudeEnd = latitudeEnd;
    }

    public Double getLongitudeStart() {
        return longitudeStart;
    }

    public void setLongitudeStart(Double longitudeStart) {
        this.longitudeStart = longitudeStart;
    }

    public Double getLongitudeEnd() {
        return longitudeEnd;
    }

    public void setLongitudeEnd(Double longitudeEnd) {
        this.longitudeEnd = longitudeEnd;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getAltitudeMeters() {
        return altitudeMeters;
    }

    public void setAltitudeMeters(Double altitudeMeters) {
        this.altitudeMeters = altitudeMeters;
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
    
    /**
     * Obtiene la latitud efectiva (preferencia a latitudeStart, luego latitude)
     */
    public Double getEffectiveLatitude() {
        return latitudeStart != null ? latitudeStart : latitude;
    }
    
    /**
     * Obtiene la longitud efectiva (preferencia a longitudeStart, luego longitude)
     */
    public Double getEffectiveLongitude() {
        return longitudeStart != null ? longitudeStart : longitude;
    }
}

