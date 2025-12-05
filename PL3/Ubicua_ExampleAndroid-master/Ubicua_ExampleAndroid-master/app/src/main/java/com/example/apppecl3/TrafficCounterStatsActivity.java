package com.example.apppecl3;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TrafficCounterStatsActivity extends AppCompatActivity {
    private String sensorId;
    private String streetId;
    private String sensorType;
    private String streetName;
    private MqttClient client;
    private Handler mainHandler;
    
    private TextView tvStreetName;
    private TextView tvStatus;
    private TextView tvVehicleCount;
    private TextView tvPedestrianCount;
    private TextView tvBicycleCount;
    private TextView tvAverageSpeed;
    private TextView tvTrafficDensity;
    private TextView tvDirection;
    private TextView tvHistory;
    
    private StringBuilder historial = new StringBuilder();
    private int totalVehicles = 0;
    private int totalPedestrians = 0;
    private int totalBicycles = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_traffic_counter_stats);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        tvStreetName = findViewById(R.id.tvStreetName);
        tvStatus = findViewById(R.id.tvStatus);
        tvVehicleCount = findViewById(R.id.tvVehicleCount);
        tvPedestrianCount = findViewById(R.id.tvPedestrianCount);
        tvBicycleCount = findViewById(R.id.tvBicycleCount);
        tvAverageSpeed = findViewById(R.id.tvAverageSpeed);
        tvTrafficDensity = findViewById(R.id.tvTrafficDensity);
        tvDirection = findViewById(R.id.tvDirection);
        tvHistory = findViewById(R.id.tvHistory);
        mainHandler = new Handler(Looper.getMainLooper());

        // Botón volver
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Obtener datos del intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            sensorId = extras.getString("sensor_id", "LAB08JAV-G1-TC");
            streetId = extras.getString("street_id", "ST_1");
            sensorType = extras.getString("sensor_type", "traffic_counter");
            streetName = extras.getString("street_name", "Calle desconocida");
            
            tvStreetName.setText("📍 " + streetName);
            
            Log.i("ubicua", "TrafficCounter - SensorId recibido: " + sensorId);
            Log.i("ubicua", "TrafficCounter - StreetId: " + streetId);
            
            // Cargar datos históricos del servidor
            cargarDatosHistoricos();
            
            // Formato: sensors/{street_id}/{sensor_type}/{sensor_id}
            String topic = "/sensors/" + streetId + "/traffic_counter/" + sensorId;
            Log.i("ubicua", "TrafficCounter - Topic MQTT: " + topic);
            conectarMqtt(topic);
        }
    }
    
    private void cargarDatosHistoricos() {
        tvStatus.setText("🔄 Cargando datos...");
        
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        retrofit2.Call<AllDataResponse> call = apiService.getAllData();
        
        call.enqueue(new retrofit2.Callback<AllDataResponse>() {
            @Override
            public void onResponse(retrofit2.Call<AllDataResponse> call, retrofit2.Response<AllDataResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AllDataResponse data = response.body();
                    procesarDatosHistoricos(data);
                } else {
                    Log.w("ubicua", "No se pudieron cargar datos históricos de contador tráfico");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<AllDataResponse> call, Throwable t) {
                Log.e("ubicua", "Error cargando históricos contador: " + t.getMessage());
            }
        });
    }
    
    private void procesarDatosHistoricos(AllDataResponse data) {
        if (data.getTrafficCounter() == null || data.getTrafficCounter().isEmpty()) {
            Log.w("ubicua", "No hay datos de contador de tráfico en la respuesta");
            return;
        }
        
        java.util.List<AllDataResponse.TrafficCounterMeasurement> trafficList = data.getTrafficCounter();
        AllDataResponse.TrafficCounterMeasurement ultimoDato = null;
        
        Log.i("ubicua", "Procesando " + trafficList.size() + " registros trafficCounter, filtrando por sensorId: " + sensorId);
        
        // Filtrar por sensor ID y sumar totales
        for (AllDataResponse.TrafficCounterMeasurement m : trafficList) {
            if (m.getSensorId() != null && m.getSensorId().equals(sensorId)) {
                ultimoDato = m;
                totalVehicles += m.getVehicleCount();
                totalPedestrians += m.getPedestrianCount();
                totalBicycles += m.getBicycleCount();
            }
        }
        
        final AllDataResponse.TrafficCounterMeasurement datoFinal = ultimoDato;
        
        mainHandler.post(() -> {
            if (datoFinal != null) {
                // Mostrar totales y último dato
                tvVehicleCount.setText(String.valueOf(totalVehicles));
                tvPedestrianCount.setText(String.valueOf(totalPedestrians));
                tvBicycleCount.setText(String.valueOf(totalBicycles));
                tvAverageSpeed.setText(String.format(java.util.Locale.US, "%.1f km/h", datoFinal.getAverageSpeedKmh()));
                tvTrafficDensity.setText(datoFinal.getTrafficDensity() != null ? datoFinal.getTrafficDensity() : "--");
                tvDirection.setText(datoFinal.getDirection() != null ? datoFinal.getDirection() : "--");
                
                Log.i("ubicua", "Mostrando datos contador: V=" + totalVehicles + ", P=" + totalPedestrians);
            }
        });
    }

    private void conectarMqtt(String topic) {
        new Thread(() -> {
            try {
                client = new MqttClient(
                        "tcp://10.0.2.2:3000",
                        MqttClient.generateClientId(),
                        new MemoryPersistence()
                );

                MqttConnectOptions options = new MqttConnectOptions();
                options.setCleanSession(true);
                options.setConnectionTimeout(10);
                options.setKeepAliveInterval(60);
                options.setAutomaticReconnect(true);

                client.connect(options);
                mainHandler.post(() -> tvStatus.setText("✅ Conectado | Topic: " + topic));

                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        Log.e("ubicua", "TrafficCounter - Conexión perdida: " + (cause != null ? cause.getMessage() : "unknown"));
                        mainHandler.post(() -> tvStatus.setText("❌ Conexión perdida"));
                    }

                    @Override
                    public void messageArrived(String receivedTopic, MqttMessage message) {
                        String msg = new String(message.getPayload());
                        Log.i("ubicua", "*** TrafficCounter MENSAJE RECIBIDO ***");
                        Log.i("ubicua", "Topic: " + receivedTopic);
                        Log.i("ubicua", "Payload: " + msg);
                        mainHandler.post(() -> procesarMensaje(msg));
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {}
                });

                client.subscribe(topic, 1);
                Log.i("ubicua", "*** TrafficCounter SUSCRITO a: " + topic + " ***");
                
            } catch (MqttException e) {
                Log.e("ubicua", "Error MQTT: " + e.getMessage());
                mainHandler.post(() -> tvStatus.setText("❌ Error: " + e.getMessage()));
            }
        }).start();
    }

    private void procesarMensaje(String json) {
        try {
            Log.i("ubicua", "TrafficCounter - Procesando JSON: " + json);
            JSONObject obj = new JSONObject(json);
            
            int vehicles = 0, pedestrians = 0, bicycles = 0;
            double speed = 0;
            String density = "--", direction = "--";
            
            // Formato ESP32: {"data":{"vehicle_count":X,"pedestrian_count":Y,...}}
            if (obj.has("data")) {
                JSONObject data = obj.getJSONObject("data");
                vehicles = data.optInt("vehicle_count", data.optInt("vehicleCount", 0));
                pedestrians = data.optInt("pedestrian_count", data.optInt("pedestrianCount", 0));
                bicycles = data.optInt("bicycle_count", data.optInt("bicycleCount", 0));
                speed = data.optDouble("average_speed_kmh", data.optDouble("averageSpeedKmh", 0));
                density = data.optString("traffic_density", data.optString("trafficDensity", "--"));
                direction = data.optString("direction", "--");
                Log.i("ubicua", "TrafficCounter - Parseado de data: V=" + vehicles + ", P=" + pedestrians + ", B=" + bicycles);
            } else {
                // Fallback formato plano
                vehicles = obj.optInt("vehicleCount", obj.optInt("vehicle_count", 0));
                pedestrians = obj.optInt("pedestrianCount", obj.optInt("pedestrian_count", 0));
                bicycles = obj.optInt("bicycleCount", obj.optInt("bicycle_count", 0));
                speed = obj.optDouble("averageSpeedKmh", obj.optDouble("average_speed_kmh", 0));
                density = obj.optString("trafficDensity", obj.optString("traffic_density", "--"));
                direction = obj.optString("direction", "--");
                Log.i("ubicua", "TrafficCounter - Parseado plano: V=" + vehicles + ", P=" + pedestrians + ", B=" + bicycles);
            }
            
            // Actualizar UI aunque sean 0 (para mostrar que llegó el mensaje)
            // Acumular conteos
            totalVehicles += vehicles;
            totalPedestrians += pedestrians;
            totalBicycles += bicycles;
            
            // Actualizar UI
            tvVehicleCount.setText(String.valueOf(totalVehicles));
            tvPedestrianCount.setText(String.valueOf(totalPedestrians));
            tvBicycleCount.setText(String.valueOf(totalBicycles));
            tvAverageSpeed.setText(String.format("%.0f", speed));
            tvTrafficDensity.setText(density);
            tvDirection.setText("Dirección: " + direction);
            
            // Agregar al historial
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            String hora = sdf.format(new Date());
            
            String entrada = String.format("[%s] 🚗%d 🚶%d 🚲%d | %s\n", 
                    hora, vehicles, pedestrians, bicycles, density);
            historial.insert(0, entrada);
            
            if (historial.length() > 500) {
                historial.setLength(500);
            }
            
            tvHistory.setText(historial.toString());
            Log.i("ubicua", "TrafficCounter - UI actualizada correctamente");
            
        } catch (Exception e) {
            Log.e("ubicua", "TrafficCounter - Error parsing JSON: " + e.getMessage() + " - JSON: " + json);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        desconectarMqtt();
    }

    private void desconectarMqtt() {
        new Thread(() -> {
            try {
                if (client != null && client.isConnected()) {
                    client.disconnect();
                    client.close();
                }
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
