package com.example.apppecl3;

import java.util.ArrayList;
import java.util.List;

/**
 * Datos de calles de Madrid para usar cuando no hay conexión al servidor
 */
public class MadridStreetsData {

    public static List<StreetWithSensors> getDefaultStreets() {
        List<StreetWithSensors> streets = new ArrayList<>();

        // Datos de la tabla_calles.csv con sensores asociados
        streets.add(createStreet("ST_0686", "Calle de Luis Peidró", "Arganzuela", "Imperial",
                40.3971536, 40.3977451, -3.6734246, -3.6731276, "LAB08JAV-G1"));

        streets.add(createStreet("ST_0970", "Calle de Nicolasa Gómez", "San Blas-Canillejas", "San Blas-Canillejas",
                40.4416917, 40.4410557, -3.6136752, -3.6133234, "LAB08JAV-G2"));

        streets.add(createStreet("ST_1725", "Calle de Ramírez de Prado", "Retiro", "Estrella",
                40.3993542, 40.3992644, -3.6882425, -3.6867021, "LAB08JAV-G3"));

        streets.add(createStreet("ST_0871", "Avenida de la Espinela", "Usera", "Usera",
                40.3419156, 40.3450135, -3.710605, -3.7094421, "LAB08JAV-G4"));

        streets.add(createStreet("ST_1617", "Calle Pepe Hillo", "Hortaleza", "Hortaleza",
                40.4513367, 40.4515721, -3.6391751, -3.6409307, "LAB08JAV-G5"));

        streets.add(createStreet("ST_1414", "Calle de la Venta de la Higuera", "Puente de Vallecas", "Puente de Vallecas",
                40.3791936, 40.3803329, -3.6644411, -3.6635773, "LAB08JAV-G6"));

        streets.add(createStreet("ST_0662", "Calle de Estanislao Gómez", "San Blas-Canillejas", "San Blas-Canillejas",
                40.4499298, 40.4522632, -3.6115262, -3.6063218, "LAB08JAV-G7"));

        streets.add(createStreet("ST_1903", "Calle del Puerto de la Bonaigua", "Moratalaz", "Moratalaz",
                40.3874641, 40.3852013, -3.6642555, -3.6661929, "LAB08JAV-G8"));

        streets.add(createStreet("ST_0713", "Calle de Arechavaleta", "Usera", "Usera",
                40.3508421, 40.3523205, -3.6968798, -3.696937, "LAB08JAV-G9"));

        streets.add(createStreet("ST_1382", "Ronda de la Abubilla", "Hortaleza", "Hortaleza",
                40.4533578, 40.4552509, -3.6330225, -3.6373947, "LAB08JAV-G10"));

        streets.add(createStreet("ST_0490", "Calle de Silvano", "Ciudad Lineal", "Ciudad Lineal",
                40.4617143, 40.4574463, -3.6398221, -3.6442275, "LAB08JAV-G11"));

        streets.add(createStreet("ST_0909", "Calle de San Erasmo", "Usera", "Usera",
                40.336041, 40.3374221, -3.7065266, -3.7065365, "LAB08JAV-G12"));

        streets.add(createStreet("ST_2230", "Calle Yuste", "Puente de Vallecas", "Puente de Vallecas",
                40.376668, 40.3755311, -3.6691574, -3.6679391, "LAB08JAV-G13"));

        streets.add(createStreet("ST_1488", "Calle de Méndez Álvaro", "Retiro", "Adelfas",
                40.3969287, 40.3960674, -3.6818367, -3.6808856, "LAB08JAV-G14"));

        streets.add(createStreet("ST_0753", "Calle del Marqués de Viana", "Moncloa-Aravaca", "Moncloa-Aravaca",
                40.4614006, 40.4617461, -3.6999951, -3.7006277, "LAB08JAV-G15"));

        return streets;
    }

    private static StreetWithSensors createStreet(String id, String name, String district, String neighborhood,
                                                   double latStart, double latEnd, double lonStart, double lonEnd,
                                                   String sensorId) {
        StreetWithSensors street = new StreetWithSensors(id, name, district, neighborhood,
                latStart, latEnd, lonStart, lonEnd);
        
        // Añadir sensores de diferentes tipos para cada calle
        street.addSensor(new Sensor(sensorId, "weather"));
        street.addSensor(new Sensor(sensorId + "-TC", "trafficCounter"));
        street.addSensor(new Sensor(sensorId + "-TL", "trafficLight"));
        street.addSensor(new Sensor(sensorId + "-DP", "display"));
        
        return street;
    }
}
