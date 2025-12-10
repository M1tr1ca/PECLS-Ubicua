package com.example.apppecl3;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Modelo para la respuesta de GetAllData
 * Formato: {"weather":[...],"trafficCounter":[...],"trafficLight":[...],"informationDisplay":[...]}
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
        @SerializedName("sensorId")
        private String sensorId;
        
        @SerializedName("timestamp")
        private String timestamp;
        
        @SerializedName("temperature")
        private double temperature;
        
        @SerializedName("humidity")
        private double humidity;
        
        @SerializedName("pressure")
        private double pressure;
        
        @SerializedName("altitude")
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
        @SerializedName("sensorId")
        private String sensorId;

        
        @SerializedName("timestamp")
        private String timestamp;
        
        @SerializedName("vehicleCount")
        private int vehicleCount;
        
        @SerializedName("pedestrianCount")
        private int pedestrianCount;
        
        @SerializedName("bicycleCount")
        private int bicycleCount;
        
        @SerializedName("direction")
        private String direction;
        
        @SerializedName("counterType")
        private String counterType;
        
        @SerializedName("technology")
        private String technology;
        
        @SerializedName("averageSpeedKmh")
        private double averageSpeedKmh;
        
        @SerializedName("occupancyPercentage")
        private double occupancyPercentage;
        
        @SerializedName("trafficDensity")
        private String trafficDensity;

        public String getSensorId() { return sensorId; }
        public String getTimestamp() { return timestamp; }
        public int getVehicleCount() { return vehicleCount; }
        public int getPedestrianCount() { return pedestrianCount; }
        public int getBicycleCount() { return bicycleCount; }
        public String getDirection() { return direction; }
        public String getCounterType() { return counterType; }
        public String getTechnology() { return technology; }
        public double getAverageSpeedKmh() { return averageSpeedKmh; }
        public double getOccupancyPercentage() { return occupancyPercentage; }
        public String getTrafficDensity() { return trafficDensity; }
    }

    // Clase para semáforos
    public static class TrafficLightMeasurement {
        @SerializedName("sensorId")
        private String sensorId;
        @SerializedName("timestamp")
        private String timestamp;
        
        @SerializedName("currentState")
        private String currentState;
        
        @SerializedName("cyclePositionSeconds")
        private int cyclePositionSeconds;
        
        @SerializedName("timeRemainingSeconds")
        private int timeRemainingSeconds;
        
        @SerializedName("cycleDurationSeconds")
        private int cycleDurationSeconds;
        
        @SerializedName("trafficLightType")
        private String trafficLightType;
        
        @SerializedName("circulationDirection")
        private String circulationDirection;
        
        @SerializedName("pedestrianWaiting")
        private boolean pedestrianWaiting;
        
        @SerializedName("pedestrianButtonPressed")
        private boolean pedestrianButtonPressed;
        
        @SerializedName("malfunctionDetected")
        private boolean malfunctionDetected;
        
        @SerializedName("cycleCount")
        private int cycleCount;
        
        @SerializedName("stateChanged")
        private boolean stateChanged;
        
        @SerializedName("lastStateChange")
        private String lastStateChange;

        public String getSensorId() { return sensorId; }
        public String getTimestamp() { return timestamp; }
        public String getCurrentState() { return currentState; }
        public int getCyclePositionSeconds() { return cyclePositionSeconds; }
        public int getTimeRemainingSeconds() { return timeRemainingSeconds; }
        public int getCycleDurationSeconds() { return cycleDurationSeconds; }
        public String getTrafficLightType() { return trafficLightType; }
        public String getCirculationDirection() { return circulationDirection; }
        public boolean isPedestrianWaiting() { return pedestrianWaiting; }
        public boolean isPedestrianButtonPressed() { return pedestrianButtonPressed; }
        public boolean isMalfunctionDetected() { return malfunctionDetected; }
        public int getCycleCount() { return cycleCount; }
        public boolean isStateChanged() { return stateChanged; }
        public String getLastStateChange() { return lastStateChange; }
    }

    // Clase para pantallas de información
    public static class DisplayMeasurement {
        @SerializedName("sensorId")
        private String sensorId;
        @SerializedName("timestamp")
        private String timestamp;
        
        @SerializedName("displayStatus")
        private String displayStatus;
        
        @SerializedName("currentMessage")
        private String currentMessage;
        
        @SerializedName("contentType")
        private String contentType;
        
        @SerializedName("brightnessLevel")
        private int brightnessLevel;
        
        @SerializedName("displayType")
        private String displayType;
        
        @SerializedName("displaySizeInches")
        private double displaySizeInches;
        
        @SerializedName("supportsColor")
        private boolean supportsColor;
        
        @SerializedName("temperatureCelsius")
        private double temperatureCelsius;
        
        @SerializedName("energyConsumptionWatts")
        private double energyConsumptionWatts;
        
        @SerializedName("lastContentUpdate")
        private String lastContentUpdate;

        public String getSensorId() { return sensorId; }
        public String getTimestamp() { return timestamp; }
        public String getDisplayStatus() { return displayStatus; }
        public String getCurrentMessage() { return currentMessage; }
        public String getContentType() { return contentType; }
        public int getBrightnessLevel() { return brightnessLevel; }
        public String getDisplayType() { return displayType; }
        public double getDisplaySizeInches() { return displaySizeInches; }
        public boolean isSupportsColor() { return supportsColor; }
        public double getTemperatureCelsius() { return temperatureCelsius; }
        public double getEnergyConsumptionWatts() { return energyConsumptionWatts; }
        public String getLastContentUpdate() { return lastContentUpdate; }
    }
}
