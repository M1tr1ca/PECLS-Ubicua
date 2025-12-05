package com.example.apppecl3;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
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

public class WeatherStatsActivity extends AppCompatActivity {
    private String sensorId;
    private String streetId;
    private String sensorType;
    private String streetName;
    private MqttClient client;
    private Handler mainHandler;
    
    private TextView tvStreetName;
    private TextView tvStatus;
    private ImageView iconStatus;
    private TextView tvTemperature;
    private TextView tvHumidity;
    private TextView tvPressure;
    private TextView tvAltitude;
    private TextView tvHistory;
    private ProgressBar progressHumidity;
    
    private StringBuilder historial = new StringBuilder();
    private int messageCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_weather_stats);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        tvStreetName = findViewById(R.id.tvStreetName);
        tvStatus = findViewById(R.id.tvStatus);
        iconStatus = findViewById(R.id.iconStatus);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvPressure = findViewById(R.id.tvPressure);
        tvAltitude = findViewById(R.id.tvAltitude);
        tvHistory = findViewById(R.id.tvHistory);
        progressHumidity = findViewById(R.id.progressHumidity);
        mainHandler = new Handler(Looper.getMainLooper());

        // Obtener datos del intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            sensorId = extras.getString("sensor_id", "LAB08JAV-G1");
            streetId = extras.getString("street_id", "ST_1");
            sensorType = extras.getString("sensor_type", "weather_station");
            streetName = extras.getString("street_name", "Calle desconocida");
            
            tvStreetName.setText(streetName);
            
            // Conectar MQTT - Formato: sensors/{street_id}/{sensor_type}/{sensor_id}
            String topic = "sensors/" + streetId + "/weather_station/" + sensorId;
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
                Log.i("ubicua", "Conectado al broker MQTT");

                mainHandler.post(() -> {
                    tvStatus.setText("Conectado");
                    iconStatus.setImageResource(R.drawable.ic_connected);
                });

                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        mainHandler.post(() -> {
                            tvStatus.setText("Sin conexión");
                            iconStatus.setImageResource(R.drawable.ic_error);
                        });
                    }

                    @Override
                    public void messageArrived(String receivedTopic, MqttMessage message) {
                        String msg = new String(message.getPayload());
                        mainHandler.post(() -> procesarMensajeWeather(msg));
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {}
                });

                client.subscribe(topic, 1);
                Log.i("ubicua", "Suscrito al topic: " + topic);
                
            } catch (MqttException e) {
                Log.e("ubicua", "Error MQTT: " + e.getMessage());
                mainHandler.post(() -> {
                    tvStatus.setText("Error de conexión");
                    iconStatus.setImageResource(R.drawable.ic_error);
                });
            }
        }).start();
    }

    private void procesarMensajeWeather(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            
            // Formato ESP32: {"sensor_id":"...","data":{"temperature_celsius":X,"humidity_percent":Y,...},"location":{"altitude_meters":Z}}
            double temp = 0, humidity = 0, pressure = 0, altitude = 0;
            
            // Intentar formato ESP32 (data.temperature_celsius)
            if (obj.has("data")) {
                JSONObject data = obj.getJSONObject("data");
                temp = data.optDouble("temperature_celsius", data.optDouble("temperature", 0));
                humidity = data.optDouble("humidity_percent", data.optDouble("humidity", 0));
                pressure = data.optDouble("atmospheric_pressure_hpa", data.optDouble("pressure", 0));
            }
            
            // Intentar obtener altitud de location
            if (obj.has("location")) {
                JSONObject location = obj.getJSONObject("location");
                altitude = location.optDouble("altitude_meters", 0);
            }
            
            // Fallback a formato plano
            if (temp == 0 && obj.has("temperature")) {
                temp = obj.optDouble("temperature", 0);
                humidity = obj.optDouble("humidity", 0);
                pressure = obj.optDouble("pressure", 0);
                altitude = obj.optDouble("altitude", 0);
            }
            
            if (temp != 0 || humidity != 0 || pressure != 0) {
                
                // Actualizar UI
                tvTemperature.setText(String.format(Locale.US, "%.1f", temp));
                tvHumidity.setText(String.format(Locale.US, "%.0f", humidity));
                tvPressure.setText(String.format(Locale.US, "%.1f hPa", pressure));
                tvAltitude.setText(String.format(Locale.US, "%.0f m", altitude));
                
                progressHumidity.setProgress((int) humidity);
                
                // Agregar al historial
                messageCount++;
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                String hora = sdf.format(new Date());
                
                String entrada = String.format(Locale.US, "[%s]  T: %.1f°C  |  H: %.0f%%  |  P: %.0f hPa\n", 
                        hora, temp, humidity, pressure);
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
