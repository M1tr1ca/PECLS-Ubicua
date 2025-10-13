#ifndef ESP32_UTILS_HPP
#define ESP32_UTILS_HPP

#include <WiFi.h>

// ============================================
// FUNCIONES DE CONEXIÓN WIFI
// ============================================

/**
 * Conecta el ESP32 a una red WiFi en modo Estación (STA)
 * @param useStaticIP: true para usar IP estática, false para DHCP
 */
void ConnectWifi_STA(bool useStaticIP = false) {
    Serial.println("");
    Serial.println("===========================================");
    Serial.println("Conectando a WiFi...");
    Serial.println("===========================================");
    
    WiFi.mode(WIFI_STA);// Set station mode para conectarse a un router
    WiFi.setHostname(hostname);
    
    if (useStaticIP) {
        WiFi.config(ip, gateway, subnet);
    }
    
    WiFi.begin(ssid, password);
    
    int attempts = 0;
    while (WiFi.status() != WL_CONNECTED && attempts < 50) {
        delay(500);
        Serial.print(".");
        attempts++;
    }
    
    if (WiFi.status() == WL_CONNECTED) {
        Serial.println("");
        Serial.println("✓ WiFi Conectado");
        Serial.print("  SSID: ");
        Serial.println(ssid);
        Serial.print("  IP: ");
        Serial.println(WiFi.localIP());
        Serial.print("  Señal: ");
        Serial.print(WiFi.RSSI());
        Serial.println(" dBm");
    } else {
        Serial.println("");
        Serial.println("✗ Error: No se pudo conectar a WiFi");
        Serial.println("  Verifica las credenciales en config.h");
    }
    Serial.println("===========================================");
}

/**
 * Conecta el ESP32 como Punto de Acceso (AP)
 * @param useStaticIP: true para configurar IP del AP
 */

// [ ] Mierar si tiene que ser un AP o se conecta por WIFI
void ConnectWifi_AP(bool useStaticIP = false) {
    Serial.println("");
    Serial.println("===========================================");
    Serial.println("Iniciando Punto de Acceso...");
    Serial.println("===========================================");
    
    WiFi.mode(WIFI_AP);
    
    while (!WiFi.softAP(ssid, password)) {
        Serial.print(".");
        delay(100);
    }
    
    if (useStaticIP) {
        WiFi.softAPConfig(ip, gateway, subnet);
    }
    
    Serial.println("");
    Serial.println("✓ Punto de Acceso iniciado");
    Serial.print("  SSID: ");
    Serial.println(ssid);
    Serial.print("  IP: ");
    Serial.println(WiFi.softAPIP());
    Serial.println("===========================================");
}

/**
 * Verifica y reconecta WiFi si es necesario
 */
void CheckWiFiConnection() {
    if (WiFi.status() != WL_CONNECTED) {
        Serial.println("⚠ WiFi desconectado. Reconectando...");
        ConnectWifi_STA(false);
    }
}

/**
 * Manejador de eventos WiFi
 */
void WiFiEvent(WiFiEvent_t event) {
    switch(event) {
        case SYSTEM_EVENT_STA_GOT_IP:
            Serial.println("✓ IP obtenida");
            break;
        case SYSTEM_EVENT_STA_DISCONNECTED:
            Serial.println("⚠ WiFi desconectado");
            break;
        case SYSTEM_EVENT_STA_START:
            Serial.println("→ WiFi iniciado");
            break;
        case SYSTEM_EVENT_STA_CONNECTED:
            Serial.println("✓ WiFi conectado");
            break;
        default:
            break;
    }
}

#endif