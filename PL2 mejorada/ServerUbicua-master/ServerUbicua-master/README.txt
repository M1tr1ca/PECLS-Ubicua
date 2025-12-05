Requisitos previos:
- Docker
- Maven (para compilación de Java)
- JDK21
Estos dos últimos solo necesario en caso de que no se encuentre .war

IMPORTANTE: Se debe revisar que se encuentre el .war
En Windows --> Antes de ejecutar cualquier comando Docker, se debe recordar que Docker Desktop debe encontrarse iniciado.
En Linux --> No es necesaria ninguna consideración adicional.

Comando para iniciar: 

- docker-compose up -d (se ejecuta en segundo plano y los Logs no aparecen en la consola)
- docker-compose up --build (aparecen los Logs en la consola, le obliga a leer de nuevo el DockerFile)

Comando para desmontar:

- docker-compose down -v (con -v nos elimina los volúmenes asociados a esos contenedores)



