# Proyecto SIGOMEI v1.0

Sistema cliente-servidor para la gestion de equipos, tecnicos y ordenes de mantenimiento.
El cliente se comunica con el servidor por RMI y el servidor guarda la informacion en MySQL.

## Tecnologias

- Java 17 o superior
- Maven Wrapper
- JUnit 5
- Java RMI
- JavaFX
- MySQL

## Configuracion

En el archivo `app.properties.example` de la carpeta `config` se muestra la configuracion que debe usarse:

```properties
db.url=jdbc:mysql://localhost:3306/sigomei_db
db.user=root
db.password=contrasena
```

El servidor lee la configuracion real desde:

```text
proyecto/config/app.properties
```

Ejemplo:

```properties
db.url=jdbc:mysql://localhost:3306/sigomei_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=tu_contrasena
rmi.registry.port=1099
rmi.object.port=1100
server.log.path=logs/server.log
```

## Crear Base de Datos

Desde la carpeta principal del proyecto, en PowerShell:

```powershell
cd C:\Users\Migue\proyecto_Sigomei\Proyecto-SIGOMEI
mysql -u root -p < .\proyecto\src\main\resources\sql\sigomei_db.sql
```

Este script crea lo necesario para ejecutar el sistema: base de datos, tablas, llaves foraneas y datos iniciales.

## Maven Wrapper

El proyecto incluye Maven Wrapper, por lo que en otra computadora no es necesario instalar Maven globalmente.
Desde la carpeta `proyecto` se usan comandos con:

```powershell
.\mvnw.cmd
```

Si la computadora ya tiene Maven instalado, tambien se puede usar `mvn`.

## Compilar

Desde la carpeta `proyecto`, en PowerShell:

```powershell
cd C:\Users\Migue\proyecto_Sigomei\Proyecto-SIGOMEI\proyecto
.\mvnw.cmd -DskipTests package
```

## Ejecutar Todas las Pruebas

Desde la carpeta `proyecto`:

```powershell
.\mvnw.cmd test
```

Resultado esperado:

```text
BUILD SUCCESS
Tests run: 71, Failures: 0, Errors: 0, Skipped: 1
```

El caso CP-35 queda omitido porque debe ejecutarse manualmente: requiere apagar el servidor mientras un cliente activo intenta hacer una operacion.

## Pruebas por Suite

Casos de prueba de sistema E2:

```powershell
.\mvnw.cmd -Dtest=SistemaE2Test test
```

Resultado esperado:

```text
Tests run: 35, Failures: 0, Errors: 0, Skipped: 1
```

Reglas de negocio RN-01 a RN-08:

```powershell
.\mvnw.cmd -Dtest=ReglasNegocioServiceTest test
```

Resultado esperado:

```text
Tests run: 26, Failures: 0, Errors: 0, Skipped: 0
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

## Ejecutar Servidor y Cliente

Para ejecutar servidor y cliente, primero vaya a la ruta:

```powershell
cd C:\Users\Migue\proyecto_Sigomei\Proyecto-SIGOMEI\proyecto
```

Servidor RMI:

```powershell
.\mvnw.cmd exec:java
```

La terminal del servidor debe quedarse abierta. Para detenerlo use `Ctrl + C`.

Cliente JavaFX, en otra terminal:

```powershell
.\mvnw.cmd javafx:run
```

Usuarios de prueba:

```text
admin / admin123
consulta / consulta123
```

## Logs

La bitacora del servidor se genera en:

```text
proyecto/logs/server.log
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
