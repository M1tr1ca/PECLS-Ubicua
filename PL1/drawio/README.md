# 📊 Diagramas del Proyecto - Estación Meteorológica IoT

Esta carpeta contiene todos los diagramas técnicos del proyecto en formato DrawIO (.drawio).

## 📁 Archivos Disponibles

### 1. `arquitectura_sistema.drawio`
**Diagrama de Arquitectura del Sistema**

Muestra la estructura completa del sistema en tres capas:
- **Capa de Comunicación**: Broker MQTT y tópicos
- **Capa de Procesamiento**: ESP32 con módulos WiFi, MQTT y lógica principal
- **Capa de Sensores**: BME280, MQ-135 y LED de alerta

**Incluye:**
- Módulos del sistema
- Flujo de datos entre capas
- Especificaciones técnicas
- Leyenda de componentes

### 2. `flujo_datos.drawio`
**Diagrama de Flujo de Datos**

Representa el ciclo completo de operación del sistema:
- **Fase 1**: Inicialización (pines, WiFi, MQTT, sensores)
- **Fase 2**: Lectura de sensores (cada 30s)
- **Fase 3**: Procesamiento y actuación (validación, umbrales, LED)
- **Fase 4**: Transmisión MQTT (publicación de datos)
- **Fase 5**: Monitoreo continuo (verificación de conexiones)

**Incluye:**
- Decisiones condicionales
- Intervalos de tiempo
- Umbrales de alerta
- Cálculo CAQI
- Formato JSON

### 3. `comunicacion_mqtt.drawio`
**Diagrama de Comunicación MQTT**

Detalla el protocolo de comunicación MQTT:
- Secuencia de conexión (TCP, CONNECT, CONNACK)
- Suscripción a tópicos (SUBSCRIBE, SUBACK)
- Publicación de datos (PUBLISH, PUBACK)
- Recepción de comandos de control
- Códigos de estado y QoS

**Incluye:**
- Mensajes MQTT paso a paso
- Payload JSON de ejemplo
- Comandos disponibles
- Configuración del broker
- Niveles QoS
- Estadísticas y seguridad

### 4. `conexiones_hardware.drawio`
**Diagrama de Conexiones Hardware**

Esquema de conexiones físicas del ESP32:
- Conexiones del BME280 (I2C)
- Conexiones del MQ-135 (analógico)
- Conexión del LED con resistencia
- Resistencias pull-up I2C
- Pines GPIO utilizados

**Incluye:**
- Tabla de componentes necesarios
- Especificaciones eléctricas
- Notas importantes de conexión
- Leyenda de tipos de conexión

## 🔧 Cómo Abrir los Diagramas

### Opción 1: Draw.io Desktop (Recomendado)

1. **Descargar Draw.io Desktop:**
   - Windows: https://github.com/jgraph/drawio-desktop/releases
   - macOS: `brew install --cask drawio`
   - Linux: Descargar .deb o .rpm desde GitHub

2. **Abrir el archivo:**
   - Doble clic en el archivo `.drawio`
   - O abrir Draw.io y cargar el archivo

### Opción 2: Draw.io Online

1. Ir a: https://app.diagrams.net/
2. Clic en "Open Existing Diagram"
3. Seleccionar el archivo `.drawio` de tu computadora

### Opción 3: VS Code

1. Instalar extensión: "Draw.io Integration"
2. Abrir el archivo `.drawio` directamente en VS Code

### Opción 4: Importar a otras herramientas

Los archivos `.drawio` son XML y pueden convertirse a:
- **PNG/JPG**: Archivo → Exportar como → PNG/JPEG
- **SVG**: Archivo → Exportar como → SVG
- **PDF**: Archivo → Exportar como → PDF
- **HTML**: Archivo → Exportar como → HTML

## 📝 Editar los Diagramas

### Modificar un diagrama existente:

1. Abrir el archivo en Draw.io
2. Hacer las modificaciones necesarias
3. Guardar (Ctrl+S o Cmd+S)
4. Exportar a formato deseado si es necesario

### Crear un nuevo diagrama:

1. Abrir Draw.io
2. Crear nuevo diagrama
3. Usar las plantillas o empezar desde cero
4. Guardar como `.drawio` en esta carpeta

## 🎨 Convenciones de Colores

Los diagramas usan un código de colores consistente:

| Color | Uso | Código Hex |
|-------|-----|------------|
| 🟢 Verde | Capas de procesamiento, estados OK | `#D5E8D4` |
| 🟡 Amarillo | Comunicación, broker MQTT | `#FFF2CC` |
| 🔵 Azul | Comunicación de datos, WiFi | `#DAE8FC` |
| 🟠 Naranja | Procesamiento, cálculos | `#FFE6CC` |
| 🔴 Rojo | Alertas, errores, LED | `#F8CECC` |
| 🟣 Morado | Control, comandos, tópicos | `#E1D5E7` |
| ⚫ Gris | Configuración, neutro | `#F5F5F5` |

## 📤 Exportar Diagramas

### Para documentación:

```bash
# Exportar todos a PNG (alta resolución)
# En Draw.io: Archivo → Exportar como → PNG
# Configuración recomendada:
# - Zoom: 100%
# - Borde: 10px
# - DPI: 300 (para impresión)
```

### Para presentaciones:

```bash
# Exportar a SVG (vectorial, escalable)
# En Draw.io: Archivo → Exportar como → SVG
# Incluir: Sombras y gradientes
```

### Para integración web:

```bash
# Exportar a HTML
# En Draw.io: Archivo → Exportar como → HTML
# Opciones: Incluir copia del diagrama
```

## 🔄 Actualización de Diagramas

Si modificas el código del proyecto, actualiza los diagramas correspondientes:

1. **Cambios en arquitectura** → `arquitectura_sistema.drawio`
2. **Cambios en flujo de datos** → `flujo_datos.drawio`
3. **Cambios en MQTT** → `comunicacion_mqtt.drawio`
4. **Cambios en hardware** → `conexiones_hardware.drawio`

## 📋 Checklist de Diagramas

- [x] Diagrama de arquitectura del sistema
- [x] Diagrama de flujo de datos
- [x] Diagrama de comunicación MQTT
- [x] Diagrama de conexiones hardware
- [ ] Diagrama de estados (opcional)
- [ ] Diagrama de casos de uso (opcional)
- [ ] Diagrama de secuencia detallado (opcional)

## 🛠️ Troubleshooting

### Problema: El archivo no abre en Draw.io

**Solución:**
- Verificar que la extensión sea `.drawio`
- Abrir Draw.io primero, luego importar el archivo
- Usar Draw.io Online si la versión desktop falla

### Problema: Las fuentes se ven diferentes

**Solución:**
- Draw.io usa fuentes del sistema
- Instalar fuentes estándar (Arial, Courier New)
- O cambiar fuente en: Format → Font

### Problema: No puedo exportar a PDF

**Solución:**
- Usar "Archivo → Exportar como → PDF"
- Si falla, exportar a PNG primero y convertir

## 📚 Recursos Adicionales

- **Draw.io Documentation**: https://www.drawio.com/doc/
- **Shortcuts**: https://www.drawio.com/shortcuts
- **Shape Libraries**: https://www.drawio.com/blog/shape-libraries
- **Templates**: https://www.drawio.com/blog/templates

## 💡 Tips de Uso

1. **Zoom**: Usa Ctrl+Scroll para hacer zoom
2. **Alineación**: Selecciona elementos y usa Arrange → Align
3. **Duplicar**: Ctrl+D para duplicar elementos seleccionados
4. **Conectores**: Arrastra desde los puntos azules de las formas
5. **Estilos**: Copia estilo con Ctrl+Shift+C, pega con Ctrl+Shift+V
6. **Buscar**: Ctrl+F para buscar texto en el diagrama
7. **Capas**: Usa View → Layers para organizar elementos

## 🤝 Contribuciones

Si mejoras o añades diagramas:

1. Mantén el estilo visual consistente
2. Usa la misma paleta de colores
3. Incluye leyendas y anotaciones
4. Documenta los cambios en este README
5. Exporta versiones PNG para referencia rápida

---

*Diagramas creados para el proyecto PECL1 - Computación Ubicua - UAH*

