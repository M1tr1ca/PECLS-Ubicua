#include <MQUnifiedsensor.h>

#define placa "ESP-32"
#define Voltage_Resolution 3.3
#define pin 34  // Pin analógico del ESP32
#define type "MQ-135"
#define ADC_Bit_Resolution 12
#define RatioMQ135CleanAir 3.6  // Ratio para CO2 en aire limpio

MQUnifiedsensor MQ135(placa, Voltage_Resolution, ADC_Bit_Resolution, pin, type);

void setup() {
  Serial.begin(115200);
  
  MQ135.setRegressionMethod(1); // _PPM = a*ratio^b
  MQ135.setA(110.47);   // Coeficiente A para CO₂
  MQ135.setB(-2.862);   // Coeficiente B para CO₂
  
  MQ135.init();
  
  // Precalentamiento recomendado
  Serial.println("Precalentando sensor MQ-135 (espera 2-3 minutos mínimo)...");
  delay(120000); // 2 minutos mínimo
  // FIXME: esto lo puedes quitar si quires
  // Calibración en aire limpio exterior (400 ppm CO₂)
  Serial.print("Calibrando sensor MQ-135 en aire exterior");
  float calcR0 = 0;
  for(int i = 1; i <= 10; i++) {
    MQ135.update();
    calcR0 += MQ135.calibrate(RatioMQ135CleanAir);
    Serial.print(".");
    delay(500); // Pausa entre lecturas
  }
  MQ135.setR0(calcR0/10);
  
  // Verificar problemas de conexión
  if (isinf(calcR0)) {
    Serial.println("\nWarning: Connection issue found, R0 is infinite (Open circuit detected) please check your wiring and supply");
    while (1);
  }
  if (calcR0 == 0) {
    Serial.println("\nWarning: Connection issue found, R0 is zero (Analog pin with short circuit to ground) please check your wiring and supply");
    while (1);
  }
  
  Serial.println("\nCalibración completa!");
  Serial.print("R0 calculado: ");
  Serial.println(calcR0);
  Serial.println("-----------------------------------");
  Serial.println("Leyendo niveles de CO₂...\n");
}

void loop() {
  MQ135.update();
  float CO2_ppm = MQ135.readSensor();
  
  Serial.print("CO₂: ");
  Serial.print(CO2_ppm, 1); // 1 decimal es suficiente para CO₂
  Serial.print(" ppm | ");
  
  // Calcular índice de calidad
  int indice = calcularIndiceCalidadCO2(CO2_ppm);
  Serial.print("Índice: ");
  Serial.print(indice);
  Serial.print(" - ");
  Serial.println(categoriaCalidadCO2(indice));
  
  delay(2000);
}

// Función para calcular índice de calidad basado en CO₂
int calcularIndiceCalidadCO2(float ppm) {
  // Rangos basados en estándares de salud y ASHRAE para aire exterior/interior
  if (ppm <= 400) return map(ppm, 250, 400, 0, 50);          // Excelente (aire exterior)
  else if (ppm <= 600) return map(ppm, 400, 600, 51, 100);   // Bueno
  else if (ppm <= 1000) return map(ppm, 600, 1000, 101, 150); // Moderado
  else if (ppm <= 1500) return map(ppm, 1000, 1500, 151, 200); // Insalubre (sensibles)
  else if (ppm <= 2000) return map(ppm, 1500, 2000, 201, 250); // Insalubre
  else if (ppm <= 5000) return map(ppm, 2000, 5000, 251, 300); // Muy insalubre
  else return 301; // Peligroso
}

String categoriaCalidadCO2(int indice) {
  if (indice <= 50) return "Excelente";
  else if (indice <= 100) return "Bueno";
  else if (indice <= 150) return "Moderado";
  else if (indice <= 200) return "Insalubre (sensibles)";
  else if (indice <= 250) return "Insalubre";
  else if (indice <= 300) return "Muy insalubre";
  else return "Peligroso";
}
