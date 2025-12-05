package com.example.apppecl3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SensorMenuActivity extends AppCompatActivity {
    private String streetId;
    private String streetName;
    private String streetDistrict;
    private String streetNeighborhood;
    private double latitude;
    private double longitude;
    
    // IDs de sensores para cada tipo (obtenidos del intent, no hardcodeados)
    private String sensorWeather;
    private String sensorTrafficCounter;
    private String sensorTrafficLight;
    private String sensorDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sensor_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtener datos del intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            streetId = extras.getString("street_id", "");
            streetName = extras.getString("street_name", "Calle desconocida");
            streetDistrict = extras.getString("street_district", "");
            streetNeighborhood = extras.getString("street_neighborhood", "");
            latitude = extras.getDouble("latitude", 0);
            longitude = extras.getDouble("longitude", 0);
            
            // Obtener los sensor_id directamente del intent (ya vienen del servidor vía MapActivity)
            sensorWeather = extras.getString("sensor_weather", null);
            sensorTrafficCounter = extras.getString("sensor_traffic_counter", null);
            sensorTrafficLight = extras.getString("sensor_traffic_light", null);
            sensorDisplay = extras.getString("sensor_display", null);
            
            Log.i("ubicua", "SensorMenu - Recibido del intent:");
            Log.i("ubicua", "  sensor_weather: " + sensorWeather);
            Log.i("ubicua", "  sensor_traffic_counter: " + sensorTrafficCounter);
            Log.i("ubicua", "  sensor_traffic_light: " + sensorTrafficLight);
            Log.i("ubicua", "  sensor_display: " + sensorDisplay);
            
            // Si no hay sensor específico, usar uno genérico basado en el primero disponible
            String fallbackSensor = sensorWeather != null ? sensorWeather :
                                   sensorTrafficCounter != null ? sensorTrafficCounter :
                                   sensorTrafficLight != null ? sensorTrafficLight :
                                   sensorDisplay != null ? sensorDisplay : "LAB08JAV-G1";
            
            // Usar el fallback si alguno es null
            if (sensorWeather == null) sensorWeather = fallbackSensor;
            if (sensorTrafficCounter == null) sensorTrafficCounter = fallbackSensor;
            if (sensorTrafficLight == null) sensorTrafficLight = fallbackSensor;
            if (sensorDisplay == null) sensorDisplay = fallbackSensor;
            
            Log.i("ubicua", "StreetId: " + streetId);
            Log.i("ubicua", "Sensors - Weather: " + sensorWeather + ", TrafficCounter: " + sensorTrafficCounter + 
                          ", TrafficLight: " + sensorTrafficLight + ", Display: " + sensorDisplay);
        }

        // Actualizar UI
        TextView tvStreetName = findViewById(R.id.tvStreetName);
        TextView tvStreetDistrict = findViewById(R.id.tvStreetDistrict);
        TextView tvCoordinates = findViewById(R.id.tvCoordinates);

        tvStreetName.setText(streetName);
        tvStreetDistrict.setText(streetDistrict + " - " + streetNeighborhood);
        tvCoordinates.setText(String.format("📌 Lat: %.4f, Lon: %.4f", latitude, longitude));

        // Botón de volver
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Configurar botones del menú (CardViews en el layout)
        CardView cardWeather = findViewById(R.id.btnWeather);
        CardView cardTrafficCounter = findViewById(R.id.btnTrafficCounter);
        CardView cardTrafficLight = findViewById(R.id.btnTrafficLight);
        CardView cardDisplay = findViewById(R.id.btnDisplay);

        cardWeather.setOnClickListener(v -> abrirEstadisticas(WeatherStatsActivity.class, sensorWeather, "weather"));
        cardTrafficCounter.setOnClickListener(v -> abrirEstadisticas(TrafficCounterStatsActivity.class, sensorTrafficCounter, "trafficCounter"));
        cardTrafficLight.setOnClickListener(v -> abrirEstadisticas(TrafficLightStatsActivity.class, sensorTrafficLight, "trafficLight"));
        cardDisplay.setOnClickListener(v -> abrirEstadisticas(DisplayStatsActivity.class, sensorDisplay, "display"));
    }

    private void abrirEstadisticas(Class<?> activityClass, String sensorId, String sensorType) {
        Intent intent = new Intent(SensorMenuActivity.this, activityClass);
        intent.putExtra("sensor_id", sensorId);
        intent.putExtra("sensor_type", sensorType);
        intent.putExtra("street_name", streetName);
        intent.putExtra("street_id", streetId);
        startActivity(intent);
    }
}
