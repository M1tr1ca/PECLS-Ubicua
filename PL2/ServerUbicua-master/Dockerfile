# Dockerfile para el Servidor Ubicua - PECL2
# Basado en Tomcat 10 con soporte para Jakarta EE

# ===================================================
# ETAPA 1: Compilación con Maven
# ===================================================
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .

# Descargar dependencias (capa cacheada)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar y empaquetar el WAR
RUN mvn clean package -DskipTests -B

# ===================================================
# ETAPA 2: Imagen de producción con Tomcat
# ===================================================
FROM tomcat:10.1-jdk17

# Información del mantenedor
LABEL maintainer="UAH - Computación Ubicua"
LABEL description="Servidor para sistema de gestión urbana - PECL2"

# Variables de entorno para MQTT
ENV MQTT_BROKER="tcp://mqtt-broker:1883"
ENV MQTT_USERNAME="ubicua"
ENV MQTT_PASSWORD="ubicua"

# Eliminar aplicaciones por defecto de Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Copiar el driver de PostgreSQL al directorio lib de Tomcat
# (para que esté disponible para el pool de conexiones)
COPY --from=builder /root/.m2/repository/org/postgresql/postgresql/42.7.2/postgresql-42.7.2.jar /usr/local/tomcat/lib/

# Copiar el WAR compilado
COPY --from=builder /app/target/ServerExampleUbicomp-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ServerExampleUbicomp.war

# Exponer el puerto de Tomcat
EXPOSE 8080

# Healthcheck para verificar que el servidor está activo
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/ServerExampleUbicomp/ || exit 1

# Comando por defecto
CMD ["catalina.sh", "run"]

