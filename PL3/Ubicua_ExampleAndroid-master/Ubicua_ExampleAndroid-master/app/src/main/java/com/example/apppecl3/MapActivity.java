package com.example.apppecl3;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends AppCompatActivity {
    private TextView tvStatus;
    private Spinner spinnerStreets;
    private Button btnSelectStreet;
    private MapView mapView;
    private List<StreetWithSensors> streetsList = new ArrayList<>();

    // Centro de Madrid
    private static final double MADRID_LAT = 40.4168;
    private static final double MADRID_LON = -3.7038;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Configurar OSMDroid antes de inflar el layout
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        tvStatus = findViewById(R.id.tvStatus);
        spinnerStreets = findViewById(R.id.spinnerStreets);
        btnSelectStreet = findViewById(R.id.btnSelectStreet);
        mapView = findViewById(R.id.mapView);

        btnSelectStreet.setEnabled(false);

        // Configurar el mapa
        configurarMapa();

        // Cargar calles
        cargarCalles();

        // Configurar botón
        btnSelectStreet.setOnClickListener(v -> {
            int selectedIndex = (int) spinnerStreets.getSelectedItemId();
            if (selectedIndex >= 0 && selectedIndex < streetsList.size()) {
                StreetWithSensors selectedStreet = streetsList.get(selectedIndex);
                abrirMenuSensores(selectedStreet);
            }
        });
    }

    private void configurarMapa() {
        // Configurar tiles de OpenStreetMap
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        
        // Controles de zoom
        mapView.getZoomController().setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
        
        // Centrar en Madrid
        IMapController mapController = mapView.getController();
        mapController.setZoom(11.0);
        GeoPoint startPoint = new GeoPoint(MADRID_LAT, MADRID_LON);
        mapController.setCenter(startPoint);
    }

    private void cargarCalles() {
        tvStatus.setText("🔄 Cargando calles de Madrid...");

        // Obtener calles del servidor usando el endpoint GetStreets
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<List<Street>> call = apiService.getStreets();

        call.enqueue(new Callback<List<Street>>() {
            @Override
            public void onResponse(Call<List<Street>> call, Response<List<Street>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // Convertir Street (API) a StreetWithSensors (modelo interno)
                    streetsList.clear();
                    for (Street street : response.body()) {
                        StreetWithSensors sws = street.toStreetWithSensors();
                        streetsList.add(sws);
                    }
                    Log.i("ubicua", "Calles cargadas del servidor: " + streetsList.size());
                    
                    // Cargar los sensores reales desde GetSensors
                    cargarSensoresDesdeServidor();
                } else {
                    Log.w("ubicua", "Respuesta vacía del servidor");
                    mostrarError("No se encontraron calles en el servidor");
                }
            }

            @Override
            public void onFailure(Call<List<Street>> call, Throwable t) {
                Log.e("ubicua", "Error de conexión al obtener calles: " + t.getMessage());
                mostrarError("Error de conexión: " + t.getMessage());
            }
        });
    }
    
    /**
     * Carga los sensores reales desde el endpoint GetSensors y los asigna a las calles correspondientes
     */
    private void cargarSensoresDesdeServidor() {
        tvStatus.setText("🔄 Cargando sensores...");
        
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<List<SensorInfo>> call = apiService.getSensors();
        
        call.enqueue(new Callback<List<SensorInfo>>() {
            @Override
            public void onResponse(Call<List<SensorInfo>> call, Response<List<SensorInfo>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<SensorInfo> sensores = response.body();
                    Log.i("ubicua", "Sensores recibidos del servidor: " + sensores.size());
                    
                    // Crear un mapa de street_id -> lista de sensores
                    java.util.Map<String, List<Sensor>> sensorsPorCalle = new java.util.HashMap<>();
                    
                    for (SensorInfo sensorInfo : sensores) {
                        String streetId = sensorInfo.getStreetId();
                        Sensor sensor = new Sensor(
                            sensorInfo.getSensorId(), 
                            sensorInfo.getSensorType(), 
                            streetId
                        );
                        
                        if (!sensorsPorCalle.containsKey(streetId)) {
                            sensorsPorCalle.put(streetId, new ArrayList<>());
                        }
                        sensorsPorCalle.get(streetId).add(sensor);
                        
                        Log.i("ubicua", "Sensor " + sensorInfo.getSensorId() + 
                                      " (tipo: " + sensorInfo.getSensorType() + 
                                      ") -> calle " + streetId);
                    }
                    
                    // Asignar sensores a cada calle
                    int callesConSensor = 0;
                    for (StreetWithSensors street : streetsList) {
                        String streetId = street.getStreetId();
                        if (sensorsPorCalle.containsKey(streetId)) {
                            for (Sensor sensor : sensorsPorCalle.get(streetId)) {
                                street.addSensor(sensor);
                            }
                            callesConSensor++;
                            Log.i("ubicua", "Calle " + streetId + " tiene " + 
                                          street.getSensors().size() + " sensores asignados");
                        }
                    }
                    
                    tvStatus.setText("✅ " + streetsList.size() + " calles, " + 
                                    callesConSensor + " con sensores");
                    
                    agregarMarcadoresAlMapa();
                    configurarSpinner();
                } else {
                    Log.w("ubicua", "No se pudieron cargar sensores del servidor, usando por defecto");
                    for (StreetWithSensors street : streetsList) {
                        agregarSensoresPorDefecto(street);
                    }
                    tvStatus.setText("✅ " + streetsList.size() + " calles (sensores por defecto)");
                    agregarMarcadoresAlMapa();
                    configurarSpinner();
                }
            }
            
            @Override
            public void onFailure(Call<List<SensorInfo>> call, Throwable t) {
                Log.e("ubicua", "Error al cargar sensores: " + t.getMessage());
                for (StreetWithSensors street : streetsList) {
                    agregarSensoresPorDefecto(street);
                }
                tvStatus.setText("✅ " + streetsList.size() + " calles (sensores por defecto)");
                agregarMarcadoresAlMapa();
                configurarSpinner();
            }
        });
    }

    /**
     * Añade sensores por defecto a una calle (weather, trafficCounter, trafficLight, display)
     * basándose en el street_id como identificador base
     */
    private void agregarSensoresPorDefecto(StreetWithSensors street) {
        String baseId = street.getStreetId();
        street.addSensor(new Sensor(baseId + "-weather", "weather", baseId));
        street.addSensor(new Sensor(baseId + "-counter", "trafficCounter", baseId));
        street.addSensor(new Sensor(baseId + "-light", "trafficLight", baseId));
        street.addSensor(new Sensor(baseId + "-display", "display", baseId));
    }

    private void mostrarError(String mensaje) {
        tvStatus.setText("❌ " + mensaje);
        btnSelectStreet.setEnabled(false);
    }

    private void agregarMarcadoresAlMapa() {
        // Limpiar marcadores anteriores
        mapView.getOverlays().clear();

        for (StreetWithSensors street : streetsList) {
            GeoPoint point = new GeoPoint(street.getCenterLatitude(), street.getCenterLongitude());

            Marker marker = new Marker(mapView);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(street.getStreetName());
            marker.setSnippet("📍 " + street.getDistrict() + "\nPulsa para ver sensores");
            
            // Icono verde personalizado
            Drawable icon = ContextCompat.getDrawable(this, R.drawable.ic_marker_green);
            if (icon != null) {
                marker.setIcon(icon);
            }

            // Click en el marcador
            marker.setOnMarkerClickListener((m, map) -> {
                CustomInfoWindow.closeAllInfoWindowsOn(map);
                m.showInfoWindow();
                // Buscar la calle correspondiente
                for (StreetWithSensors s : streetsList) {
                    if (s.getStreetName().equals(m.getTitle())) {
                        // Seleccionar en spinner
                        int index = streetsList.indexOf(s);
                        spinnerStreets.setSelection(index);
                        break;
                    }
                }
                return true;
            });

            // Click en la ventana de info
            marker.setInfoWindow(new CustomInfoWindow(mapView, street, this));

            mapView.getOverlays().add(marker);
        }

        // Refrescar el mapa
        mapView.invalidate();

        // Ajustar zoom para ver todos los marcadores
        if (!streetsList.isEmpty()) {
            // 1. Inicializamos con valores extremos inversos
            double minLat = Double.MAX_VALUE;
            double maxLat = -Double.MAX_VALUE; //No usamos MIN_VALUE porque daría 00000, y las coordenadas de Madrid pueden ser negativas
            double minLon = Double.MAX_VALUE;
            double maxLon = -Double.MAX_VALUE;

            // 2. Encontramos los límites
            for (StreetWithSensors street : streetsList) {
                double lat = street.getCenterLatitude();
                double lon = street.getCenterLongitude();

                if (lat < minLat) minLat = lat;
                if (lat > maxLat) maxLat = lat;
                if (lon < minLon) minLon = lon;
                if (lon > maxLon) maxLon = lon;
            }

            // 3. Creamos la caja delimitadora (Norte, Este, Sur, Oeste)
            org.osmdroid.util.BoundingBox box = new org.osmdroid.util.BoundingBox(maxLat, maxLon, minLat, minLon);

            // 4. Hacemos zoom a esa caja (con una animación de 1 segundo y 50px de margen)
            mapView.zoomToBoundingBox(box, true);
        }
    }

    private void configurarSpinner() {
        ArrayList<String> nombres = new ArrayList<>();
        for (StreetWithSensors street : streetsList) {
            nombres.add(street.getStreetName() + " (" + street.getDistrict() + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                nombres
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStreets.setAdapter(adapter);

        btnSelectStreet.setEnabled(true);
    }

    public void abrirMenuSensores(StreetWithSensors street) {
        Intent intent = new Intent(MapActivity.this, SensorMenuActivity.class);
        intent.putExtra("street_id", street.getStreetId());
        intent.putExtra("street_name", street.getStreetName());
        intent.putExtra("street_district", street.getDistrict());
        intent.putExtra("street_neighborhood", street.getNeighborhood());
        intent.putExtra("latitude", street.getCenterLatitude());
        intent.putExtra("longitude", street.getCenterLongitude());

        // Pasar IDs de sensores filtrando por tipo
        List<Sensor> sensors = street.getSensors();
        String sensorWeather = null;
        String sensorTrafficCounter = null;
        String sensorTrafficLight = null;
        String sensorDisplay = null;
        String sensorGeneric = null; // Para sensores tipo "generic" que manejan todos los datos
        
        // Filtrar sensores por tipo
        for (Sensor sensor : sensors) {
            String type = sensor.getSensorType();
            if (type == null) continue;
            
            switch (type.toLowerCase()) {
                case "generic":
                    // Sensor genérico - se usa para todos los tipos de datos
                    sensorGeneric = sensor.getSensorId();
                    break;
                case "weather":
                case "weather_station":
                    sensorWeather = sensor.getSensorId();
                    break;
                case "traffic_counter":
                    sensorTrafficCounter = sensor.getSensorId();
                    break;
                case "traffic_light":
                    sensorTrafficLight = sensor.getSensorId();
                    break;
                case "display":
                    sensorDisplay = sensor.getSensorId();
                    break;
            }
        }
        
        // Si hay un sensor genérico y no hay sensores específicos, usar el genérico
        if (sensorGeneric != null) {
            if (sensorWeather == null) sensorWeather = sensorGeneric;
            if (sensorTrafficCounter == null) sensorTrafficCounter = sensorGeneric;
            if (sensorTrafficLight == null) sensorTrafficLight = sensorGeneric;
            if (sensorDisplay == null) sensorDisplay = sensorGeneric;
        }
        
        Log.i("ubicua", "Sensores para calle " + street.getStreetId() + " (" + street.getStreetName() + "):");
        Log.i("ubicua", "  Weather: " + sensorWeather);
        Log.i("ubicua", "  Traffic Counter: " + sensorTrafficCounter);
        Log.i("ubicua", "  Traffic Light: " + sensorTrafficLight);
        Log.i("ubicua", "  Display: " + sensorDisplay);
        
        // Pasar los sensores específicos (pueden ser null si no existen)
        if (sensorWeather != null) intent.putExtra("sensor_weather", sensorWeather);
        if (sensorTrafficCounter != null) intent.putExtra("sensor_traffic_counter", sensorTrafficCounter);
        if (sensorTrafficLight != null) intent.putExtra("sensor_traffic_light", sensorTrafficLight);
        if (sensorDisplay != null) intent.putExtra("sensor_display", sensorDisplay);

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    // Clase para ventana de info personalizada
    private static class CustomInfoWindow extends org.osmdroid.views.overlay.infowindow.InfoWindow {
        private StreetWithSensors street;
        private MapActivity activity;

        public CustomInfoWindow(MapView mapView, StreetWithSensors street, MapActivity activity) {
            super(R.layout.marker_info_window, mapView);
            this.street = street;
            this.activity = activity;
        }

        @Override
        public void onOpen(Object item) {
            TextView tvTitle = mView.findViewById(R.id.tvInfoTitle);
            TextView tvSnippet = mView.findViewById(R.id.tvInfoSnippet);
            Button btnOpen = mView.findViewById(R.id.btnOpenSensors);
            android.widget.ImageButton btnClose = mView.findViewById(R.id.btnClose);

            tvTitle.setText(street.getStreetName());
            tvSnippet.setText("📍 " + street.getDistrict() + " - " + street.getNeighborhood());

            btnOpen.setOnClickListener(v -> {
                activity.abrirMenuSensores(street);
                close();
            });

            btnClose.setOnClickListener(v -> {
                close();
            });
        }

        @Override
        public void onClose() {
        }
    }
}
