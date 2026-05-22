# Proyecto-SIGOMEI

Servidor RMI para la gestion de equipos, tecnicos y ordenes de mantenimiento.
La capa de servicio aplica las reglas de negocio RN-01 a RN-08 y expone el contrato remoto definido para E2.

## Tecnologias

- Java 17+
- Maven
- JUnit 5
- RMI
- MySQL

## Configuracion

El servidor lee configuracion externa desde `config/app.properties`.
No hay credenciales de base de datos embebidas en el codigo.

1. Copiar el archivo de ejemplo:

```powershell
Copy-Item config\app.properties.example config\app.properties
```

2. Editar `config/app.properties` con las credenciales locales de MySQL.

## Base de datos

El script de creacion y datos iniciales esta en:

```text
proyecto/src/main/resources/sql/sigomei_db.sql
```

## Compilar y probar

Desde `proyecto`:

```powershell
mvn clean test
```

Resultado esperado:

```text
Tests run: 61, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Ejecutar servidor RMI

Desde `proyecto`:

```powershell
mvn clean test
java -cp target\classes com.sigomei.servidor.rmi.SigomeiServer
```

El servidor publica el servicio remoto con el nombre `SIGOMEI`.

## Ejecutar cliente demo

En otra terminal, desde `proyecto`:

```powershell
java -cp target\classes com.sigomei.cliente.DemoClienteRmi
```

El cliente demo solo usa RMI. No importa `ConexionBD`, no contiene credenciales y no se conecta directamente a MySQL.

## Logs

La bitacora del servidor se genera en:

```text
proyecto/logs/server.log
```

La ruta puede cambiarse con `server.log.path` en `config/app.properties`.

## Evidencias

El paquete documental esta en:

```text
EVIDENCIAS_ENTREGA.md
```

Incluye salida de pruebas unitarias, seguimiento de casos de sistema, bitacora de defectos, auditoria de suite y trazabilidad.
