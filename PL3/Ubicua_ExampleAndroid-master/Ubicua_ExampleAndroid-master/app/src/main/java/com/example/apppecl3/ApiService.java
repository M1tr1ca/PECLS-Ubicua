package com.example.apppecl3;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {

    // Obtener todos los datos de todos los sensores
    @GET("ServerExampleUbicomp-1.0-SNAPSHOT/GetAllData")
    Call<AllDataResponse> getAllData();

    // Obtener lista de sensores (datos meteorológicos)
    @GET("ServerExampleUbicomp-1.0-SNAPSHOT/GetData")
    Call<List<AllDataResponse.WeatherMeasurement>> getWeatherData();
}
