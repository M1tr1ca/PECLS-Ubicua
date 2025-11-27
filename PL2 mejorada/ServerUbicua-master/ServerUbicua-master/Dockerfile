# Le decimos a Docker que emplee un Linux con java 10.1 y jdk instalado
FROM tomcat:10.1-jdk21
#Copia el .war generado por Maven al contenedor, es el .war que Tomcat empleará
COPY ./ServerUbicua-master/target/ServerExampleUbicomp-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/Server.war
#Informamos a Docker que este contenedor escuchará en el puerto 8080
EXPOSE 8080
#Comando para inciar tomcat y dejarlo ejecutando
CMD ["catalina.sh", "run"]