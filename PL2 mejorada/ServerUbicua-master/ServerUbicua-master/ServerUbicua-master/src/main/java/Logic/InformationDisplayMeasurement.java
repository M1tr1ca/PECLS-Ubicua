package logic;

import java.sql.Timestamp;

public class InformationDisplayMeasurement 
{
    private String sensorId;
    private Timestamp timestamp;
    private String displayStatus;
    private String currentMessage;
    private String contentType;
    private int brightnessLevel;
    private String displayType;
    private double displaySizeInches;
    private boolean supportsColor;
    private double temperatureCelsius;
    private double energyConsumptionWatts;
    private Timestamp lastContentUpdate;
 
    public InformationDisplayMeasurement() 
    {
        this.sensorId = null;
        this.timestamp = null;
        this.displayStatus = null;
        this.currentMessage = null;
        this.contentType = null;
        this.brightnessLevel = 0;
        this.displayType = null;
        this.displaySizeInches = 0;
        this.supportsColor = false;
        this.temperatureCelsius = 0;
        this.energyConsumptionWatts = 0;
        this.lastContentUpdate = null;
    }

    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public String getDisplayStatus() { return displayStatus; }
    public void setDisplayStatus(String displayStatus) { this.displayStatus = displayStatus; }

    public String getCurrentMessage() { return currentMessage; }
    public void setCurrentMessage(String currentMessage) { this.currentMessage = currentMessage; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public int getBrightnessLevel() { return brightnessLevel; }
    public void setBrightnessLevel(int brightnessLevel) { this.brightnessLevel = brightnessLevel; }

    public String getDisplayType() { return displayType; }
    public void setDisplayType(String displayType) { this.displayType = displayType; }

    public double getDisplaySizeInches() { return displaySizeInches; }
    public void setDisplaySizeInches(double displaySizeInches) { this.displaySizeInches = displaySizeInches; }

    public boolean isSupportsColor() { return supportsColor; }
    public void setSupportsColor(boolean supportsColor) { this.supportsColor = supportsColor; }

    public double getTemperatureCelsius() { return temperatureCelsius; }
    public void setTemperatureCelsius(double temperatureCelsius) { this.temperatureCelsius = temperatureCelsius; }

    public double getEnergyConsumptionWatts() { return energyConsumptionWatts; }
    public void setEnergyConsumptionWatts(double energyConsumptionWatts) { this.energyConsumptionWatts = energyConsumptionWatts; }

    public Timestamp getLastContentUpdate() { return lastContentUpdate; }
    public void setLastContentUpdate(Timestamp lastContentUpdate) { this.lastContentUpdate = lastContentUpdate; }
}
