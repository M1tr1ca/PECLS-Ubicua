package logic;

import java.sql.Timestamp;

public class TrafficLightMeasurement 
{
    private String sensorId;
    private Timestamp timestamp;
    private String currentState;
    private int cyclePositionSeconds;
    private int timeRemainingSeconds;
    private int cycleDurationSeconds;
    private String trafficLightType;
    private String circulationDirection;
    private boolean pedestrianWaiting;
    private boolean pedestrianButtonPressed;
    private boolean malfunctionDetected;
    private int cycleCount;
    private boolean stateChanged;
    private Timestamp lastStateChange;
 
    public TrafficLightMeasurement() 
    {
        this.sensorId = null;
        this.timestamp = null;
        this.currentState = null;
        this.cyclePositionSeconds = 0;
        this.timeRemainingSeconds = 0;
        this.cycleDurationSeconds = 0;
        this.trafficLightType = null;
        this.circulationDirection = null;
        this.pedestrianWaiting = false;
        this.pedestrianButtonPressed = false;
        this.malfunctionDetected = false;
        this.cycleCount = 0;
        this.stateChanged = false;
        this.lastStateChange = null;
    }

    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public String getCurrentState() { return currentState; }
    public void setCurrentState(String currentState) { this.currentState = currentState; }

    public int getCyclePositionSeconds() { return cyclePositionSeconds; }
    public void setCyclePositionSeconds(int cyclePositionSeconds) { this.cyclePositionSeconds = cyclePositionSeconds; }

    public int getTimeRemainingSeconds() { return timeRemainingSeconds; }
    public void setTimeRemainingSeconds(int timeRemainingSeconds) { this.timeRemainingSeconds = timeRemainingSeconds; }

    public int getCycleDurationSeconds() { return cycleDurationSeconds; }
    public void setCycleDurationSeconds(int cycleDurationSeconds) { this.cycleDurationSeconds = cycleDurationSeconds; }

    public String getTrafficLightType() { return trafficLightType; }
    public void setTrafficLightType(String trafficLightType) { this.trafficLightType = trafficLightType; }

    public String getCirculationDirection() { return circulationDirection; }
    public void setCirculationDirection(String circulationDirection) { this.circulationDirection = circulationDirection; }

    public boolean isPedestrianWaiting() { return pedestrianWaiting; }
    public void setPedestrianWaiting(boolean pedestrianWaiting) { this.pedestrianWaiting = pedestrianWaiting; }

    public boolean isPedestrianButtonPressed() { return pedestrianButtonPressed; }
    public void setPedestrianButtonPressed(boolean pedestrianButtonPressed) { this.pedestrianButtonPressed = pedestrianButtonPressed; }

    public boolean isMalfunctionDetected() { return malfunctionDetected; }
    public void setMalfunctionDetected(boolean malfunctionDetected) { this.malfunctionDetected = malfunctionDetected; }

    public int getCycleCount() { return cycleCount; }
    public void setCycleCount(int cycleCount) { this.cycleCount = cycleCount; }

    public boolean isStateChanged() { return stateChanged; }
    public void setStateChanged(boolean stateChanged) { this.stateChanged = stateChanged; }

    public Timestamp getLastStateChange() { return lastStateChange; }
    public void setLastStateChange(Timestamp lastStateChange) { this.lastStateChange = lastStateChange; }
}
