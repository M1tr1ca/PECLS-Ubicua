package servlets;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import logic.Log;
import logic.Logic;
import logic.Measurement;

/**
 * Servlet implementation class GetData
 */
@WebServlet("/GetData")
public class GetData extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetData() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Log.log.info("--Obteniendo datos de sensores--");
		response.setContentType("application/json;charset=UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		PrintWriter out = response.getWriter();
		try 
		{
			// Obtener parámetros opcionales
			String sensorId = request.getParameter("sensor_id");
			String limitStr = request.getParameter("limit");
			
			int limit = 100; // Por defecto 100 lecturas
			if (limitStr != null) {
				try {
					limit = Integer.parseInt(limitStr);
				} catch (NumberFormatException e) {
					Log.log.warn("Límite inválido, usando valor por defecto: 100");
				}
			}
			
			java.util.List<logic.SensorReading> readings;
			
			if (sensorId != null && !sensorId.isEmpty()) {
				// Obtener lecturas de un sensor específico
				Log.log.info("Obteniendo lecturas del sensor: {} (límite: {})", sensorId, limit);
				readings = logic.SensorLogic.getLatestReadingsBySensor(sensorId, limit);
			} else {
				// Obtener las últimas lecturas de todos los sensores
				Log.log.info("Obteniendo últimas {} lecturas de todos los sensores", limit);
				readings = logic.SensorLogic.getLatestReadings(limit);
			}
			
			String jsonReadings = new Gson().toJson(readings);
			Log.log.info("Devolviendo {} lecturas", readings.size());
			out.println(jsonReadings);
			
		} catch (NumberFormatException nfe) 
		{
			out.println("{\"error\":\"Formato de número inválido\"}");
			Log.log.error("Number Format Exception: " + nfe);
		} catch (IndexOutOfBoundsException iobe) 
		{
			out.println("{\"error\":\"Índice fuera de límites\"}");
			Log.log.error("Index out of bounds Exception: " + iobe);
		} catch (Exception e) 
		{
			out.println("{\"error\":\"Error interno del servidor\"}");
			Log.log.error("Exception: " + e);
			e.printStackTrace();
		} finally 
		{
			out.close();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
