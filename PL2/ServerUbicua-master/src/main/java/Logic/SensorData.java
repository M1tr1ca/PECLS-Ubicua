package logic;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo para los datos del sensor meteorológico
 * Compatible con el formato de la PL1
 */
public class SensorData {
    
    @SerializedName("temperature_celsius")
    private Double temperatureCelsius;
    
    @SerializedName("humidity_percent")
    private Double humidityPercent;
    
    @SerializedName("atmospheric_pressure_hpa")
    private Double atmosphericPressureHpa;
    
    @SerializedName("air_quality_index")
    private Integer airQualityIndex;
    
    @SerializedName("wind_speed_kmh")
    private Double windSpeedKmh;
    
    @SerializedName("wind_direction_degrees")
    private Integer windDirectionDegrees;
    
    @SerializedName("uv_index")
    private Integer uvIndex;

    public SensorData() {
    }

    // Getters y Setters
    public Double getTemperatureCelsius() {
        return temperatureCelsius;
    }

    public void setTemperatureCelsius(Double temperatureCelsius) {
        this.temperatureCelsius = temperatureCelsius;
    }

    public Double getHumidityPercent() {
        return humidityPercent;
    }

    public void setHumidityPercent(Double humidityPercent) {
        this.humidityPercent = humidityPercent;
    }

    public Double getAtmosphericPressureHpa() {
        return atmosphericPressureHpa;
    }

    public void setAtmosphericPressureHpa(Double atmosphericPressureHpa) {
        this.atmosphericPressureHpa = atmosphericPressureHpa;
    }

    public Integer getAirQualityIndex() {
        return airQualityIndex;
    }

    public void setAirQualityIndex(Integer airQualityIndex) {
        this.airQualityIndex = airQualityIndex;
    }

    public Double getWindSpeedKmh() {
        return windSpeedKmh;
    }

    public void setWindSpeedKmh(Double windSpeedKmh) {
        this.windSpeedKmh = windSpeedKmh;
    }

    public Integer getWindDirectionDegrees() {
        return windDirectionDegrees;
    }

    public void setWindDirectionDegrees(Integer windDirectionDegrees) {
        this.windDirectionDegrees = windDirectionDegrees;
    }

    public Integer getUvIndex() {
        return uvIndex;
    }

    public void setUvIndex(Integer uvIndex) {
        this.uvIndex = uvIndex;
    }
}

