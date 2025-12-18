package com.example.apppecl3;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
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

public class TrafficLightStatsActivity extends AppCompatActivity {
    private String sensorId;
    private String streetId;
    private String sensorType;
    private String streetName;
    private MqttClient client;
    private Handler mainHandler;
    
    private TextView tvStreetName;
    private TextView tvStatus;
    private TextView tvCurrentState;
    private TextView tvTimeRemaining;
    private TextView tvCycleDuration;
    private TextView tvPedestrianWaiting;
    private TextView tvMalfunction;
    private TextView tvCirculationDirection;
    private TextView tvHistory;
    
    private View lightRed;
    private View lightYellow;
    private View lightGreen;
    
    private StringBuilder historial = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_traffic_light_stats);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        tvStreetName = findViewById(R.id.tvStreetName);
        tvStatus = findViewById(R.id.tvStatus);
        tvCurrentState = findViewById(R.id.tvCurrentState);
        tvTimeRemaining = findViewById(R.id.tvTimeRemaining);
        tvCycleDuration = findViewById(R.id.tvCycleDuration);
        tvPedestrianWaiting = findViewById(R.id.tvPedestrianWaiting);
        tvMalfunction = findViewById(R.id.tvMalfunction);
        tvCirculationDirection = findViewById(R.id.tvCirculationDirection);
        tvHistory = findViewById(R.id.tvHistory);
        
        lightRed = findViewById(R.id.lightRed);
        lightYellow = findViewById(R.id.lightYellow);
        lightGreen = findViewById(R.id.lightGreen);
        
        mainHandler = new Handler(Looper.getMainLooper());

        // Botón volver
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Obtener datos del intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            sensorId = extras.getString("sensor_id", "LAB08JAV-G1");
            streetId = extras.getString("street_id", "ST_1");
            sensorType = extras.getString("sensor_type", "traffic_light");
            streetName = extras.getString("street_name", "Calle desconocida");
            
            tvStreetName.setText("📍 " + streetName);
            
            // Cargar datos históricos del servidor
            cargarDatosHistoricos();
            
            // Formato: sensors/{street_id}/{sensor_type}/{sensor_id}
            String topic = "sensors/" + streetId + "/traffic_light/" + "#";
            Log.i("ubicua", "TrafficLight - SensorId: " + sensorId + ", StreetId: " + streetId);
            Log.i("ubicua", "TrafficLight - Suscribiendo a topic: " + topic);
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
                    Log.w("ubicua", "No se pudieron cargar datos históricos de semáforos");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<AllDataResponse> call, Throwable t) {
                Log.e("ubicua", "Error cargando históricos semáforos: " + t.getMessage());
            }
        });
    }
    
    private void procesarDatosHistoricos(AllDataResponse data) {
        if (data.getTrafficLight() == null || data.getTrafficLight().isEmpty()) {
            Log.w("ubicua", "No hay datos de semáforos en la respuesta");
            return;
        }
        
        java.util.List<AllDataResponse.TrafficLightMeasurement> trafficLightList = data.getTrafficLight();
        AllDataResponse.TrafficLightMeasurement ultimoDato = null;
        
        Log.i("ubicua", "Procesando " + trafficLightList.size() + " registros trafficLight");
        

        for (AllDataResponse.TrafficLightMeasurement m : trafficLightList) {
                if(m.getSensorId().equals(sensorId)){
                    ultimoDato = m;
                }


        }
        
        final AllDataResponse.TrafficLightMeasurement datoFinal = ultimoDato;
        
        mainHandler.post(() -> {
            if (datoFinal != null) {
                // Mostrar el último dato
                String state = datoFinal.getCurrentState();
                tvCurrentState.setText(state != null ? state.toUpperCase() : "--");
                tvTimeRemaining.setText(datoFinal.getTimeRemainingSeconds() + " s");
                tvCycleDuration.setText(datoFinal.getCycleDurationSeconds() + " s");
                tvPedestrianWaiting.setText(datoFinal.isPedestrianWaiting() ? "Sí" : "No");
                tvMalfunction.setText(datoFinal.isMalfunctionDetected() ? "⚠️ Detectada" : "✅ Normal");
                tvCirculationDirection.setText(datoFinal.getCirculationDirection() != null ? 
                        datoFinal.getCirculationDirection() : "--");
                
                // Actualizar luces
                actualizarSemaforo(state);
                
                Log.i("ubicua", "Mostrando último dato semáforo: estado=" + state);
            }
        });
    }

    private void conectarMqtt(String topic) {
        new Thread(() -> {
            try {
                client = new MqttClient(
                        "tcp://172.20.10.9:3000",
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
                        mainHandler.post(() -> tvStatus.setText("❌ Conexión perdida"));
                    }

                    @Override
                    public void messageArrived(String receivedTopic, MqttMessage message) {
                        String msg = new String(message.getPayload());
                        mainHandler.post(() -> procesarMensaje(msg));
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {}
                });

                client.subscribe(topic, 1);
                Log.i("ubicua", "Suscrito al topic: " + topic);
                
            } catch (MqttException e) {
                Log.e("ubicua", "Error MQTT: " + e.getMessage());
                mainHandler.post(() -> tvStatus.setText("❌ Error: " + e.getMessage()));
            }
        }).start();
    }

    private void procesarMensaje(String json) {
        try {
            Log.i("ubicua", "TrafficLight mensaje recibido: " + json);
            JSONObject obj = new JSONObject(json);
            
            String state = "--";
            int timeRemaining = 0, cycleDuration = 0;
            boolean pedestrianWaiting = false, malfunction = false;
            String direction = "--";
            
            // Formato ESP32: {"data":{"current_state":"red","time_remaining_seconds":60,...}}
            if (obj.has("data")) {
                JSONObject data = obj.getJSONObject("data");
                // Soportar snake_case y camelCase
                state = data.optString("current_state", 
                        data.optString("state", 
                        data.optString("currentState", "--")));
                timeRemaining = data.optInt("time_remaining_seconds", 
                        data.optInt("remaining_seconds", 
                        data.optInt("timeRemainingSeconds", 0)));
                cycleDuration = data.optInt("cycle_duration_seconds", 
                        data.optInt("cycleDurationSeconds", 0));
                pedestrianWaiting = data.optBoolean("pedestrian_waiting", 
                        data.optBoolean("pedestrianWaiting", false));
                malfunction = data.optBoolean("malfunction_detected", 
                        data.optBoolean("malfunctionDetected", false));
                direction = data.optString("circulation_direction", 
                        data.optString("circulationDirection", "--"));
                
                Log.i("ubicua", "Datos parseados - State: " + state + ", TimeRemaining: " + timeRemaining);
            } else {
                // Fallback formato plano
                state = obj.optString("current_state", 
                        obj.optString("currentState", "--"));
                timeRemaining = obj.optInt("time_remaining_seconds", 
                        obj.optInt("timeRemainingSeconds", 0));
                cycleDuration = obj.optInt("cycle_duration_seconds", 
                        obj.optInt("cycleDurationSeconds", 0));
                pedestrianWaiting = obj.optBoolean("pedestrian_waiting", 
                        obj.optBoolean("pedestrianWaiting", false));
                malfunction = obj.optBoolean("malfunction_detected", 
                        obj.optBoolean("malfunctionDetected", false));
                direction = obj.optString("circulation_direction", 
                        obj.optString("circulationDirection", "--"));
            }
            
            // Actualizar UI siempre que llegue un mensaje
            final String finalState = state;
            final int finalTimeRemaining = timeRemaining;
            final int finalCycleDuration = cycleDuration;
            final boolean finalPedestrianWaiting = pedestrianWaiting;
            final boolean finalMalfunction = malfunction;
            final String finalDirection = direction;
            
            // Actualizar UI
            tvCurrentState.setText(finalState.toUpperCase());
            tvTimeRemaining.setText(String.valueOf(finalTimeRemaining));
            tvCycleDuration.setText(String.valueOf(finalCycleDuration));
            tvPedestrianWaiting.setText(finalPedestrianWaiting ? "Sí 🚶" : "No");
            tvMalfunction.setText(finalMalfunction ? "⚠️ Sí" : "✅ No");
            tvCirculationDirection.setText("Dirección: " + finalDirection);
            
            // Actualizar semáforo visual
            actualizarSemaforo(finalState);
            
            // Agregar al historial
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            String hora = sdf.format(new Date());
            
            String entrada = String.format("[%s] Estado: %s | Restante: %ds\n", 
                    hora, finalState.toUpperCase(), finalTimeRemaining);
            historial.insert(0, entrada);
            
            if (historial.length() > 500) {
                historial.setLength(500);
            }
            
            tvHistory.setText(historial.toString());
            Log.i("ubicua", "UI TrafficLight actualizada");
            
        } catch (Exception e) {
            Log.e("ubicua", "Error parsing JSON TrafficLight: " + e.getMessage() + " - JSON: " + json);
        }
    }

    private void actualizarSemaforo(String state) {
        // Resetear todas las luces
        lightRed.setBackgroundResource(R.drawable.traffic_light_off);
        lightYellow.setBackgroundResource(R.drawable.traffic_light_off);
        lightGreen.setBackgroundResource(R.drawable.traffic_light_off);
        
        // Encender la luz correspondiente
        switch (state.toLowerCase()) {
            case "red":
            case "rojo":
                lightRed.setBackgroundResource(R.drawable.traffic_light_red);
                break;
            case "yellow":
            case "amber":
            case "amarillo":
                lightYellow.setBackgroundResource(R.drawable.traffic_light_yellow);
                break;
            case "green":
            case "verde":
                lightGreen.setBackgroundResource(R.drawable.traffic_light_green);
                break;
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
