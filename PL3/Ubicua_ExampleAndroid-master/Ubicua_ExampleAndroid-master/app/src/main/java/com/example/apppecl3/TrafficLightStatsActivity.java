package com.example.apppecl3;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
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

        // Obtener datos del intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            sensorId = extras.getString("sensor_id", "LAB08JAV-G1-TL");
            streetId = extras.getString("street_id", "ST_1");
            sensorType = extras.getString("sensor_type", "traffic_light");
            streetName = extras.getString("street_name", "Calle desconocida");
            
            tvStreetName.setText("📍 " + streetName);
            
            // Formato: sensors/{street_id}/{sensor_type}/{sensor_id}
            String topic = "/sensors/" + streetId + "/traffic_light/" + sensorId;
            conectarMqtt(topic);
        }
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
            JSONObject obj = new JSONObject(json);
            
            String state = "--";
            int timeRemaining = 0, cycleDuration = 0;
            boolean pedestrianWaiting = false, malfunction = false;
            String direction = "--";
            
            // Formato ESP32: {"data":{"state":"green","remaining_seconds":25,...}}
            if (obj.has("data")) {
                JSONObject data = obj.getJSONObject("data");
                state = data.optString("state", data.optString("currentState", "--"));
                timeRemaining = data.optInt("remaining_seconds", data.optInt("timeRemainingSeconds", 0));
                cycleDuration = data.optInt("cycle_duration_seconds", data.optInt("cycleDurationSeconds", 0));
                pedestrianWaiting = data.optBoolean("pedestrian_waiting", data.optBoolean("pedestrianWaiting", false));
                malfunction = data.optBoolean("malfunction_detected", data.optBoolean("malfunctionDetected", false));
                direction = data.optString("circulation_direction", data.optString("circulationDirection", "--"));
            } else {
                // Fallback formato plano
                state = obj.optString("currentState", "--");
                timeRemaining = obj.optInt("timeRemainingSeconds", 0);
                cycleDuration = obj.optInt("cycleDurationSeconds", 0);
                pedestrianWaiting = obj.optBoolean("pedestrianWaiting", false);
                malfunction = obj.optBoolean("malfunctionDetected", false);
                direction = obj.optString("circulationDirection", "--");
            }
            
            if (!state.equals("--")) {
                
                // Actualizar UI
                tvCurrentState.setText(state.toUpperCase());
                tvTimeRemaining.setText(String.valueOf(timeRemaining));
                tvCycleDuration.setText(String.valueOf(cycleDuration));
                tvPedestrianWaiting.setText(pedestrianWaiting ? "Sí 🚶" : "No");
                tvMalfunction.setText(malfunction ? "⚠️ Sí" : "✅ No");
                tvCirculationDirection.setText("Dirección: " + direction);
                
                // Actualizar semáforo visual
                actualizarSemaforo(state);
                
                // Agregar al historial
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                String hora = sdf.format(new Date());
                
                String entrada = String.format("[%s] Estado: %s | Restante: %ds\n", 
                        hora, state.toUpperCase(), timeRemaining);
                historial.insert(0, entrada);
                
                if (historial.length() > 500) {
                    historial.setLength(500);
                }
                
                tvHistory.setText(historial.toString());
            }
            
        } catch (Exception e) {
            Log.e("ubicua", "Error parsing JSON: " + e.getMessage());
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
