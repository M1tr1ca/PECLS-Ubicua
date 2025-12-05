package com.example.apppecl3;

import com.google.gson.JsonArray;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Modelo para la respuesta de GetAllData
 */
public class AllDataResponse {
    
    @SerializedName("weather")
    private List<WeatherMeasurement> weather;
    
    @SerializedName("trafficCounter")
    private List<TrafficCounterMeasurement> trafficCounter;
    
    @SerializedName("trafficLight")
    private List<TrafficLightMeasurement> trafficLight;
    
    @SerializedName("informationDisplay")
    private List<DisplayMeasurement> informationDisplay;

    public List<WeatherMeasurement> getWeather() { return weather; }
    public List<TrafficCounterMeasurement> getTrafficCounter() { return trafficCounter; }
    public List<TrafficLightMeasurement> getTrafficLight() { return trafficLight; }
    public List<DisplayMeasurement> getInformationDisplay() { return informationDisplay; }

    // Clase interna para mediciones meteorológicas
    public static class WeatherMeasurement {
        private String sensorId;
        private String timestamp;
        private double temperature;
        private double humidity;
        private double pressure;
        private double altitude;

        public String getSensorId() { return sensorId; }
        public String getTimestamp() { return timestamp; }
        public double getTemperature() { return temperature; }
        public double getHumidity() { return humidity; }
        public double getPressure() { return pressure; }
        public double getAltitude() { return altitude; }
    }

    // Clase para contador de tráfico
    public static class TrafficCounterMeasurement {
        private String sensorId;
        private String timestamp;
        private int vehicleCount;
        private int pedestrianCount;
        private int bicycleCount;
        private String direction;
        private double averageSpeedKmh;
        private String trafficDensity;

        public String getSensorId() { return sensorId; }
        public String getTimestamp() { return timestamp; }
        public int getVehicleCount() { return vehicleCount; }
        public int getPedestrianCount() { return pedestrianCount; }
        public int getBicycleCount() { return bicycleCount; }
        public String getDirection() { return direction; }
        public double getAverageSpeedKmh() { return averageSpeedKmh; }
        public String getTrafficDensity() { return trafficDensity; }
    }

    // Clase para semáforos
    public static class TrafficLightMeasurement {
        private String sensorId;
        private String timestamp;
        private String currentState;
        private int timeRemainingSeconds;
        private boolean malfunctionDetected;

        public String getSensorId() { return sensorId; }
        public String getTimestamp() { return timestamp; }
        public String getCurrentState() { return currentState; }
        public int getTimeRemainingSeconds() { return timeRemainingSeconds; }
        public boolean isMalfunctionDetected() { return malfunctionDetected; }
    }

    // Clase para pantallas de información
    public static class DisplayMeasurement {
        private String sensorId;
        private String timestamp;
        private String displayStatus;
        private String currentMessage;
        private int brightnessLevel;

        public String getSensorId() { return sensorId; }
        public String getTimestamp() { return timestamp; }
        public String getDisplayStatus() { return displayStatus; }
        public String getCurrentMessage() { return currentMessage; }
        public int getBrightnessLevel() { return brightnessLevel; }
    }
}
