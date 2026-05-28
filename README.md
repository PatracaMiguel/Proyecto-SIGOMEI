# Proyecto SIGOMEI v1.0

Servidor RMI y cliente JavaFX para la gestion de equipos, tecnicos y ordenes de mantenimiento.
La capa de servicio aplica las reglas de negocio RN-01 a RN-08 y el servidor guarda la informacion en MySQL.

## Tecnologias

- Java 17 o superior
- Maven Wrapper
- JUnit 5
- Java RMI
- JavaFX
- MySQL

## Configuracion

El servidor lee la configuracion desde:

```text
proyecto/config/app.properties
```

Ahi se configura la conexion local a MySQL:

```properties
db.url=jdbc:mysql://localhost:3306/sigomei_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=tu_contrasena
rmi.registry.port=1099
rmi.object.port=1100
server.log.path=logs/server.log
```

El script final de la base de datos esta en:

```text
proyecto/src/main/resources/sql/sigomei_db.sql
```

## Comandos con Maven Wrapper

En Windows no es necesario tener Maven instalado globalmente. Desde la carpeta `proyecto` use:

```powershell
.\mvnw.cmd comando
```

Si ya tiene Maven instalado tambien puede usar `mvn comando`.

## Compilar

Desde la carpeta `proyecto`:

```powershell
.\mvnw.cmd -DskipTests package
```

## Ejecutar Servidor RMI

Desde la carpeta `proyecto`:

```powershell
.\mvnw.cmd exec:java
```

El servidor publica el servicio remoto con el nombre `SIGOMEI`.
La terminal del servidor debe quedarse abierta. Para detenerlo use `Ctrl + C`.

## Ejecutar Cliente JavaFX

En otra terminal, desde la carpeta `proyecto`:

```powershell
.\mvnw.cmd javafx:run
```

Usuarios de prueba:

```text
admin / admin123
consulta / consulta123
```

## Ejecutar Cliente Demo

En otra terminal, desde la carpeta `proyecto`:

```powershell
.\mvnw.cmd exec:java "-Dexec.mainClass=com.sigomei.cliente.DemoClienteRmi"
```

El cliente se comunica por RMI. No contiene credenciales de base de datos y no se conecta directamente a MySQL.

## Logs

La bitacora del servidor se genera en:

```text
proyecto/logs/server.log
```

## Ejecutar Todas las Pruebas

Desde la carpeta `proyecto`:

```powershell
.\mvnw.cmd test
```

Resultado esperado:

```text
Tests run: 71, Failures: 0, Errors: 0, Skipped: 1
BUILD SUCCESS
```

El caso omitido es CP-35, porque requiere ejecucion manual apagando el servidor durante una operacion del cliente.

## Pruebas por Suite

Reglas de negocio RN-01 a RN-08:

```powershell
.\mvnw.cmd -Dtest=ReglasNegocioServiceTest test
```

Resultado esperado:

```text
Tests run: 26, Failures: 0, Errors: 0, Skipped: 0
```

Casos de sistema E2:

```powershell
.\mvnw.cmd -Dtest=SistemaE2Test test
```

Resultado esperado:

```text
Tests run: 35, Failures: 0, Errors: 0, Skipped: 1
```

Pruebas CRUD:

```powershell
.\mvnw.cmd -Dtest=CrudServiceTest test
```

Resultado esperado:

```text
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

Pruebas adicionales de validacion:

```powershell
.\mvnw.cmd -Dtest=ValidacionesServiceTest test
```

Resultado esperado:

```text
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
```

## Reporte de Cobertura

JaCoCo se genera al ejecutar:

```powershell
.\mvnw.cmd test
```

El reporte queda en:

```text
proyecto/target/site/jacoco/index.html
```
