# Proyecto SIGOMEI v1.0

Servidor RMI y cliente JavaFX para la gestion de equipos, tecnicos y ordenes de mantenimiento.
La capa de servicio aplica las reglas de negocio RN-01 a RN-08 y el servidor guarda la informacion en MySQL.

## Tecnologias

- Java 17 o superior
- Maven
- JUnit 5
- Java RMI
- JavaFX
- MySQL

## Configuracion

El servidor lee la configuracion de:

proyecto/config/app.properties

 ahi se configura la conexion local a MySQL:

properties
db.url=jdbc:mysql://localhost:3306/sigomei_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=contraseña
rmi.registry.port=1099
rmi.object.port=1100
server.log.path=logs/server.log


El script final de la bd  esta en  proyecto/src/main/resources/sql/sigomei_db.sql


## Compilar

en la ruta  proyecto y  en powershell
mvn -DskipTests package


## Ejecutar Servidor RMI

en  la carpeta proyecto y  en powershell ejecute 
mvn exec:java

El servidor publica el servicio remoto con el nombre SIGOMEI

## Ejecutar Cliente JavaFX

En otra terminal, igual en la carpeta proyecto y en powershell ejecute
mvn javafx:run


Usuarios  :
admin / admin123
consulta / consulta123


## Ejecutar Cliente Demo

 Si quiree probar el cliente desde la consola en una terminal y en  la carpeta proyecto y  en powershell ejecute
mvn exec:java "-Dexec.mainClass=com.sigomei.cliente.DemoClienteRmi"

El cliente se comunica por RMI. No contiene credenciales de base de datos y no se conecta directamente a MySQL.


## Logs

La bitacora del servidor se genera en proyecto/logs/server.log


## Ejecutar Todas las Pruebas

en la carpeta proyecto y en powershell ejecute 
mvn test


Resultado esperado:


Tests run: 71, Failures: 0, Errors: 0, Skipped: 1
BUILD SUCCESS


El caso omitido es el CP-35, porque requiere ejecucion manual apagando el servidor durante una operacion del cliente.

## Pruebas por Suite

Reglas de negocio RN-01 a RN-08:

powershell
mvn -Dtest=ReglasNegocioServiceTest test

Resultado esperado Tests run: 26, Failures: 0, Errors: 0, Skipped: 0


Casos de sistema E2  en powershell
mvn -Dtest=SistemaE2Test test


Resultado esperado Tests run: 35, Failures: 0, Errors: 0, Skipped: 1


Pruebas CRUD en powershell
mvn -Dtest=CrudServiceTest test
Resultado esperado  Tests run: 3, Failures: 0, Errors: 0, Skipped: 0


Pruebas adicionales de validacion  en powershell
mvn -Dtest=ValidacionesServiceTest test

Resultado esperado Tests run: 7, Failures: 0, Errors: 0, Skipped: 0


## Reporte de Cobertura

JaCoCo se genera al ejecutar en powershell
mvn test


El reporte queda en:

proyecto/target/site/jacoco/index.html

