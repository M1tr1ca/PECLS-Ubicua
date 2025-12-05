package com.example.apppecl3;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
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

        // Intentar obtener del servidor, si falla usar datos por defecto
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<AllDataResponse> call = apiService.getAllData();

        call.enqueue(new Callback<AllDataResponse>() {
            @Override
            public void onResponse(Call<AllDataResponse> call, Response<AllDataResponse> response) {
                usarDatosPorDefecto();
            }

            @Override
            public void onFailure(Call<AllDataResponse> call, Throwable t) {
                Log.e("ubicua", "Error de conexión: " + t.getMessage());
                usarDatosPorDefecto();
            }
        });
    }

    private void usarDatosPorDefecto() {
        streetsList = MadridStreetsData.getDefaultStreets();
        tvStatus.setText("✅ " + streetsList.size() + " calles cargadas");

        agregarMarcadoresAlMapa();
        configurarSpinner();
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
            double minLat = Double.MAX_VALUE, maxLat = Double.MIN_VALUE;
            double minLon = Double.MAX_VALUE, maxLon = Double.MIN_VALUE;
            
            for (StreetWithSensors street : streetsList) {
                minLat = Math.min(minLat, street.getCenterLatitude());
                maxLat = Math.max(maxLat, street.getCenterLatitude());
                minLon = Math.min(minLon, street.getCenterLongitude());
                maxLon = Math.max(maxLon, street.getCenterLongitude());
            }
            
            double centerLat = (minLat + maxLat) / 2;
            double centerLon = (minLon + maxLon) / 2;
            
            IMapController mapController = mapView.getController();
            mapController.setCenter(new GeoPoint(centerLat, centerLon));
            mapController.setZoom(11.5);
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

        // Pasar IDs de sensores
        List<Sensor> sensors = street.getSensors();
        if (!sensors.isEmpty()) {
            for (Sensor sensor : sensors) {
                switch (sensor.getSensorType()) {
                    case "weather":
                        intent.putExtra("sensor_weather", sensor.getSensorId());
                        break;
                    case "trafficCounter":
                        intent.putExtra("sensor_traffic_counter", sensor.getSensorId());
                        break;
                    case "trafficLight":
                        intent.putExtra("sensor_traffic_light", sensor.getSensorId());
                        break;
                    case "display":
                        intent.putExtra("sensor_display", sensor.getSensorId());
                        break;
                }
            }
        }

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
