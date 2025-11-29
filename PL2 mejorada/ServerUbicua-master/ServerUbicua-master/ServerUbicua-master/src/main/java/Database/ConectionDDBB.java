package Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import logic.Log;


public class ConectionDDBB
{
	public Connection obtainConnection(boolean autoCommit) throws NullPointerException
    {
        Connection con=null;
        int intentos = 5;
        for (int i = 0; i < intentos; i++) 
        {
        	Log.log.info("Attempt " + i + " to connect to the database");
        	try
	          {
	            Context ctx = new InitialContext();
	            // Get the connection factory configured in Tomcat
	            DataSource ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/ubicomp");
	           
	            // Obtiene una conexion, se conecta con PostgreSQL
	            con = ds.getConnection();

				      Calendar calendar = Calendar.getInstance();
				      java.sql.Date date = new java.sql.Date(calendar.getTime().getTime());
	            Log.log.debug("Connection creation. Bd connection identifier: " + con.toString() + " obtained in " + date.toString());
	            con.setAutoCommit(autoCommit);
	        	  Log.log.info("Conection obtained in the attempt: " + i);
	            i = intentos;
	          } catch (NamingException ex)
	          {
	            Log.log.error("Error getting connection while trying: " + i + " = " + ex); 
	          } catch (SQLException ex)
	          {
	            Log.log.error("ERROR sql getting connection while trying: " + i + " = " + ex.getSQLState() + "\n" + ex.toString());
	            throw (new NullPointerException("SQL connection is null"));
	          }
		}        
        return con;
    }
    
    public void closeTransaction(Connection con)
    {
        try
          {
            // Guarda los cambios en la BD
            con.commit();
            Log.log.debug("Transaction closed");
          } catch (SQLException ex)
          {
            Log.log.error("Error closing the transaction: " + ex);
          }
    }
    
    public void cancelTransaction(Connection con)
    {
        try
          {
            // Deshace los cambios en la BD
            con.rollback();
            Log.log.debug("Transaction canceled");
          } catch (SQLException ex)
          {
            Log.log.error("ERROR sql when canceling the transation: " + ex.getSQLState() + "\n"  + ex.toString());
          }
    }

    public void closeConnection(Connection con)
    {
        try
          {
        	Log.log.info("Closing the connection");
            if (null != con)
              {
				        Calendar calendar = Calendar.getInstance();
				        java.sql.Date date = new java.sql.Date(calendar.getTime().getTime());
	              Log.log.debug("Connection closed. Bd connection identifier: " + con.toString() + " obtained in " + date.toString());
                con.close();
              }

        	Log.log.info("The connection has been closed");
          } catch (SQLException e)
          {
        	  Log.log.error("ERROR sql closing the connection: " + e);
        	  e.printStackTrace();
          }
    }
    
    public static PreparedStatement getStatement(Connection con,String sql)
    {
        PreparedStatement ps = null;
        try
          {
            if (con != null)
              {
                ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

              }
          } catch (SQLException ex)
          {
    	        Log.log.warn("ERROR sql creating PreparedStatement: " + ex.toString());
          }

        return ps;
    }    

    public static PreparedStatement GetDataBD(Connection con)
    {
    	return getStatement(con,"SELECT sensor_id, timestamp, temperature_celsius, humidity_percent, atmospheric_pressure_hpa, altitude_meters FROM sensor_readings ORDER BY timestamp DESC LIMIT 50");  	
    }
    
    public static PreparedStatement GetTrafficCounterDataBD(Connection con)
    {
    	return getStatement(con,"SELECT sensor_id, timestamp, vehicle_count, pedestrian_count, bicycle_count, direction, counter_type, technology, average_speed_kmh, occupancy_percentage, traffic_density FROM traffic_counter_readings ORDER BY timestamp DESC LIMIT 50");  	
    }
    
    public static PreparedStatement GetTrafficLightDataBD(Connection con)
    {
    	return getStatement(con,"SELECT sensor_id, timestamp, current_state, cycle_position_seconds, time_remaining_seconds, cycle_duration_seconds, traffic_light_type, circulation_direction, pedestrian_waiting, pedestrian_button_pressed, malfunction_detected, cycle_count, state_changed, last_state_change FROM traffic_light_readings ORDER BY timestamp DESC LIMIT 50");  	
    }
    
    public static PreparedStatement GetInformationDisplayDataBD(Connection con)
    {
    	return getStatement(con,"SELECT sensor_id, timestamp, display_status, current_message, content_type, brightness_level, display_type, display_size_inches, supports_color, temperature_celsius, energy_consumption_watts, last_content_update FROM information_display_readings ORDER BY timestamp DESC LIMIT 50");  	
    }
    
    public static PreparedStatement SetDataBD(Connection con)
    {
    	return getStatement(con,"INSERT INTO sensor_readings (sensor_id, timestamp, temperature_celsius, humidity_percent, atmospheric_pressure_hpa, altitude_meters) VALUES (?,?,?,?,?,?)");  	
    }
    public static PreparedStatement getOther(Connection con){
      return getStatement(con, "SELECT * FROM other ORDER BY timestamp DESC LIMIT 50");
    }
}
