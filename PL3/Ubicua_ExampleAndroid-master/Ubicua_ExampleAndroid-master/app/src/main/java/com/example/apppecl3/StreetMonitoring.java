package com.example.apppecl3;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ScrollView;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StreetMonitoring extends AppCompatActivity {
    private String sensorId;
    private String sensorType;
    private MqttClient client;
    private TextView tvSensorName;
    private TextView tvStatus;
    private TextView tvMonitoring;
    private ScrollView scrollView;
    private Handler mainHandler;
    private StringBuilder mensajesRecibidos = new StringBuilder();
    private int messageCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_street_monitoring);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        tvSensorName = findViewById(R.id.streetname);
        tvStatus = findViewById(R.id.tvStatus);
        tvMonitoring = findViewById(R.id.monitoring);
        scrollView = findViewById(R.id.scrollView);
        mainHandler = new Handler(Looper.getMainLooper());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            sensorId = extras.getString("sensor_id");
            sensorType = extras.getString("sensor_type", "generic");
            
            // Mostrar info del sensor
            tvSensorName.setText("📡 Sensor: " + sensorId);
            tvStatus.setText("🔄 Conectando al broker MQTT...");
            
            Log.i("ubicua", "Sensor ID: " + sensorId + " Tipo: " + sensorType);
            
            // Suscribirse al topic del sensor
            // Topic format: sensors/LAB08JAV-G1 (basado en sensor_id)
            String topic = "/sensors/" + sensorId;
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

                Log.i("ubicua", "Conectando al broker MQTT...");
                client.connect(options);
                Log.i("ubicua", "Conectado ✔");

                // Actualizar UI en hilo principal
                mainHandler.post(() -> {
                    tvStatus.setText("✅ Conectado | Topic: " + topic);
                });

                // Configurar callback para mensajes
                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        Log.e("ubicua", "Conexión perdida: " + cause.getMessage());
                        mainHandler.post(() -> {
                            tvStatus.setText("❌ Conexión perdida - Reconectando...");
                        });
                    }

                    @Override
                    public void messageArrived(String receivedTopic, MqttMessage message) {
                        String msg = new String(message.getPayload());
                        Log.i("ubicua", "[" + receivedTopic + "] " + msg);
                        mainHandler.post(() -> {
                            agregarMensaje(receivedTopic, msg);
                        });
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        // No usado para suscriptor
                    }
                });

                // Suscribirse al topic
                client.subscribe(topic, 1);
                Log.i("ubicua", "Suscrito a: " + topic);

                // También suscribirse a topics wildcard para captar más mensajes
                client.subscribe("sensors/#", 1);
                Log.i("ubicua", "Suscrito a: sensors/#");

            } catch (MqttException e) {
                Log.e("ubicua", "Error MQTT: " + e.getMessage());
                e.printStackTrace();
                
                mainHandler.post(() -> {
                    tvStatus.setText("❌ Error: " + e.getMessage());
                    tvMonitoring.setText("No se pudo conectar al broker MQTT.\n\n" +
                            "Verifica que:\n" +
                            "• Docker esté corriendo\n" +
                            "• El broker Mosquitto esté activo\n" +
                            "• Puerto 1883 esté disponible");
                });
            }
        }).start();
    }

    private void agregarMensaje(String topic, String mensaje) {
        messageCount++;
        
        // Obtener timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String hora = sdf.format(new Date());
        
        // Formatear mensaje JSON de forma legible
        String mensajeFormateado = formatearMensaje(mensaje);
        
        // Agregar mensaje con formato (nuevos arriba)
        String nuevoMensaje = "━━━━━━━━━━━━━━━━━━━━━\n" +
                "📩 Mensaje #" + messageCount + "\n" +
                "⏱ " + hora + "\n" +
                "📍 Topic: " + topic + "\n\n" +
                mensajeFormateado + "\n\n";
        
        mensajesRecibidos.insert(0, nuevoMensaje);
        tvMonitoring.setText(mensajesRecibidos.toString());
        
        // Scroll al inicio para ver el último mensaje
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_UP));
    }

    private String formatearMensaje(String json) {
        try {
            // Formateo simple del JSON para mejor visualización
            return json
                    .replace(",", ",\n  ")
                    .replace("{", "{\n  ")
                    .replace("}", "\n}")
                    .replace("\":", "\": ");
        } catch (Exception e) {
            return json;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new Thread(() -> {
            try {
                if (client != null && client.isConnected()) {
                    client.disconnect();
                    client.close();
                    Log.i("ubicua", "Desconectado del broker MQTT");
                }
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}