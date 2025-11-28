package logic;

// sirve para LEER datos de la BD, NO para guardarlos.
import java.sql.Timestamp;

public class Measurement 
{
    private String sensorId;
    private Timestamp date;
    private double temperature;
    private double humidity;
    private double pressure;
    private double altitude;
 
    // constructors
    public Measurement() 
    {
        this.sensorId = null;
        this.date = null;
        this.temperature = 0;
        this.humidity = 0;
        this.pressure = 0;
        this.altitude = 0;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }
}
