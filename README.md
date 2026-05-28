# Proyecto-SIGOMEI v1.0
Servidor RMI para la gestion de equipos, tecnicos y ordenes de mantenimiento.
La capa de servicio aplica las reglas de negocio RN-01 a RN-08 y expone el contrato remoto definido para E2.

## Tecnologias

- Java 17
- Maven
- JUnit 5
- RMI
- MySQL

## Configuracion

El servidor lee configuracion externa desde `config/app.properties`. , edite este archivo con las credenciales locales de MySQL.

## Correr pruebas

Desde `proyecto` y en la powershell escriba mvn clean test

Resultado esperado:
Tests run: 71, Failures: 0, Errors: 0, Skipped: 1
BUILD SUCCESS

## Ejecutar servidor RMI

Desde `proyecto` y en powershell 
java -cp target\classes com.sigomei.servidor.rmi.SigomeiServer
El servidor publica el servicio remoto con el nombre `SIGOMEI`.

## Ejecutar cliente demo

En otra terminal, desde `proyecto` ejecute java -cp target\classes com.sigomei.cliente.DemoClienteRmi
El cliente demo solo usa RMI. No importa `ConexionBD`, no contiene credenciales y no se conecta directamente a MySQL.

## Ejecutar cliente RMI
Desde `proyecto` ejecute 
mvn javafx:run
El cliente utiliza JDBC para conectarse a MySQL 

## Logs
La bitacora del servidor se genera en:
proyecto/logs/server.log
La ruta puede cambiarse con `server.log.path` en `config/app.properties`.

## Evidencias

## Pruebas de las reglas de negocio de la E3 en verde 
en la ruta  Proyecto-SIGOMEI_v1.0\proyecto ejecute
 mvn -Dtest=ReglasNegocioServiceTest test

Saida esperad
ReglasNegocioServiceTest
Tests run: 26, Failures: 0, Errors: 0, Skipped: 0

## Casos de prueba de la E2 en verde
en la ruta  Proyecto-SIGOMEI_v1.0\proyecto ejecute 
 mvn -Dtest=SistemaE2Test test

 Saida esperada
 Running com.sigomei.servidor.service.SistemaE2Test
Tests run: 32, Failures: 0, Errors: 0, Skipped: 0

## pruebas de CRUD
en la ruta  Proyecto-SIGOMEI_v1.0\proyecto ejecute 
 mvn -Dtest=CrudServiceTest test

Saida esperada
Running com.sigomei.servidor.service.CrudServiceTest
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0

## Preubas de validacion de Service
en la ruta  Proyecto-SIGOMEI_v1.0\proyecto ejecute 
 mvn -Dtest=ValidacionServiceTest test

Saida esperada
Running com.sigomei.servidor.service.ValidacionServiceTest
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0