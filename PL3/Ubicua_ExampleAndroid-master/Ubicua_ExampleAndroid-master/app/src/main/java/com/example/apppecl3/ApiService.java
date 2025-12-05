package com.example.apppecl3;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    // Obtener todos los datos de todos los sensores
    @GET("Server/GetAllData")
    Call<AllDataResponse> getAllData();

    // Obtener lista de sensores (datos meteorológicos)
    @GET("Server/GetData")
    Call<List<AllDataResponse.WeatherMeasurement>> getWeatherData();

    // Obtener lista de calles disponibles (devuelve array directamente)
    @GET("Server/GetStreets")
    Call<List<Street>> getStreets();

    // Obtener lista de calles filtradas por distrito
    @GET("Server/GetStreets")
    Call<List<Street>> getStreetsByDistrict(@Query("district") String district);
    
    // Obtener lista de sensores con sus IDs reales
    @GET("Server/GetSensors")
    Call<List<SensorInfo>> getSensors();
}
