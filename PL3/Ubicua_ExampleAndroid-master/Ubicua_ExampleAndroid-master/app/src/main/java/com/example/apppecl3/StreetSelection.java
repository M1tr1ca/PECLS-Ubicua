package com.example.apppecl3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StreetSelection extends AppCompatActivity {
    Spinner spinner;
    Button boton;
    TextView tvStatus;
    ProgressBar progressBar;
    List<Sensor> listaSensores = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_street_selection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        spinner = findViewById(R.id.spinner);
        boton = findViewById(R.id.button);
        tvStatus = findViewById(R.id.tvStatus);
        progressBar = findViewById(R.id.progressBarSelection);
        
        boton.setEnabled(false);
        obtenerDatosDelServidor();
    }

    private void obtenerDatosDelServidor() {
        tvStatus.setText("🔄 Conectando al servidor...");
        progressBar.setVisibility(View.VISIBLE);
        
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<AllDataResponse> call = apiService.getAllData();
        
        call.enqueue(new Callback<AllDataResponse>() {
            @Override
            public void onResponse(Call<AllDataResponse> call, Response<AllDataResponse> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    AllDataResponse data = response.body();
                    extraerSensores(data);
                    tvStatus.setText("✅ " + listaSensores.size() + " sensores encontrados");
                } else {
                    tvStatus.setText("⚠️ Sin datos del servidor");
                    Log.e("ubicua", "Error del servidor: " + response.code());
                    // Cargar sensores por defecto
                    cargarSensoresPorDefecto();
                }
            }

            @Override
            public void onFailure(Call<AllDataResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvStatus.setText("❌ Error de conexión");
                Log.e("ubicua", "Error: " + t.getMessage());
                // Cargar sensores por defecto para poder probar
                cargarSensoresPorDefecto();
            }
        });
    }

    private void extraerSensores(AllDataResponse data) {
        Set<String> sensorIds = new HashSet<>();
        
        // Extraer sensores de datos meteorológicos
        if (data.getWeather() != null) {
            for (AllDataResponse.WeatherMeasurement m : data.getWeather()) {
                if (!sensorIds.contains(m.getSensorId())) {
                    sensorIds.add(m.getSensorId());
                    listaSensores.add(new Sensor(m.getSensorId(), "weather"));
                }
            }
        }
        
        // Extraer sensores de contador de tráfico
        if (data.getTrafficCounter() != null) {
            for (AllDataResponse.TrafficCounterMeasurement m : data.getTrafficCounter()) {
                if (!sensorIds.contains(m.getSensorId())) {
                    sensorIds.add(m.getSensorId());
                    listaSensores.add(new Sensor(m.getSensorId(), "trafficCounter"));
                }
            }
        }
        
        // Extraer sensores de semáforo
        if (data.getTrafficLight() != null) {
            for (AllDataResponse.TrafficLightMeasurement m : data.getTrafficLight()) {
                if (!sensorIds.contains(m.getSensorId())) {
                    sensorIds.add(m.getSensorId());
                    listaSensores.add(new Sensor(m.getSensorId(), "trafficLight"));
                }
            }
        }
        
        // Extraer sensores de display
        if (data.getInformationDisplay() != null) {
            for (AllDataResponse.DisplayMeasurement m : data.getInformationDisplay()) {
                if (!sensorIds.contains(m.getSensorId())) {
                    sensorIds.add(m.getSensorId());
                    listaSensores.add(new Sensor(m.getSensorId(), "display"));
                }
            }
        }
        
        if (listaSensores.isEmpty()) {
            cargarSensoresPorDefecto();
        } else {
            mostrarLista();
        }
    }

    private void cargarSensoresPorDefecto() {
        // Sensores por defecto de la base de datos
        listaSensores.clear();
        for (int i = 1; i <= 15; i++) {
            listaSensores.add(new Sensor("LAB08JAV-G" + i, "generic"));
        }
        tvStatus.setText("📋 Usando sensores por defecto");
        mostrarLista();
    }

    private void mostrarLista() {
        ArrayList<String> nombres = new ArrayList<>();
        for (Sensor sensor : listaSensores) {
            nombres.add(sensor.toString());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, 
            android.R.layout.simple_spinner_item, 
            nombres
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        
        boton.setEnabled(true);
        boton.setOnClickListener(v -> {
            int selectedIndex = (int) spinner.getSelectedItemId();
            Sensor selectedSensor = listaSensores.get(selectedIndex);
            
            Log.i("ubicua", "Sensor seleccionado: " + selectedSensor.getSensorId());
            
            Intent intent = new Intent(StreetSelection.this, StreetMonitoring.class);
            intent.putExtra("sensor_id", selectedSensor.getSensorId());
            intent.putExtra("sensor_type", selectedSensor.getSensorType());
            startActivity(intent);
        });
    }
}