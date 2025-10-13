# 📚 Índice de Documentación - Estación Meteorológica IoT

## 📋 Guía de Navegación

Bienvenido a la documentación completa del proyecto **Estación Meteorológica IoT** para el curso de Computación Ubicua de la Universidad de Alcalá de Henares.

---

## 🗂️ Estructura de la Documentación

### 📖 Documentación Principal

#### 1. [README.md](README.md) - Descripción General del Proyecto
**Contenido:**
- Descripción general del sistema
- Componentes hardware y software
- Datos monitorizados
- Configuración MQTT
- Ubicación del sensor
- Características principales
- Estructura del proyecto

**Para quién:**
- Primeros pasos
- Visión general rápida
- Presentación del proyecto

---

#### 2. [ARQUITECTURA.md](ARQUITECTURA.md) - Documentación Técnica
**Contenido:**
- Arquitectura en 3 capas
- Diagrama de conexiones hardware
- Componentes software detallados
- Flujo de datos completo
- Algoritmo de cálculo CAQI
- Sistema de alertas
- Validación y manejo de errores
- Optimizaciones implementadas

**Para quién:**
- Desarrolladores
- Revisión técnica
- Comprensión profunda del sistema

---

#### 3. [INSTALACION.md](INSTALACION.md) - Guía de Instalación
**Contenido:**
- Requisitos previos
- Instalación paso a paso (Wokwi y Hardware Real)
- Configuración de bibliotecas
- Conexión del hardware
- Compilación y carga del código
- Verificación de la instalación
- Solución de problemas detallada
- Herramientas de monitoreo

**Para quién:**
- Instalación inicial
- Configuración del entorno
- Resolución de problemas

---

#### 4. [API_MQTT.md](API_MQTT.md) - Especificación API MQTT
**Contenido:**
- Configuración del broker MQTT
- Estructura de tópicos
- Formato JSON completo
- Mensajes de control
- Flujos de comunicación
- Códigos de estado
- Esquemas JSON (JSON Schema)
- Ejemplos de uso en varios lenguajes
- Índice CAQI detallado
- Seguridad y optimización

**Para quién:**
- Integración con otros sistemas
- Desarrollo de clientes MQTT
- Análisis de datos

---

## 🎨 Diagramas Visuales

### Carpeta: [drawio/](../drawio/)

#### 1. [arquitectura_sistema.drawio](../drawio/arquitectura_sistema.drawio)
**Diagrama de Arquitectura del Sistema**
- Vista de 3 capas (Comunicación, Procesamiento, Sensores)
- Módulos y sus interacciones
- Especificaciones técnicas
- Leyenda de componentes

#### 2. [flujo_datos.drawio](../drawio/flujo_datos.drawio)
**Diagrama de Flujo de Datos**
- Ciclo completo de operación
- 5 fases del sistema
- Decisiones condicionales
- Intervalos y umbrales

#### 3. [comunicacion_mqtt.drawio](../drawio/comunicacion_mqtt.drawio)
**Diagrama de Comunicación MQTT**
- Secuencia de mensajes MQTT
- Publicación y suscripción
- Comandos de control
- Configuración y QoS

#### 4. [conexiones_hardware.drawio](../drawio/conexiones_hardware.drawio)
**Diagrama de Conexiones Hardware**
- Esquema de conexiones ESP32
- Pines GPIO utilizados
- Componentes necesarios
- Especificaciones eléctricas

#### 📋 [README.md - Diagramas](../drawio/README.md)
Guía completa para usar, editar y exportar los diagramas.

---

## 🚀 Rutas de Aprendizaje

### 👤 Usuario Nuevo (Nunca ha trabajado con IoT)

1. ✅ [README.md](README.md) - Entender qué hace el proyecto
2. ✅ [Diagrama de Arquitectura](../drawio/arquitectura_sistema.drawio) - Ver estructura visual
3. ✅ [INSTALACION.md](INSTALACION.md) - Seguir instalación paso a paso
4. ✅ Probar con Wokwi (simulador)

### 🔧 Instalador/Configurador

1. ✅ [INSTALACION.md](INSTALACION.md) - Guía completa
2. ✅ [Diagrama de Conexiones](../drawio/conexiones_hardware.drawio) - Esquema físico
3. ✅ [README.md](README.md) - Verificación final
4. ✅ Sección "Troubleshooting" en INSTALACION.md

### 💻 Desarrollador

1. ✅ [README.md](README.md) - Visión general
2. ✅ [ARQUITECTURA.md](ARQUITECTURA.md) - Detalles técnicos
3. ✅ [Diagrama de Flujo](../drawio/flujo_datos.drawio) - Lógica del sistema
4. ✅ [API_MQTT.md](API_MQTT.md) - Interfaz de comunicación
5. ✅ Código fuente (main.ino, headers)

### 🔌 Integrador de Sistemas

1. ✅ [API_MQTT.md](API_MQTT.md) - Especificación completa
2. ✅ [Diagrama MQTT](../drawio/comunicacion_mqtt.drawio) - Flujo de mensajes
3. ✅ Ejemplos de clientes (Python, Node.js, etc.)
4. ✅ JSON Schemas

### 🎓 Estudiante (Entrega del Proyecto)

1. ✅ [README.md](README.md) - Descripción para el informe
2. ✅ [ARQUITECTURA.md](ARQUITECTURA.md) - Análisis técnico
3. ✅ Todos los diagramas - Anexos visuales
4. ✅ [INSTALACION.md](INSTALACION.md) - Manual de usuario

---

## 📊 Mapa Mental del Proyecto

```
Estación Meteorológica IoT
│
├── 📖 Documentación
│   ├── README.md (General)
│   ├── ARQUITECTURA.md (Técnico)
│   ├── INSTALACION.md (Práctico)
│   └── API_MQTT.md (Integración)
│
├── 🎨 Diagramas
│   ├── Arquitectura del Sistema
│   ├── Flujo de Datos
│   ├── Comunicación MQTT
│   └── Conexiones Hardware
│
├── 💾 Código Fuente
│   ├── main.ino (Principal)
│   ├── ESP32_UTILS.hpp (WiFi)
│   ├── ESP32_Utils_MQTT.hpp (MQTT)
│   └── config.h (Configuración)
│
└── ⚙️ Configuración
    ├── config.h (C++)
    └── config.json (JSON)
```

---

## 🔍 Búsqueda Rápida por Tema

### Hardware
- Componentes → [README.md](README.md#componentes-del-sistema)
- Conexiones → [Diagrama Hardware](../drawio/conexiones_hardware.drawio)
- Especificaciones → [ARQUITECTURA.md](ARQUITECTURA.md#diagrama-de-conexiones-hardware)
- Lista de compras → [INSTALACION.md](INSTALACION.md#lista-de-componentes)

### Software
- Arquitectura → [ARQUITECTURA.md](ARQUITECTURA.md)
- Flujo de datos → [Diagrama de Flujo](../drawio/flujo_datos.drawio)
- Funciones → [ARQUITECTURA.md](ARQUITECTURA.md#componentes-software)

### Sensores
- BME280 → [README.md](README.md#sensores), [ARQUITECTURA.md](ARQUITECTURA.md#esquema-de-conexión-bme280)
- MQ-135 → [README.md](README.md#sensores), [ARQUITECTURA.md](ARQUITECTURA.md#algoritmo-de-cálculo-caqi)
- Cálculo CAQI → [ARQUITECTURA.md](ARQUITECTURA.md#algoritmo-de-cálculo-caqi), [API_MQTT.md](API_MQTT.md#índice-caqi)

### Comunicación
- MQTT → [API_MQTT.md](API_MQTT.md)
- JSON → [API_MQTT.md](API_MQTT.md#formato-de-mensajes-json)
- Tópicos → [API_MQTT.md](API_MQTT.md#tópicos-mqtt)
- Diagrama → [Comunicación MQTT](../drawio/comunicacion_mqtt.drawio)

### Instalación
- Wokwi → [INSTALACION.md](INSTALACION.md#opción-1-simulador-wokwi)
- Hardware Real → [INSTALACION.md](INSTALACION.md#opción-2-hardware-real)
- Bibliotecas → [INSTALACION.md](INSTALACION.md#bibliotecas-requeridas)
- Problemas → [INSTALACION.md](INSTALACION.md#solución-de-problemas)

### Configuración
- WiFi → [config.h](../config.h), [INSTALACION.md](INSTALACION.md#configurar-el-proyecto)
- MQTT Broker → [API_MQTT.md](API_MQTT.md#configuración-del-broker-mqtt)
- Umbrales → [ARQUITECTURA.md](ARQUITECTURA.md#sistema-de-alertas)
- Parámetros → [config.json](../config.json)

---

## 📝 Glosario Rápido

| Término | Descripción | Dónde encontrar más |
|---------|-------------|---------------------|
| **ESP32** | Microcontrolador WiFi/Bluetooth | [README.md](README.md) |
| **BME280** | Sensor de temp/humedad/presión | [ARQUITECTURA.md](ARQUITECTURA.md) |
| **MQ-135** | Sensor de calidad del aire | [ARQUITECTURA.md](ARQUITECTURA.md) |
| **MQTT** | Protocolo de mensajería IoT | [API_MQTT.md](API_MQTT.md) |
| **CAQI** | Índice de calidad del aire | [API_MQTT.md](API_MQTT.md#índice-caqi) |
| **QoS** | Quality of Service MQTT | [API_MQTT.md](API_MQTT.md#qos-quality-of-service) |
| **I2C** | Bus de comunicación serial | [ARQUITECTURA.md](ARQUITECTURA.md) |
| **ADC** | Convertidor analógico-digital | [ARQUITECTURA.md](ARQUITECTURA.md) |
| **JSON** | Formato de datos | [API_MQTT.md](API_MQTT.md#formato-de-mensajes-json) |

---

## 📞 Información de Contacto y Soporte

### Proyecto
- **Universidad**: Universidad de Alcalá de Henares
- **Asignatura**: Computación Ubicua
- **Práctica**: PECL1
- **Tipo**: Estación Meteorológica IoT

### Recursos Externos
- [Documentación ESP32](https://docs.espressif.com/projects/esp-idf/en/latest/esp32/)
- [MQTT.org](https://mqtt.org/)
- [Wokwi Simulator](https://wokwi.com/)
- [Draw.io](https://app.diagrams.net/)

---

## 📌 Actualizaciones del Documento

| Versión | Fecha | Cambios |
|---------|-------|---------|
| 1.0 | 13/10/2025 | Documentación inicial completa |
| | | - README.md creado |
| | | - ARQUITECTURA.md creado |
| | | - INSTALACION.md creado |
| | | - API_MQTT.md creado |
| | | - Diagramas DrawIO creados |
| | | - INDEX.md creado |

---

## ✅ Checklist de Documentación

### Documentación Escrita
- [x] README.md - Descripción general
- [x] ARQUITECTURA.md - Detalles técnicos
- [x] INSTALACION.md - Guía de instalación
- [x] API_MQTT.md - Especificación API
- [x] INDEX.md - Índice general (este archivo)

### Diagramas
- [x] Arquitectura del sistema
- [x] Flujo de datos
- [x] Comunicación MQTT
- [x] Conexiones hardware
- [x] README de diagramas

### Código
- [x] main.ino documentado
- [x] ESP32_UTILS.hpp documentado
- [x] ESP32_Utils_MQTT.hpp documentado
- [x] config.h comentado
- [x] config.json estructurado

---

## 🎯 Próximos Pasos Sugeridos

Después de revisar la documentación:

1. **Si eres nuevo**: Comienza con [README.md](README.md)
2. **Para instalar**: Sigue [INSTALACION.md](INSTALACION.md)
3. **Para desarrollar**: Lee [ARQUITECTURA.md](ARQUITECTURA.md)
4. **Para integrar**: Consulta [API_MQTT.md](API_MQTT.md)
5. **Para entender visualmente**: Abre los [Diagramas](../drawio/)

---

*Documentación creada para el proyecto PECL1 - Computación Ubicua - Universidad de Alcalá de Henares*

**📅 Última actualización:** 13 de Octubre de 2025

