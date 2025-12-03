package logic;

import Database.ConectionDDBB;
import java.util.ArrayList;
import java.util.Date;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Logic 
{
	public static ArrayList<Measurement> getDataFromDB()
	{
		ArrayList<Measurement> values = new ArrayList<Measurement>();
		
		ConectionDDBB conector = new ConectionDDBB();
		Connection con = null;
		try
		{
			con = conector.obtainConnection(true);
			Log.log.info("Database Connected");
			
			PreparedStatement ps = ConectionDDBB.GetDataBD(con);
			Log.log.info("Query=>" + ps.toString());
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				Measurement measure = new Measurement();
				measure.setSensorId(rs.getString("sensor_id"));
				measure.setTimestamp(rs.getTimestamp("timestamp"));
				measure.setTemperature(rs.getDouble("temperature_celsius"));
				measure.setHumidity(rs.getDouble("humidity_percent"));
				measure.setPressure(rs.getDouble("atmospheric_pressure_hpa"));
				measure.setAltitude(rs.getDouble("altitude_meters"));
				values.add(measure);
			}	
		} catch (SQLException e)
		{
			Log.log.error("Error: " + e);
			values = new ArrayList<Measurement>();
		} catch (NullPointerException e)
		{
			Log.log.error("Error: " + e);
			values = new ArrayList<Measurement>();
		} catch (Exception e)
		{
			Log.log.error("Error:" + e);
			values = new ArrayList<Measurement>();
		}
		conector.closeConnection(con);
		return values;
	}

	public static ArrayList<TrafficCounterMeasurement> getTrafficCounterDataFromDB()
	{
		ArrayList<TrafficCounterMeasurement> values = new ArrayList<TrafficCounterMeasurement>();
		
		ConectionDDBB conector = new ConectionDDBB();
		Connection con = null;
		try
		{
			con = conector.obtainConnection(true);
			Log.log.info("Database Connected - Traffic Counter");
			
			PreparedStatement ps = ConectionDDBB.GetTrafficCounterDataBD(con);
			Log.log.info("Query=>" + ps.toString());
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				TrafficCounterMeasurement measure = new TrafficCounterMeasurement();
				measure.setSensorId(rs.getString("sensor_id"));
				measure.setTimestamp(rs.getTimestamp("timestamp"));
				measure.setVehicleCount(rs.getInt("vehicle_count"));
				measure.setPedestrianCount(rs.getInt("pedestrian_count"));
				measure.setBicycleCount(rs.getInt("bicycle_count"));
				measure.setDirection(rs.getString("direction"));
				measure.setCounterType(rs.getString("counter_type"));
				measure.setTechnology(rs.getString("technology"));
				measure.setAverageSpeedKmh(rs.getDouble("average_speed_kmh"));
				measure.setOccupancyPercentage(rs.getDouble("occupancy_percentage"));
				measure.setTrafficDensity(rs.getString("traffic_density"));
				values.add(measure);
			}	
		} catch (SQLException e)
		{
			Log.log.error("Error: " + e);
			values = new ArrayList<TrafficCounterMeasurement>();
		} catch (NullPointerException e)
		{
			Log.log.error("Error: " + e);
			values = new ArrayList<TrafficCounterMeasurement>();
		} catch (Exception e)
		{
			Log.log.error("Error:" + e);
			values = new ArrayList<TrafficCounterMeasurement>();
		}
		conector.closeConnection(con);
		return values;
	}

	public static ArrayList<TrafficLightMeasurement> getTrafficLightDataFromDB()
	{
		ArrayList<TrafficLightMeasurement> values = new ArrayList<TrafficLightMeasurement>();
		
		ConectionDDBB conector = new ConectionDDBB();
		Connection con = null;
		try
		{
			con = conector.obtainConnection(true);
			Log.log.info("Database Connected - Traffic Light");
			
			PreparedStatement ps = ConectionDDBB.GetTrafficLightDataBD(con);
			Log.log.info("Query=>" + ps.toString());
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				TrafficLightMeasurement measure = new TrafficLightMeasurement();
				measure.setSensorId(rs.getString("sensor_id"));
				measure.setTimestamp(rs.getTimestamp("timestamp"));
				measure.setCurrentState(rs.getString("current_state"));
				measure.setCyclePositionSeconds(rs.getInt("cycle_position_seconds"));
				measure.setTimeRemainingSeconds(rs.getInt("time_remaining_seconds"));
				measure.setCycleDurationSeconds(rs.getInt("cycle_duration_seconds"));
				measure.setTrafficLightType(rs.getString("traffic_light_type"));
				measure.setCirculationDirection(rs.getString("circulation_direction"));
				measure.setPedestrianWaiting(rs.getBoolean("pedestrian_waiting"));
				measure.setPedestrianButtonPressed(rs.getBoolean("pedestrian_button_pressed"));
				measure.setMalfunctionDetected(rs.getBoolean("malfunction_detected"));
				measure.setCycleCount(rs.getInt("cycle_count"));
				measure.setStateChanged(rs.getBoolean("state_changed"));
				measure.setLastStateChange(rs.getTimestamp("last_state_change"));
				values.add(measure);
			}	
		} catch (SQLException e)
		{
			Log.log.error("Error: " + e);
			values = new ArrayList<TrafficLightMeasurement>();
		} catch (NullPointerException e)
		{
			Log.log.error("Error: " + e);
			values = new ArrayList<TrafficLightMeasurement>();
		} catch (Exception e)
		{
			Log.log.error("Error:" + e);
			values = new ArrayList<TrafficLightMeasurement>();
		}
		conector.closeConnection(con);
		return values;
	}
	public static ArrayList<String> getOtherFromDB(){

		ArrayList<String> values = new ArrayList<>();

		ConectionDDBB conector = new ConectionDDBB();
		Connection con = null;
		try {
			con = conector.obtainConnection(true);
			Log.log.info("Database Connected - Other");
			PreparedStatement ps = ConectionDDBB.getOther(con);
			Log.log.info("Query=>" + ps.toString());
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				values.add(rs.getString("sensor_id") + "," + rs.getString("timestamp") + "," + rs.getString("json"));
			}
		} catch (SQLException e)
		{
			Log.log.error("Error: " + e);
			values = new ArrayList<>();
		} catch (NullPointerException e)
		{
			Log.log.error("Error: " + e);
			values = new ArrayList<>();
		} catch (Exception e)
		{
			Log.log.error("Error:" + e);
			values = new ArrayList<>();
		}
		conector.closeConnection(con);
		return values;
	}
	public static ArrayList<InformationDisplayMeasurement> getInformationDisplayDataFromDB()
	{
		ArrayList<InformationDisplayMeasurement> values = new ArrayList<InformationDisplayMeasurement>();
		
		ConectionDDBB conector = new ConectionDDBB();
		Connection con = null;
		try
		{
			con = conector.obtainConnection(true);
			Log.log.info("Database Connected - Information Display");
			
			PreparedStatement ps = ConectionDDBB.GetInformationDisplayDataBD(con);
			Log.log.info("Query=>" + ps.toString());
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				InformationDisplayMeasurement measure = new InformationDisplayMeasurement();
				measure.setSensorId(rs.getString("sensor_id"));
				measure.setTimestamp(rs.getTimestamp("timestamp"));
				measure.setDisplayStatus(rs.getString("display_status"));
				measure.setCurrentMessage(rs.getString("current_message"));
				measure.setContentType(rs.getString("content_type"));
				measure.setBrightnessLevel(rs.getInt("brightness_level"));
				measure.setDisplayType(rs.getString("display_type"));
				measure.setDisplaySizeInches(rs.getDouble("display_size_inches"));
				measure.setSupportsColor(rs.getBoolean("supports_color"));
				measure.setTemperatureCelsius(rs.getDouble("temperature_celsius"));
				measure.setEnergyConsumptionWatts(rs.getDouble("energy_consumption_watts"));
				measure.setLastContentUpdate(rs.getTimestamp("last_content_update"));
				values.add(measure);
			}	
		} catch (SQLException e)
		{
			Log.log.error("Error: " + e);
			values = new ArrayList<InformationDisplayMeasurement>();
		} catch (NullPointerException e)
		{
			Log.log.error("Error: " + e);
			values = new ArrayList<InformationDisplayMeasurement>();
		} catch (Exception e)
		{
			Log.log.error("Error:" + e);
			values = new ArrayList<InformationDisplayMeasurement>();
		}
		conector.closeConnection(con);
		return values;
	}

	public static ArrayList<Measurement> setDataToDB(int value)
	{
		ArrayList<Measurement> values = new ArrayList<Measurement>();
		
		ConectionDDBB conector = new ConectionDDBB();
		Connection con = null;
		try
		{
			con = conector.obtainConnection(true);
			Log.log.info("Database Connected");

			PreparedStatement ps = ConectionDDBB.SetDataBD(con);
			ps.setInt(1, value);
			ps.setTimestamp(2, new Timestamp((new Date()).getTime()));
			Log.log.info("Query=>" + ps.toString());
			ps.executeUpdate();
		} catch (SQLException e)
		{
			Log.log.error("Error: " + e);
			values = new ArrayList<Measurement>();
		} catch (NullPointerException e)
		{
			Log.log.error("Error: " + e);
			values = new ArrayList<Measurement>();
		} catch (Exception e)
		{
			Log.log.error("Error:" + e);
			values = new ArrayList<Measurement>();
		}
		conector.closeConnection(con);
		return values;
	}
	
	
}
