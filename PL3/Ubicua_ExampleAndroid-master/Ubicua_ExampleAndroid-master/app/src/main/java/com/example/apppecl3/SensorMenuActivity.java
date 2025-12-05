package com.example.apppecl3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
    
    // IDs de sensores para cada tipo
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
            
            sensorWeather = extras.getString("sensor_weather", streetId.replace("ST_", "LAB08JAV-G"));
            sensorTrafficCounter = extras.getString("sensor_traffic_counter", sensorWeather + "-TC");
            sensorTrafficLight = extras.getString("sensor_traffic_light", sensorWeather + "-TL");
            sensorDisplay = extras.getString("sensor_display", sensorWeather + "-DP");
        }

        // Actualizar UI
        TextView tvStreetName = findViewById(R.id.tvStreetName);
        TextView tvStreetDistrict = findViewById(R.id.tvStreetDistrict);
        TextView tvCoordinates = findViewById(R.id.tvCoordinates);

        tvStreetName.setText("📍 " + streetName);
        tvStreetDistrict.setText(streetDistrict + " - " + streetNeighborhood);
        tvCoordinates.setText(String.format("📌 Lat: %.4f, Lon: %.4f", latitude, longitude));

        // Configurar botones del menú
        LinearLayout btnWeather = findViewById(R.id.btnWeather);
        LinearLayout btnTrafficCounter = findViewById(R.id.btnTrafficCounter);
        LinearLayout btnTrafficLight = findViewById(R.id.btnTrafficLight);
        LinearLayout btnDisplay = findViewById(R.id.btnDisplay);

        btnWeather.setOnClickListener(v -> abrirEstadisticas(WeatherStatsActivity.class, sensorWeather, "weather"));
        btnTrafficCounter.setOnClickListener(v -> abrirEstadisticas(TrafficCounterStatsActivity.class, sensorTrafficCounter, "trafficCounter"));
        btnTrafficLight.setOnClickListener(v -> abrirEstadisticas(TrafficLightStatsActivity.class, sensorTrafficLight, "trafficLight"));
        btnDisplay.setOnClickListener(v -> abrirEstadisticas(DisplayStatsActivity.class, sensorDisplay, "display"));
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
