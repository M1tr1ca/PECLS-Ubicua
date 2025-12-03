package logic;

import java.sql.Timestamp;

public class TrafficCounterMeasurement 
{
    private String sensorId;
    private Timestamp timestamp;
    private int vehicleCount;
    private int pedestrianCount;
    private int bicycleCount;
    private String direction;
    private String counterType;
    private String technology;
    private double averageSpeedKmh;
    private double occupancyPercentage;
    private String trafficDensity;
 
    public TrafficCounterMeasurement() 
    {
        this.sensorId = null;
        this.timestamp = null;
        this.vehicleCount = 0;
        this.pedestrianCount = 0;
        this.bicycleCount = 0;
        this.direction = null;
        this.counterType = null;
        this.technology = null;
        this.averageSpeedKmh = 0;
        this.occupancyPercentage = 0;
        this.trafficDensity = null;
    }

    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public int getVehicleCount() { return vehicleCount; }
    public void setVehicleCount(int vehicleCount) { this.vehicleCount = vehicleCount; }

    public int getPedestrianCount() { return pedestrianCount; }
    public void setPedestrianCount(int pedestrianCount) { this.pedestrianCount = pedestrianCount; }

    public int getBicycleCount() { return bicycleCount; }
    public void setBicycleCount(int bicycleCount) { this.bicycleCount = bicycleCount; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getCounterType() { return counterType; }
    public void setCounterType(String counterType) { this.counterType = counterType; }

    public String getTechnology() { return technology; }
    public void setTechnology(String technology) { this.technology = technology; }

    public double getAverageSpeedKmh() { return averageSpeedKmh; }
    public void setAverageSpeedKmh(double averageSpeedKmh) { this.averageSpeedKmh = averageSpeedKmh; }

    public double getOccupancyPercentage() { return occupancyPercentage; }
    public void setOccupancyPercentage(double occupancyPercentage) { this.occupancyPercentage = occupancyPercentage; }

    public String getTrafficDensity() { return trafficDensity; }
    public void setTrafficDensity(String trafficDensity) { this.trafficDensity = trafficDensity; }
}
