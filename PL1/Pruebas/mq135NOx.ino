#include <MQUnifiedsensor.h>

#define placa "ESP-32"
#define Voltage_Resolution 3.3
#define pin 34  // Pin analógico del ESP32
#define type "MQ-135"
#define ADC_Bit_Resolution 12
#define RatioMQ135CleanAir 3.6

MQUnifiedsensor MQ135(placa, Voltage_Resolution, ADC_Bit_Resolution, pin, type);

void setup() {
  Serial.begin(115200);
  
  MQ135.setRegressionMethod(1); // _PPM = a*ratio^b
  MQ135.setA(77.255); 
  MQ135.setB(-3.18); // Coeficientes para NOx
  
  MQ135.init();
  
  // Calibración en aire limpio
  Serial.print("Calibrando sensor MQ-135");
  float calcR0 = 0;
  for(int i = 1; i <= 10; i++) {
    MQ135.update();
    calcR0 += MQ135.calibrate(RatioMQ135CleanAir);
    Serial.print(".");
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
  
  Serial.println(" Calibración completa!");
  Serial.print("R0 calculado: ");
  Serial.println(calcR0);
  Serial.println("-----------------------------------");
}

void loop() {
  MQ135.update();
  float NOx_ppm = MQ135.readSensor();
  
  Serial.print("NOx: ");
  Serial.print(NOx_ppm, 3);//3 decimales
  Serial.print(" ppm | ");
  
  // Calcular índice de calidad
  int indice = calcularIndiceCalidadNOx(NOx_ppm);
  Serial.print("Índice: ");
  Serial.print(indice);
  Serial.print(" - ");
  Serial.println(categoriaCalidadNOx(indice));
  
  delay(2000);
}

// Función para calcular índice de calidad basado en NOx
int calcularIndiceCalidadNOx(float ppm) {
  // Rangos aproximados para NOx en aire exterior (basados en estándares EPA)
  if (ppm <= 0.05) return map(ppm * 1000, 0, 50, 0, 50);           // Bueno
  else if (ppm <= 0.1) return map(ppm * 1000, 50, 100, 51, 100);   // Moderado
  else if (ppm <= 0.36) return map(ppm * 1000, 100, 360, 101, 150); // Insalubre (sensibles)
  else if (ppm <= 0.65) return map(ppm * 1000, 360, 650, 151, 200); // Insalubre
  else if (ppm <= 1.24) return map(ppm * 1000, 650, 1240, 201, 300); // Muy insalubre
  else return 301; // Peligroso
}

String categoriaCalidadNOx(int indice) {
  if (indice <= 50) return "Bueno";
  else if (indice <= 100) return "Moderado";
  else if (indice <= 150) return "Insalubre (sensibles)";
  else if (indice <= 200) return "Insalubre";
  else if (indice <= 300) return "Muy insalubre";
  else return "Peligroso";
}