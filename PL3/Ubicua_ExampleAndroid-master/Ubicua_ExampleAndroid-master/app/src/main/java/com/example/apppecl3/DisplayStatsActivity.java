package com.example.apppecl3;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
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

public class DisplayStatsActivity extends AppCompatActivity {
    private String sensorId;
    private String streetId;
    private String sensorType;
    private String streetName;
    private MqttClient client;
    private Handler mainHandler;
    
    private TextView tvStreetName;
    private TextView tvStatus;
    private TextView tvDisplayStatus;
    private TextView tvCurrentMessage;
    private TextView tvContentType;
    private TextView tvBrightness;
    private TextView tvTemperature;
    private TextView tvEnergyConsumption;
    private TextView tvDisplaySize;
    private TextView tvHistory;
    private ProgressBar progressBrightness;
    private View statusIndicator;
    
    private StringBuilder historial = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_display_stats);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        tvStreetName = findViewById(R.id.tvStreetName);
        tvStatus = findViewById(R.id.tvStatus);
        tvDisplayStatus = findViewById(R.id.tvDisplayStatus);
        tvCurrentMessage = findViewById(R.id.tvCurrentMessage);
        tvContentType = findViewById(R.id.tvContentType);
        tvBrightness = findViewById(R.id.tvBrightness);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvEnergyConsumption = findViewById(R.id.tvEnergyConsumption);
        tvDisplaySize = findViewById(R.id.tvDisplaySize);
        tvHistory = findViewById(R.id.tvHistory);
        progressBrightness = findViewById(R.id.progressBrightness);
        statusIndicator = findViewById(R.id.statusIndicator);
        
        mainHandler = new Handler(Looper.getMainLooper());

        // Obtener datos del intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            sensorId = extras.getString("sensor_id", "LAB08JAV-G1-DP");
            streetId = extras.getString("street_id", "ST_1");
            sensorType = extras.getString("sensor_type", "display");
            streetName = extras.getString("street_name", "Calle desconocida");
            
            tvStreetName.setText("📍 " + streetName);
            
            // Formato: sensors/{street_id}/{sensor_type}/{sensor_id}
            String topic = "sensors/" + streetId + "/display/" + sensorId;
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
            
            String displayStatus = "--", currentMessage = "Sin mensaje", contentType = "--";
            int brightness = 0;
            double temperature = 0, energy = 0, size = 0;
            
            // Formato ESP32: {"data":{"display_status":"active","current_message":"...","brightness_percent":75}}
            if (obj.has("data")) {
                JSONObject data = obj.getJSONObject("data");
                displayStatus = data.optString("display_status", data.optString("displayStatus", "--"));
                currentMessage = data.optString("current_message", data.optString("currentMessage", "Sin mensaje"));
                contentType = data.optString("content_type", data.optString("contentType", "--"));
                brightness = data.optInt("brightness_percent", data.optInt("brightnessLevel", 0));
                temperature = data.optDouble("temperature_celsius", data.optDouble("temperatureCelsius", 0));
                energy = data.optDouble("energy_consumption_watts", data.optDouble("energyConsumptionWatts", 0));
                size = data.optDouble("display_size_inches", data.optDouble("displaySizeInches", 0));
            } else {
                // Fallback formato plano
                displayStatus = obj.optString("displayStatus", "--");
                currentMessage = obj.optString("currentMessage", "Sin mensaje");
                contentType = obj.optString("contentType", "--");
                brightness = obj.optInt("brightnessLevel", 0);
                temperature = obj.optDouble("temperatureCelsius", 0);
                energy = obj.optDouble("energyConsumptionWatts", 0);
                size = obj.optDouble("displaySizeInches", 0);
            }
            
            if (!displayStatus.equals("--") || !currentMessage.equals("Sin mensaje")) {
                
                // Actualizar UI
                tvDisplayStatus.setText(displayStatus);
                tvCurrentMessage.setText(currentMessage);
                tvContentType.setText("Tipo: " + contentType);
                tvBrightness.setText(String.valueOf(brightness));
                tvTemperature.setText(String.format("%.1f", temperature));
                tvEnergyConsumption.setText(String.format("%.1f", energy));
                tvDisplaySize.setText(String.format("%.1f", size));
                
                progressBrightness.setProgress(brightness);
                
                // Actualizar indicador de estado
                if (displayStatus.equalsIgnoreCase("on") || displayStatus.equalsIgnoreCase("active")) {
                    statusIndicator.setBackgroundResource(R.drawable.status_indicator_on);
                } else {
                    statusIndicator.setBackgroundResource(R.drawable.status_indicator_off);
                }
                
                // Agregar al historial
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                String hora = sdf.format(new Date());
                
                String entrada = String.format("[%s] %s: \"%s\"\n", 
                        hora, displayStatus, currentMessage.length() > 30 ? 
                                currentMessage.substring(0, 30) + "..." : currentMessage);
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
