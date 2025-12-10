package com.example.apppecl3;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private TextView tvDataCount;
    private ProgressBar progressHumidity;
    
    // Gráficas
    private LineChart chartTemperature;
    private LineChart chartHumidity;
    
    // Datos para las gráficas
    private ArrayList<Entry> temperatureEntries = new ArrayList<>();
    private ArrayList<Entry> humidityEntries = new ArrayList<>();
    private ArrayList<String> timeLabels = new ArrayList<>();
    
    private StringBuilder historial = new StringBuilder();
    private int messageCount = 0;
    private int totalDataCount = 0;
    
    // Máximo de puntos en la gráfica
    private static final int MAX_DATA_POINTS = 50;


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
        tvDataCount = findViewById(R.id.tvDataCount);
        progressHumidity = findViewById(R.id.progressHumidity);
        chartTemperature = findViewById(R.id.chartTemperature);
        chartHumidity = findViewById(R.id.chartHumidity);
        mainHandler = new Handler(Looper.getMainLooper());

        // Configurar gráficas
        setupChart(chartTemperature, "Temperatura (°C)", Color.parseColor("#EF5350"));
        setupChart(chartHumidity, "Humedad (%)", Color.parseColor("#42A5F5"));

        // Botón volver
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Obtener datos del intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            sensorId = extras.getString("sensor_id", "LAB08JAV-G1");
            streetId = extras.getString("street_id", "ST_1");
            sensorType = extras.getString("sensor_type", "weather_station");
            streetName = extras.getString("street_name", "Calle desconocida");
            
            tvStreetName.setText(streetName);
            
            Log.i("ubicua", "Topic de suscripción: /sensors/" + streetId + "/weather_station/" + "#");
            //Log.i("ubicua", "SensorId: " + sensorId + ", StreetId: " + streetId);
            
            // Cargar datos históricos del servidor
            cargarDatosHistoricos();
            
            // Conectar MQTT - Formato: /sensors/{street_id}/{sensor_type}/{sensor_id}
            String topic = "/sensors/" + streetId + "/weather_station/" + "#";
            conectarMqtt(topic);
        }
    }
    
    private void setupChart(LineChart chart, String label, int color) {
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);
        chart.setBackgroundColor(Color.WHITE);
        
        // Configurar eje X
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.parseColor("#78909C"));
        xAxis.setTextSize(10f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < timeLabels.size()) {
                    return timeLabels.get(index);
                }
                return "";
            }
        });
        
        // Configurar eje Y izquierdo
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.parseColor("#78909C"));
        leftAxis.setTextSize(10f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#ECEFF1"));
        
        // Deshabilitar eje Y derecho
        chart.getAxisRight().setEnabled(false);
        
        // Configurar leyenda
        chart.getLegend().setEnabled(true);
        chart.getLegend().setTextColor(Color.parseColor("#546E7A"));
        
        // Datos iniciales vacíos
        LineDataSet dataSet = new LineDataSet(new ArrayList<>(), label);
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(9f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(color);
        dataSet.setFillAlpha(50);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawValues(false);
        
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();
    }
    
    private void cargarDatosHistoricos() {
        tvStatus.setText("Cargando histórico...");
        
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<AllDataResponse> call = apiService.getAllData();
        
        call.enqueue(new Callback<AllDataResponse>() {
            @Override
            public void onResponse(Call<AllDataResponse> call, Response<AllDataResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AllDataResponse data = response.body();
                    procesarDatosHistoricos(data);
                } else {
                    Log.w("ubicua", "No se pudieron cargar datos históricos");
                }
            }

            @Override
            public void onFailure(Call<AllDataResponse> call, Throwable t) {
                Log.e("ubicua", "Error cargando históricos: " + t.getMessage());
            }
        });
    }
    
    private void procesarDatosHistoricos(AllDataResponse data) {
        if (data.getWeather() == null || data.getWeather().isEmpty()) {
            Log.w("ubicua", "No hay datos weather en la respuesta");
            return;
        }
        
        List<AllDataResponse.WeatherMeasurement> weatherList = data.getWeather();
        int count = 0;
        AllDataResponse.WeatherMeasurement ultimoDato = null;
        
        Log.i("ubicua", "Procesando " + weatherList.size() + " registros weather");

        // Añadir a las gráficas
        for (AllDataResponse.WeatherMeasurement m : weatherList) {
                if(m.getStreet_id().equals(streetId)){


                count++;
                ultimoDato = m;
                
                // Añadir a las gráficas (limitar a los últimos MAX_DATA_POINTS)
                if (temperatureEntries.size() < MAX_DATA_POINTS) {
                    float index = temperatureEntries.size();
                    temperatureEntries.add(new Entry(index, (float) m.getTemperature()));
                    humidityEntries.add(new Entry(index, (float) m.getHumidity()));

                    // Extraer hora del timestamp
                    String time = extraerHora(m.getTimestamp());
                    timeLabels.add(time);


                    // Añadir al historial
                    String entrada = String.format(Locale.US, "[%s]  T: %.1f°C  |  H: %.0f%%  |  P: %.0f hPa\n",
                            extraerHora(m.getTimestamp()), m.getTemperature(), m.getHumidity(), m.getPressure());
                    historial.append(entrada);
                }
            }
        }
        
        totalDataCount = count;
        final int finalCount = count;
        final AllDataResponse.WeatherMeasurement datoFinal = ultimoDato;
        
        mainHandler.post(() -> {
            // Si hay datos, mostrar el último en la UI principal
            if (datoFinal != null) {
                tvTemperature.setText(String.format(Locale.US, "%.1f", datoFinal.getTemperature()));
                tvHumidity.setText(String.format(Locale.US, "%.0f", datoFinal.getHumidity()));
                tvPressure.setText(String.format(Locale.US, "%.1f hPa", datoFinal.getPressure()));
                tvAltitude.setText(String.format(Locale.US, "%.0f m", datoFinal.getAltitude()));
                progressHumidity.setProgress((int) datoFinal.getHumidity());
                
                Log.i("ubicua", "Mostrando último dato: T=" + datoFinal.getTemperature() + 
                              ", H=" + datoFinal.getHumidity() + ", P=" + datoFinal.getPressure());
            }
            
            // Actualizar gráficas
            actualizarGraficaTemperatura();
            actualizarGraficaHumedad();
            
            // Actualizar historial
            if (historial.length() > 0) {
                tvHistory.setText(historial.toString());
            }
            if (tvDataCount != null) {
                tvDataCount.setText(totalDataCount + " registros");
            }
            
            Log.i("ubicua", "Cargados " + finalCount + " registros históricos para " + sensorId);
        });
    }
    
    private String extraerHora(String timestamp) {
        if (timestamp == null) return "--:--";
        try {
            // Formato servidor: "Nov 25, 2025, 8:05:00 PM"
            if (timestamp.contains(",") && (timestamp.contains("AM") || timestamp.contains("PM"))) {
                // Buscar la hora antes de AM/PM
                String[] parts = timestamp.split(", ");
                if (parts.length >= 3) {
                    String timePart = parts[2]; // "8:05:00 PM"
                    String[] timeParts = timePart.split(" ");
                    if (timeParts.length >= 1) {
                        String time = timeParts[0]; // "8:05:00"
                        String[] hms = time.split(":");
                        if (hms.length >= 2) {
                            return hms[0] + ":" + hms[1]; // "8:05"
                        }
                    }
                }
            }
            // Formato ISO: 2025-12-05T14:30:00
            else if (timestamp.contains("T")) {
                return timestamp.substring(11, 16);
            } 
            // Formato: 2025-12-05 14:30:00
            else if (timestamp.contains(" ")) {
                String[] parts = timestamp.split(" ");
                if (parts.length > 1) {
                    String time = parts[1];
                    return time.length() >= 5 ? time.substring(0, 5) : time;
                }
            }
        } catch (Exception e) {
            Log.e("ubicua", "Error parseando timestamp: " + timestamp);
        }
        return "--:--";
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
                        Log.e("ubicua", "MQTT conexión perdida: " + (cause != null ? cause.getMessage() : "unknown"));
                        mainHandler.post(() -> {
                            tvStatus.setText("Sin conexión");
                            iconStatus.setImageResource(R.drawable.ic_error);
                        });
                    }

                    @Override
                    public void messageArrived(String receivedTopic, MqttMessage message) {
                        String msg = new String(message.getPayload());
                        Log.i("ubicua", "*** MENSAJE MQTT RECIBIDO ***");
                        Log.i("ubicua", "Topic: " + receivedTopic);
                        Log.i("ubicua", "Payload: " + msg);
                        mainHandler.post(() -> procesarMensajeWeather(msg));
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {}
                });

                client.subscribe(topic, 1);
                Log.i("ubicua", "*** SUSCRITO EXITOSAMENTE al topic: " + topic + " ***");
                
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
            Log.i("ubicua", "Mensaje recibido: " + json);
            JSONObject obj = new JSONObject(json);
            
            // Formato ESP32: {"sensor_id":"...","data":{"temperature_celsius":X,"humidity_percent":Y,...},"location":{"altitude_meters":Z}}
            double temp = 0, humidity = 0, pressure = 0, altitude = 0;
            
            // Intentar formato ESP32 (data.temperature_celsius)
            if (obj.has("data")) {
                JSONObject data = obj.getJSONObject("data");
                temp = data.optDouble("temperature_celsius", data.optDouble("temperature", 0));
                humidity = data.optDouble("humidity_percent", data.optDouble("humidity", 0));
                pressure = data.optDouble("atmospheric_pressure_hpa", data.optDouble("pressure", 0));
                Log.i("ubicua", "Datos parseados - Temp: " + temp + ", Hum: " + humidity + ", Pres: " + pressure);
            }
            
            // Intentar obtener altitud de location
            if (obj.has("location")) {
                JSONObject location = obj.getJSONObject("location");
                altitude = location.optDouble("altitude_meters", 0);
            }
            
            // Fallback a formato plano
            if (temp == 0 && humidity == 0 && pressure == 0) {
                temp = obj.optDouble("temperature", obj.optDouble("temperature_celsius", 0));
                humidity = obj.optDouble("humidity", obj.optDouble("humidity_percent", 0));
                pressure = obj.optDouble("pressure", obj.optDouble("atmospheric_pressure_hpa", 0));
                altitude = obj.optDouble("altitude", obj.optDouble("altitude_meters", 0));
            }
            
            // Actualizar UI siempre que llegue un mensaje válido
            final double finalTemp = temp;
            final double finalHumidity = humidity;
            final double finalPressure = pressure;
            final double finalAltitude = altitude;
            
            // Actualizar UI
            tvTemperature.setText(String.format(Locale.US, "%.1f", finalTemp));
            tvHumidity.setText(String.format(Locale.US, "%.0f", finalHumidity));
            tvPressure.setText(String.format(Locale.US, "%.1f hPa", finalPressure));
            tvAltitude.setText(String.format(Locale.US, "%.0f m", finalAltitude));
            
            progressHumidity.setProgress((int) finalHumidity);
            
            // Agregar al historial
            messageCount++;
            totalDataCount++;
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            String hora = sdf.format(new Date());
            
            String entrada = String.format(Locale.US, "[%s]  T: %.1f°C  |  H: %.0f%%  |  P: %.0f hPa\n", 
                    hora, finalTemp, finalHumidity, finalPressure);
            historial.insert(0, entrada);
            
            if (historial.length() > 800) {
                historial.setLength(800);
            }
            
            tvHistory.setText(historial.toString());
            if (tvDataCount != null) {
                tvDataCount.setText(totalDataCount + " registros");
            }
            
            // Actualizar gráficas en tiempo real
            agregarPuntoGrafica(finalTemp, finalHumidity, hora);
            
            Log.i("ubicua", "UI actualizada correctamente");
            
        } catch (Exception e) {
            Log.e("ubicua", "Error parsing JSON: " + e.getMessage() + " - JSON: " + json);
        }
    }
    
    private void agregarPuntoGrafica(double temp, double humidity, String time) {
        // Limitar número de puntos
        if (temperatureEntries.size() >= MAX_DATA_POINTS) {
            temperatureEntries.remove(0);
            humidityEntries.remove(0);
            timeLabels.remove(0);
            
            // Reindexar
            for (int i = 0; i < temperatureEntries.size(); i++) {
                temperatureEntries.get(i).setX(i);
                humidityEntries.get(i).setX(i);
            }
        }
        
        float index = temperatureEntries.size();
        temperatureEntries.add(new Entry(index, (float) temp));
        humidityEntries.add(new Entry(index, (float) humidity));
        
        // Solo HH:mm para la etiqueta
        String timeLabel = time.length() >= 5 ? time.substring(0, 5) : time;
        timeLabels.add(timeLabel);
        
        actualizarGraficaTemperatura();
        actualizarGraficaHumedad();
    }
    
    private void actualizarGraficaTemperatura() {
        if (temperatureEntries.isEmpty()) return;
        
        LineDataSet dataSet = new LineDataSet(new ArrayList<>(temperatureEntries), "Temperatura (°C)");
        dataSet.setColor(Color.parseColor("#EF5350"));
        dataSet.setCircleColor(Color.parseColor("#EF5350"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#EF5350"));
        dataSet.setFillAlpha(50);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawValues(false);
        
        LineData lineData = new LineData(dataSet);
        chartTemperature.setData(lineData);
        chartTemperature.notifyDataSetChanged();
        chartTemperature.invalidate();
    }
    
    private void actualizarGraficaHumedad() {
        if (humidityEntries.isEmpty()) return;
        
        LineDataSet dataSet = new LineDataSet(new ArrayList<>(humidityEntries), "Humedad (%)");
        dataSet.setColor(Color.parseColor("#42A5F5"));
        dataSet.setCircleColor(Color.parseColor("#42A5F5"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#42A5F5"));
        dataSet.setFillAlpha(50);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawValues(false);
        
        LineData lineData = new LineData(dataSet);
        chartHumidity.setData(lineData);
        chartHumidity.notifyDataSetChanged();
        chartHumidity.invalidate();
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
